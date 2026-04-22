package com.unbidden.jvtaskmanagementsystem.service;

import java.io.OutputStream;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.core.v2.check.EchoResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.unbidden.jvtaskmanagementsystem.dto.project.internal.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.project.internal.ProjectConnectedToDropboxResult;
import com.unbidden.jvtaskmanagementsystem.dto.task.internal.CreatedTaskFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface DropboxService {
    @Nullable
    CreatedProjectFolderResult createSharedProjectFolder(@NonNull User user, @NonNull Project project);

    ThirdPartyOperationResult deleteProjectFolder(@NonNull User user, @NonNull Project project);

    @Nullable
    CreatedTaskFolderResult createTaskFolder(@NonNull User user, @NonNull Task task);

    void deleteTaskFolder(@NonNull User user, @NonNull Task task);

    ThirdPartyOperationResult addProjectMemberToSharedFolder(@NonNull User user, @NonNull User newMember,
            @NonNull Project project);

    ThirdPartyOperationResult removeMemberFromSharedFolder(@NonNull User user, @NonNull User memberToRemove,
            @NonNull Project project);

    void transferOwnership(@NonNull User user, @NonNull User newOwner, @NonNull Project project);

    @NonNull
    ProjectConnectedToDropboxResult connectProjectToDropbox(@NonNull User user, @NonNull Project project);

    void joinDropbox(@NonNull User user, @NonNull Project project);

    FileMetadata uploadFileInTaskFolder(@NonNull User user, @NonNull Task task,
            @NonNull MultipartFile file);

    FileMetadata downloadFile(@NonNull User user, @NonNull String dropboxId,
            @NonNull OutputStream os);

    void deleteFile(@NonNull User user, @NonNull String dropboxId);

    EchoResult testDropboxUserConnection(@NonNull User user);

    void logout(@NonNull User user);
}
