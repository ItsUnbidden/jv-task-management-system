package com.unbidden.jvtaskmanagementsystem.service;

import com.unbidden.jvtaskmanagementsystem.dto.google.GoogleSuccessfulTestResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.time.LocalDate;
import org.springframework.lang.NonNull;

public interface GoogleCalendarService {
    void createCalendarForProject(@NonNull User user, @NonNull Project project);

    void deleteProjectCalendar(@NonNull User user, @NonNull Project project);

    void createEventForTask(@NonNull User user, @NonNull Task task);

    void deleteTaskEvent(@NonNull User user, @NonNull Task task);

    void addUserToCalendar(@NonNull Project project, @NonNull User newUser);

    void removeUserFromCalendar(@NonNull Project project, @NonNull User userToRemove);

    void transferOwnership(@NonNull User user, @NonNull Project project, @NonNull User newOwner);

    void connectProjectToCalendar(@NonNull User user, @NonNull Project project);

    void joinCalendar(@NonNull User user, @NonNull Project project);

    void changeProjectEventsDates(@NonNull User user, @NonNull Project project,
            @NonNull LocalDate newStart, @NonNull LocalDate newEnd);

    void changeTaskEventDueDate(@NonNull User user, @NonNull Task task,
            @NonNull LocalDate newDueDate);

    GoogleSuccessfulTestResponseDto test(@NonNull User user);

    void logout(@NonNull User user);
}
