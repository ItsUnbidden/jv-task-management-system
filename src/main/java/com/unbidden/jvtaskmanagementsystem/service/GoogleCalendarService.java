package com.unbidden.jvtaskmanagementsystem.service;

import java.time.LocalDate;

import org.springframework.lang.NonNull;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.calendar.CalendarOperationResult;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface GoogleCalendarService {
    @NonNull
    CalendarOperationResult createCalendarForProject(@NonNull User user, @NonNull Project project);

    @NonNull
    CalendarOperationResult deleteProjectCalendar(@NonNull User user, @NonNull Project project);

    @NonNull
    CalendarOperationResult createEventForTask(@NonNull User user, @NonNull Task task);

    @NonNull
    CalendarOperationResult deleteTaskEvent(@NonNull User user, @NonNull Task task);

    @NonNull
    CalendarOperationResult addUserToCalendar(@NonNull Project project, @NonNull User newUser);

    @NonNull
    CalendarOperationResult removeUserFromCalendar(@NonNull Project project, @NonNull User userToRemove);

    @NonNull
    CalendarOperationResult transferOwnership(@NonNull User user, @NonNull Project project, @NonNull User newOwner);

    @NonNull
    CalendarOperationResult connectProjectToCalendar(@NonNull User user, @NonNull Project project);

    @NonNull
    CalendarOperationResult joinCalendar(@NonNull User user, @NonNull Project project);

    @NonNull
    CalendarOperationResult changeProjectEventsDates(@NonNull User user, @NonNull Project project,
            @NonNull LocalDate newStart, LocalDate newEnd);

    @NonNull
    CalendarOperationResult changeTaskEventDueDate(@NonNull User user, @NonNull Task task,
            LocalDate newDueDate);

    @NonNull
    CalendarOperationResult test(@NonNull User user);

    @NonNull
    CalendarOperationResult logout(@NonNull User user);
}
