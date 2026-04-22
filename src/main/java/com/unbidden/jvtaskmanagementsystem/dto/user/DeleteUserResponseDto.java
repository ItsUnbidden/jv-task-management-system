package com.unbidden.jvtaskmanagementsystem.dto.user;

import lombok.Data;

@Data
public class DeleteUserResponseDto {
    private Integer totalDeletedProjects;

    private Integer totalOwnProjectsWithDropbox;

    private Integer totalOwnProjectsWithCalendar;

    private Integer totalOwnProjectsWithDropboxFullyDeleted;

    private Integer totalOwnProjectsWithCalendarFullyDeleted;

    private Integer totalProjectsQuit;

    private Integer totalOtherProjectsWithDropbox;

    private Integer totalOtherProjectsWithCalendar;

    private Integer totalOtherProjectsWithDropboxQuit;

    private Integer totalOtherProjectsWithCalendarQuit;
}
