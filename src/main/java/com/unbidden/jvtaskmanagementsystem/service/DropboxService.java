package com.unbidden.jvtaskmanagementsystem.service;

import java.io.OutputStream;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AccountResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AddUserToProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedTaskFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DeleteResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.FileOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.ProjectConnectedToDropboxResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.RemoveUserFromProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.TransferOwnershipResult;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface DropboxService {
    @NonNull
    CreatedProjectFolderResult createSharedProjectFolder(@NonNull User user, @NonNull Project project);

    @NonNull
    DeleteResult deleteProjectFolder(@NonNull User user, @NonNull Project project);

    @NonNull
    CreatedTaskFolderResult createTaskFolder(@NonNull User user, @NonNull Task task);

    @NonNull
    DeleteResult deleteTaskFolder(@NonNull User user, @NonNull Task task);

    @NonNull
    AddUserToProjectFolderResult addProjectMemberToSharedFolder(@NonNull User user, @NonNull User newMember,
            @NonNull Project project);

    @NonNull
    RemoveUserFromProjectFolderResult removeMemberFromSharedFolder(@NonNull User user,
            @NonNull User memberToRemove, @NonNull Project project);

    @NonNull
    TransferOwnershipResult transferOwnership(@NonNull User user,
            @NonNull User newOwner, @NonNull Project project);

    @NonNull
    ProjectConnectedToDropboxResult connectProjectToDropbox(@NonNull User user, @NonNull Project project);

    @NonNull
    AddUserToProjectFolderResult joinDropbox(@NonNull User user, @NonNull Project project);

    @NonNull
    FileOperationResult uploadFileInTaskFolder(@NonNull User user, @NonNull Task task,
            @NonNull MultipartFile file);

    @NonNull
    FileOperationResult downloadFile(@NonNull User user, @NonNull String dropboxId,
            @NonNull OutputStream os);

    @NonNull
    DeleteResult deleteFile(@NonNull User user, @NonNull String dropboxId);

    @NonNull
    DropboxOperationResult testDropboxUserConnection(@NonNull User user);

    @NonNull
    DropboxOperationResult logout(@NonNull User user);

    @NonNull
    AccountResult getUserAccount(@NonNull User user);
}
