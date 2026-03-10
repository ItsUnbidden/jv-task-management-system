package com.unbidden.jvtaskmanagementsystem.service;

import java.io.OutputStream;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.core.v2.check.EchoResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;

public interface DropboxService {
    void createSharedProjectFolder(@NonNull User user, @NonNull Project project);

    void deleteProjectFolder(@NonNull User user, @NonNull Project project);

    void createTaskFolder(@NonNull User user, @NonNull Task task);

    void deleteTaskFolder(@NonNull User user, @NonNull Task task);

    void addProjectMemberToSharedFolder(@NonNull User user, @NonNull User newMember,
            @NonNull Project project);

    void removeMemberFromSharedFolder(@NonNull User user, @NonNull User memberToRemove,
            @NonNull Project project);

    void transferOwnership(@NonNull User user, @NonNull User newOwner, @NonNull Project project);

    void connectProjectToDropbox(@NonNull User user, @NonNull Project project);

    void joinDropbox(@NonNull User user, @NonNull Project project);

    FileMetadata uploadFileInTaskFolder(@NonNull User user, @NonNull Task task,
            @NonNull MultipartFile file);

    FileMetadata downloadFile(@NonNull User user, @NonNull String dropboxId,
            @NonNull OutputStream os);

    void deleteFile(@NonNull User user, @NonNull String dropboxId);

    EchoResult testDropboxUserConnection(@NonNull User user);

    void logout(@NonNull User user);
}
