package com.unbidden.jvtaskmanagementsystem.service;

import com.dropbox.core.v2.check.EchoResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import java.io.OutputStream;
import org.springframework.web.multipart.MultipartFile;

public interface DropboxService {
    void createSharedProjectFolder(User user, Project project);

    void deleteProjectFolder(User user, Project project);

    void createTaskFolder(User user, Task task);

    void deleteTaskFolder(User user, Task task);

    void addProjectMemberToSharedFolder(User user, User newMember, Project project);

    void removeMemberFromSharedFolder(User user, User memberToRemove, Project project);

    void transferOwnership(User user, User newOwner, Project project);

    void connectProjectToDropbox(User user, Project project);

    FileMetadata uploadFileInTaskFolder(User user, Task task, MultipartFile file);

    FileMetadata downloadFile(User user, String dropboxId, OutputStream os);

    EchoResult testDropboxUserConnection(User user);

    void logout(User user);
}
