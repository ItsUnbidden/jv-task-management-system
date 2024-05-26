package com.unbidden.jvtaskmanagementsystem.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Acl;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.AclRule.Scope;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.unbidden.jvtaskmanagementsystem.dto.google.GoogleSuccessfulTestResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.ThirdPartyApiException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizedClientLoadingException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectCalendar;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.TaskEvent;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectCalendarRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskEventRepository;
import com.unbidden.jvtaskmanagementsystem.repository.oauth2.ClientRegistrationRepository;
import com.unbidden.jvtaskmanagementsystem.service.GoogleCalendarService;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.OAuth2Service;
import com.unbidden.jvtaskmanagementsystem.util.BearerAuthentication;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import com.unbidden.jvtaskmanagementsystem.util.HttpClientUtil.HeaderNames;
import com.unbidden.jvtaskmanagementsystem.util.HttpClientUtil.HeaderValues;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoogleCalendarServiceImpl implements GoogleCalendarService {
    private static final Logger LOGGER = LogManager.getLogger(GoogleCalendarServiceImpl.class);
    
    private static final GsonFactory FACTORY = GsonFactory.getDefaultInstance();
    
    private static final int REMINDER_TIME = 24 * 60;

    private static final String GOOGLE_TOKEN_REVOKE_URL =
            "https://oauth2.googleapis.com/revoke?token=%s";

    private final ClientRegistration clientRegistration;

    private final OAuth2Service oauthService;

    private final ProjectCalendarRepository projectCalendarRepository;

    private final TaskEventRepository taskEventRepository;

    private final EntityUtil entityUtil;

    private final HttpClient http;

    public GoogleCalendarServiceImpl(
            @Autowired ClientRegistrationRepository clientRegistrationRepository,
            @Autowired OAuth2Service oauthService,
            @Autowired ProjectCalendarRepository projectCalendarRepository,
            @Autowired TaskEventRepository taskEventRepository,
            @Autowired EntityUtil entityUtil,
            @Autowired HttpClient http) {
        this.clientRegistration = clientRegistrationRepository.findByClientName("google").get();
        this.oauthService = oauthService;
        this.projectCalendarRepository = projectCalendarRepository;
        this.taskEventRepository = taskEventRepository;
        this.entityUtil = entityUtil;
        this.http = http;
    }
    
    @Override
    public void createCalendarForProject(User user, Project project) {
        Calendar service;
        try {
            service = getService(user);
            createCalendarForProject0(service, project);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            LOGGER.warn("Authorzied client for user " + user.getId() + " and service "
                    + clientRegistration.getClientName() + " is unavailable. "
                    + "Action skipped.");
        }

    }

    @Override
    public void deleteProjectCalendar(User user, Project project) {
        try {
            Optional<ProjectCalendar> projectCalendarOpt =
                    projectCalendarRepository.findByProjectId(project.getId());
            if (projectCalendarOpt.isPresent()) {
                try {
                    Calendar service = getService(user);
                    service.calendars().delete(projectCalendarOpt.get().getCalendarId()).execute();
                } catch (OAuth2AuthorizedClientLoadingException e) {
                    LOGGER.warn("Authorzied client for user " + user.getId() + " and service "
                            + clientRegistration.getClientName() + " is unavailable. To prevent"
                            + " a potential hard-locking of calendar related methods, "
                            + "local project calendar will be deleted but remote calendar will "
                            + "have to be deleted manualy.");
                }
                projectCalendarRepository.delete(projectCalendarOpt.get());
            } else {
                LOGGER.warn("Calendar for project " + project.getId()
                        + " doesn't exist. Action skipped.");
            }
        } catch (IOException e) {
            throw new ThirdPartyApiException("Unable to delete a calendar for project "
                    + project.getId(), e);
        }
    }
    
    @Override
    public void createEventForTask(User user, Task task) {
        if (isCalendarConnected(task.getProject())) {
            try {
                Calendar service = getService(user);
                createEventForTask0(service, task, task.getDueDate());
            } catch (OAuth2AuthorizedClientLoadingException e) {
                processAuthClientLoadingException(user);
            }
        } else {
            LOGGER.warn("Calendar for project " + task.getProject().getId()
                            + " doesn't exist. Action skipped.");
        }
    }
    
    @Override
    public void deleteTaskEvent(User user, Task task) {
        if (isCalendarConnected(task.getProject())) {
            try {
                Calendar service = getService(user);
                deleteTaskEvent0(service, task);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                processAuthClientLoadingException(user);
            }
        } else {
            LOGGER.warn("Calendar for project " + task.getProject().getId()
                            + " doesn't exist. Action skipped.");
        }
    }

    @Override
    public void addUserToCalendar(Project project, User newUser) {
        final User user = entityUtil.getProjectOwner(project);

        if (isCalendarConnected(project)) {
            try {
                Calendar service = getService(user);
                addUserToCalendar0(service, project, newUser);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                processAuthClientLoadingException(user);
            }
        } else {
            LOGGER.warn("Calendar for project " + project.getId()
                            + " doesn't exist. Action skipped.");
        }
    }

    @Override
    public void removeUserFromCalendar(Project project, User userToRemove) {
        
        try {
            Optional<ProjectCalendar> projectCalendarOpt =
                    projectCalendarRepository.findByProjectId(project.getId());
            if (projectCalendarOpt.isPresent()) {
                final Calendar service = getService(entityUtil.getProjectOwner(project));

                OAuth2AuthorizedClient authorizedClient =
                        oauthService.loadAuthorizedClient(userToRemove, clientRegistration);
                Acl acl = service.acl().list(projectCalendarOpt.get().getCalendarId()).execute();
                for (AclRule rule : acl.getItems()) {
                    if (rule.getScope().getValue().equals(
                            authorizedClient.getExternalAccountId())) {
                        service.acl().delete(projectCalendarOpt.get()
                                .getCalendarId(), rule.getId()).execute();
                        break;
                    }
                }
            } else {
                LOGGER.warn("Calendar for project " + project.getId()
                        + " doesn't exist. Action skipped.");
            }
        } catch (IOException e) {
            throw new ThirdPartyApiException("Unable to remove user "
                    + userToRemove.getId() + " from calendar for project " + project.getId(), e);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            LOGGER.warn("Unable to load authorized client for user "
                    + userToRemove.getId() + ". Action skipped.");
        }
    }

    @Override
    public void transferOwnership(User user, Project project, User newOwner) {
        try {
            Optional<ProjectCalendar> projectCalendarOpt =
                    projectCalendarRepository.findByProjectId(project.getId());
            if (projectCalendarOpt.isPresent()) {
                final Calendar service = getService(user);
                Calendar serviceNewOwner = getService(newOwner);

                OAuth2AuthorizedClient authorizedClient =
                        oauthService.loadAuthorizedClient(newOwner, clientRegistration);
                OAuth2AuthorizedClient authorizedClientOwner =
                        oauthService.loadAuthorizedClient(user, clientRegistration);
                Acl acl = service.acl().list(projectCalendarOpt.get().getCalendarId()).execute();
                for (AclRule rule : acl.getItems()) {
                    if (rule.getScope().getValue().equals(
                                authorizedClient.getExternalAccountId())) {
                        rule.setRole("owner");
                        service.acl().update(projectCalendarOpt.get().getCalendarId(),
                                rule.getId(), rule).setSendNotifications(false).execute();
                        break;
                    }
                }
                for (AclRule rule : acl.getItems()) {
                    if (rule.getScope().getValue().equals(
                                authorizedClientOwner.getExternalAccountId())) {
                        rule.setRole("writer");
                        serviceNewOwner.acl().update(projectCalendarOpt.get().getCalendarId(),
                                rule.getId(), rule).setSendNotifications(false).execute();
                        break;
                    }
                }
            } else {
                LOGGER.warn("Calendar for project " + project.getId()
                        + " doesn't exist. Action skipped.");
            }
        } catch (IOException e) {
            throw new ThirdPartyApiException("Unable to trasfer calendar for project "
                    + project.getId() + " to user " + newOwner.getId(), e);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            LOGGER.warn("Unable to load authorized client for user "
                    + newOwner.getId() + ". Action skipped.");
        }
    }

    @Override
    public void connectProjectToCalendar(User user, Project project) {
        Optional<ProjectCalendar> projectCalendarOpt =
                projectCalendarRepository.findByProjectId(project.getId());
        if (!projectCalendarOpt.isPresent()) {
            try {
                Calendar service = getService(user);
                createCalendarForProject0(service, project);

                List<User> projectMembers = project.getProjectRoles().stream()
                        .filter(pr -> !pr.getRoleType().equals(ProjectRoleType.CREATOR))
                        .map(pr -> pr.getUser())
                        .toList();
                for (User projectMember : projectMembers) {
                    addUserToCalendar0(service, project, projectMember);
                }
                for (Task task : project.getTasks()) {
                    createEventForTask0(service, task, task.getDueDate());
                }
            } catch (OAuth2AuthorizedClientLoadingException e) {
                processAuthClientLoadingException(user);
            }
        } else {
            throw new UnsupportedOperationException("No need to connect project "
                    + project.getId() + " to calendar because it is already connected.");
        }
    }

    @Override
    public void joinCalendar(User user, Project project) {
        Optional<ProjectCalendar> projectCalendarOpt =
                projectCalendarRepository.findByProjectId(project.getId());
        if (projectCalendarOpt.isPresent()) {
            try {
                Calendar service = getService(entityUtil.getProjectOwner(project));
                addUserToCalendar0(service, project, user);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                processAuthClientLoadingException(user);
            }
        } else {
            throw new UnsupportedOperationException("Unable to join calendar for project "
                    + project.getId() + " because project owner has not connected " 
                    + "project to google calendar.");
        }
    }

    @Override
    public void changeProjectEventsDates(User user, Project project,
            LocalDate newStart, LocalDate newEnd) {
        Optional<ProjectCalendar> projectCalendarOpt =
                projectCalendarRepository.findByProjectId(project.getId());
        if (projectCalendarOpt.isPresent()) {
            try {
                Calendar service = getService(user);
                try {
                    if (!project.getStartDate().equals(newStart)) {
                        LOGGER.info("Start date changed.");
                        service.events().patch(projectCalendarOpt.get().getCalendarId(),
                            projectCalendarOpt.get().getStartEventId(),
                            getEventWithNewDate(newStart)).execute();
                    }
                    
                    if ((project.getEndDate() == null && newEnd == null)
                            || (project.getEndDate() != null
                            && project.getEndDate().equals(newEnd))) {
                        LOGGER.info("End date is the same as before. Nothing happens.");
                        return;
                    }
                    if (project.getEndDate() != null) {
                        LOGGER.info("Project " + project.getId()
                                + " had an end date previously.");
                        if (newEnd == null) {
                            LOGGER.info("New end date is null. End event will be deleted.");
                            service.events().delete(projectCalendarOpt.get().getCalendarId(),
                                    projectCalendarOpt.get().getEndEventId()).execute();
                        } else {
                            LOGGER.info("Updating end date for project...");
                            service.events().patch(projectCalendarOpt.get().getCalendarId(),
                                    projectCalendarOpt.get().getEndEventId(),
                                    getEventWithNewDate(newEnd)).execute();
                        }
                    } else {
                        LOGGER.info("Project did not have an end date previously. "
                                + "Creating new event.");
                        Event endEvent = getEvent("Project '" + project.getName() + "' ends",
                                project.getDescription(), newEnd);
                        endEvent = service.events().insert(projectCalendarOpt.get()
                                .getCalendarId(), endEvent).execute();
                        projectCalendarOpt.get().setEndEventId(endEvent.getId());
                        projectCalendarRepository.save(projectCalendarOpt.get());
                    }
                } catch (IOException e) {
                    throw new ThirdPartyApiException("Unable to patch events with new project "
                            + project.getId() + " dates.", e);
                }
            } catch (OAuth2AuthorizedClientLoadingException e) {
                processAuthClientLoadingException(user);
            }
        } else {
            LOGGER.warn("Calendar for project " + project.getId()
                        + " doesn't exist. Action skipped.");
        }
    }

    @Override
    public void changeTaskEventDueDate(User user, Task task, LocalDate newDueDate) {
        Optional<ProjectCalendar> projectCalendarOpt =
                projectCalendarRepository.findByProjectId(task.getProject().getId());
        if (projectCalendarOpt.isPresent()) {
            if ((task.getDueDate() == null && newDueDate == null)
                    || (newDueDate != null && newDueDate.equals(task.getDueDate()))) {
                LOGGER.info("Due date is the same as before. Nothing happens.");
                return;
            }

            try {
                Calendar service = getService(user);
                if (task.getDueDate() != null) {
                    LOGGER.info("Task " + task.getId() + " previously had a due date.");
                    Optional<TaskEvent> taskEventOpt = taskEventRepository
                            .findByTaskId(task.getId());
                    if (newDueDate == null) {
                        LOGGER.info("Task " + task.getId() + " doesn't have a due date now. "
                                + "Deleting event...");
                        deleteTaskEvent0(service, task);
                    } else {
                        LOGGER.info("Updating event date for task " + task.getId());
                        try {
                            service.events().patch(projectCalendarOpt.get().getCalendarId(),
                                    taskEventOpt.get().getEventId(),
                                    getEventWithNewDate(newDueDate)).execute();
                        } catch (IOException e) {
                            throw new ThirdPartyApiException("Unable to patch due date event for "
                                    + "task " + task.getId(), e);
                        }
                    }
                } else {
                    LOGGER.info("Task " + task.getId() + " didn't have a due date. "
                            + " Creating new event...");
                    createEventForTask0(service, task, newDueDate);
                }
            } catch (OAuth2AuthorizedClientLoadingException e) {
                processAuthClientLoadingException(user);
            }
        } else {
            LOGGER.warn("Calendar for project " + task.getProject().getId()
                    + " doesn't exist. Action skipped.");
        }
    }

    @Override
    public GoogleSuccessfulTestResponseDto test(User user) {
        try {
            Calendar service = getService(user);

            com.google.api.services.calendar.model.Calendar calendar =
                    new com.google.api.services.calendar.model.Calendar();
            calendar.setSummary("Test Calendar");
            calendar.setDescription("Calendar for testing api functionality. "
                    + "This should have been deleted automaticaly. If it hasn't been deleted, "
                    + "you should do it manualy.");
            try {
                calendar = service.calendars().insert(calendar).execute();
                LOGGER.info("Calendar inserted.");
                Event event = getEvent("Test event", "Event for testing api functionality.",
                        LocalDate.now().plusDays(2));
                event = service.events().insert(calendar.getId(), event).execute();
                LOGGER.info("Event inserted.");
                service.events().delete(calendar.getId(), event.getId()).execute();
                LOGGER.info("Event deleted.");
                service.calendars().delete(calendar.getId()).execute();
                LOGGER.info("Calendar deleted.");
            } catch (IOException e) {
                throw new ThirdPartyApiException("Some part of the test flow has "
                        + "thrown an exception. ", e);
            }
        } catch (OAuth2AuthorizedClientLoadingException e) {
            processAuthClientLoadingException(user);
        }
        return new GoogleSuccessfulTestResponseDto("Google Calendar test flow completed. "
                    + "This means Google Calendar is most likely connected successfully.");
    }

    @Override
    public void logout(User user) {
        final List<ProjectCalendar> projectCalendars =
                projectCalendarRepository.findByCreatorId(user.getId());
        OAuth2AuthorizedClient authorizedClient;
        try {
            authorizedClient = oauthService.loadAuthorizedClient(user, clientRegistration);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            LOGGER.warn("Unable to load authorized client for user " + user.getId()
                    + ". This might be due to the fact that google refresh "
                    + "tokens have a limited lifespan. To avoid hard-locking of google services, "
                    + "user's authorized client will be deleted localy, but the actual token "
                    + "will not be properly revoked.");
            authorizedClient = oauthService.getAuthorizedClientForUser(user, clientRegistration);
            oauthService.deleteAuthorizedClient(authorizedClient);
            LOGGER.info("Authorized client for user " + user.getId() + " and service "
                    + clientRegistration.getClientName() + " has been successfully deleted.");
            projectCalendarRepository.deleteAll(projectCalendars);
            LOGGER.info("Project calendars for user " + user.getId()
                    + " have been successfully deleted localy.");
            return;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GOOGLE_TOKEN_REVOKE_URL.formatted(authorizedClient.getToken())))
                .POST(BodyPublishers.noBody())
                .setHeader(HeaderNames.CONTENT_TYPE, HeaderValues.APPLICATION_FORM_URLENCODED)
                .build();

        try {
            for (ProjectCalendar projectCalendar : projectCalendars) {
                deleteProjectCalendar(user, projectCalendar.getProject());
                LOGGER.info("Project calendar " + projectCalendar.getId()
                        + " has been successfully deleted remotely.");
                projectCalendarRepository.delete(projectCalendar);
                LOGGER.info("Project calendar " + projectCalendar.getId()
                        + " has been successfully deleted localy.");
            }
            LOGGER.info("Sending request to revoke user " + user.getId() + "'s token for service "
                    + clientRegistration.getClientName());
            HttpResponse<String> response = http.send(request, BodyHandlers.ofString());
            LOGGER.info("Response recieved.");

            if (response.statusCode() != 200) {
                throw new ThirdPartyApiException("Token revocation for user " + user.getId()
                        + " and service " + clientRegistration.getClientName() + " failed.");
            }
            oauthService.deleteAuthorizedClient(authorizedClient);
        } catch (IOException | InterruptedException e) {
            throw new ThirdPartyApiException("Unable to send request to revoke user "
                    + user.getId() + "'s token for service "
                    + clientRegistration.getClientName(), e);
        }
    }

    private void createCalendarForProject0(Calendar service, Project project) {
        com.google.api.services.calendar.model.Calendar calendar =
                new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary("Project " + project.getName());
        calendar.setDescription(project.getDescription());
        try {
            ProjectCalendar projectCalendar = new ProjectCalendar(project,
                    entityUtil.getProjectOwner(project));
            calendar = service.calendars().insert(calendar).execute();
            projectCalendar.setCalendarId(calendar.getId());

            Event startEvent = getEvent("Project '" + project.getName() + "' starts",
                    project.getDescription(), project.getStartDate());
            startEvent = service.events().insert(calendar.getId(), startEvent).execute();
            projectCalendar.setStartEventId(startEvent.getId());

            if (project.getEndDate() != null) {
                Event endEvent = getEvent("Project '" + project.getName() + "' ends",
                        project.getDescription(), project.getEndDate());
                endEvent = service.events().insert(calendar.getId(), endEvent).execute();
                projectCalendar.setEndEventId(endEvent.getId());
            }
            projectCalendarRepository.save(projectCalendar);
        } catch (IOException e) {
            throw new ThirdPartyApiException("Unable to create a calendar for project "
                    + project.getId(), e);
        }
    }

    private void createEventForTask0(Calendar service, Task task, LocalDate dueDate) {
        if (dueDate != null) {
            try {
                Optional<ProjectCalendar> projectCalendarOpt =
                        projectCalendarRepository.findByProjectId(task.getProject().getId());
                Event event = getEvent("Task '" + task.getName() + "' ends",
                        task.getDescription(), dueDate);
                event = service.events().insert(projectCalendarOpt.get().getCalendarId(),
                        event).execute();
                TaskEvent taskEvent = new TaskEvent(task, projectCalendarOpt.get());
                taskEvent.setEventId(event.getId());
                taskEventRepository.save(taskEvent);
            } catch (IOException e) {
                throw new ThirdPartyApiException("Unable to create an event for task "
                        + task.getId(), e);
            }
        } else {
            LOGGER.warn("Due date for task " + task.getId()
                    + " is not specified. Action skipped.");
        }
    }

    private void deleteTaskEvent0(Calendar service, Task task) {
        try {
            Optional<ProjectCalendar> projectCalendarOpt =
                    projectCalendarRepository.findByProjectId(task.getProject().getId());
            Optional<TaskEvent> taskEventOpt =
                    taskEventRepository.findByTaskId(task.getId());
            if (taskEventOpt.isPresent()) {
                service.events().delete(projectCalendarOpt.get().getCalendarId(),
                        taskEventOpt.get().getEventId()).execute();
                taskEventRepository.delete(taskEventOpt.get());
            } else {
                LOGGER.warn("Event for " + task.getId() + " doesn't exist. Action skipped.");
            }
        } catch (IOException e) {
            throw new ThirdPartyApiException("Unable to delete an event for task "
                    + task.getId(), e);
        }
    }

    private void addUserToCalendar0(Calendar service, Project project, User newUser) {
        try {
            final OAuth2AuthorizedClient authorizedClient =
                    oauthService.loadAuthorizedClient(newUser, clientRegistration);
            Optional<ProjectCalendar> projectCalendarOpt =
                    projectCalendarRepository.findByProjectId(project.getId());
            
            Acl acl = service.acl().list(projectCalendarOpt.get().getCalendarId()).execute();
            for (AclRule rule : acl.getItems()) {
                if (rule.getScope().getValue().equals(
                        authorizedClient.getExternalAccountId())) {
                    throw new UnsupportedOperationException("Unable to add user "
                            + newUser.getId() + " to project " + project.getId()
                            + "'s calendar because user is already a member.");
                }
            }

            AclRule aclRule = new AclRule();
            Scope scope = new Scope();
            scope.setType("user").setValue(authorizedClient.getExternalAccountId());
            aclRule.setScope(scope).setRole("writer");
            service.acl().insert(projectCalendarOpt.get().getCalendarId(), aclRule)
                    .setSendNotifications(false).execute();

            final Calendar newUserService = getService(newUser);
            CalendarListEntry calendarListEntry = new CalendarListEntry();
            calendarListEntry.setId(projectCalendarOpt.get().getCalendarId());
            newUserService.calendarList().insert(calendarListEntry).execute();
        } catch (IOException e) {
            throw new ThirdPartyApiException("Unable to add new user "
                    + newUser.getId() + " to calendar for project " + project.getId(), e);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            LOGGER.warn("Unable to load authorized client for user "
                    + newUser.getId() + ". Action skipped.");
        }
    }

    private Calendar getService(User user) throws OAuth2AuthorizedClientLoadingException {
        NetHttpTransport transport;
        OAuth2AuthorizedClient authorizedClient;

        try {
            LOGGER.info("Getting transport...");
            transport = GoogleNetHttpTransport.newTrustedTransport();
            LOGGER.info("Getting authorized client...");
            authorizedClient = oauthService.loadAuthorizedClient(user, clientRegistration);
        } catch (GeneralSecurityException | IOException e) {
            throw new ThirdPartyApiException("Unable to create transport.", e);
        }

        LOGGER.info("Creating calendar service...");
        Calendar service = new Calendar.Builder(transport, FACTORY,
                new BearerAuthentication(authorizedClient.getToken()))
                .setApplicationName("Task Management System").build();
        LOGGER.info("Calendar service created.");
        return service;
    }

    private Event getEvent(String name, String description, LocalDate date) {
        Event event = getEventWithNewDate(date);
        event.setSummary(name);
        event.setDescription(description);
    
        List<EventReminder> reminderOverrides = new ArrayList<>();
        reminderOverrides.add(new EventReminder()
                .setMethod("email").setMinutes(REMINDER_TIME));
        reminderOverrides.add(new EventReminder()
                .setMethod("popup").setMinutes(REMINDER_TIME));
        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(reminderOverrides);
        event.setReminders(reminders);
        return event;
    }

    private Event getEventWithNewDate(LocalDate date) {
        final String eventDateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        Event event = new Event();
        DateTime dateTime = new DateTime(eventDateStr);
        EventDateTime eventDateTime = new EventDateTime();
        eventDateTime.setDate(dateTime);
        event.setStart(eventDateTime);
        event.setEnd(eventDateTime);
        return event;
    }

    private boolean isCalendarConnected(Project project) {
        return projectCalendarRepository.findByProjectId(project.getId()).isPresent();
    }

    private void processAuthClientLoadingException(User user) {
        throw new ThirdPartyApiException("Authorized client for user " + user.getId()
                + " and service " + clientRegistration.getClientName() + " is "
                + "unavailable.");
    }
}
