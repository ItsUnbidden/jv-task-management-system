package com.unbidden.jvtaskmanagementsystem.service;

import com.unbidden.jvtaskmanagementsystem.dto.google.GoogleSuccessfulTestResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.time.LocalDate;

public interface GoogleCalendarService {
    void createCalendarForProject(User user, Project project);

    void deleteProjectCalendar(User user, Project project);

    void createEventForTask(User user, Task task);

    void deleteTaskEvent(User user, Task task);

    void addUserToCalendar(Project project, User newUser);

    void removeUserFromCalendar(Project project, User userToRemove);

    void transferOwnership(User user, Project project, User newOwner);

    void connectProjectToCalendar(User user, Project project);

    void joinCalendar(User user, Project project);

    void changeProjectEventsDates(User user, Project project,
            LocalDate newStart, LocalDate newEnd);

    void changeTaskEventDueDate(User user, Task task, LocalDate newDueDate);

    GoogleSuccessfulTestResponseDto test(User user);

    void logout(User user);
}
