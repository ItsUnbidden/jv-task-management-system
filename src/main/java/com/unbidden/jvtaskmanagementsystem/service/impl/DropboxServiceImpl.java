package com.unbidden.jvtaskmanagementsystem.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.check.EchoResult;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.sharing.AccessLevel;
import com.dropbox.core.v2.sharing.AclUpdatePolicy;
import com.dropbox.core.v2.sharing.AddFolderMemberErrorException;
import com.dropbox.core.v2.sharing.AddMember;
import com.dropbox.core.v2.sharing.MemberSelector;
import com.dropbox.core.v2.sharing.MountFolderErrorException;
import com.dropbox.core.v2.sharing.RelinquishFolderMembershipErrorException;
import com.dropbox.core.v2.sharing.RemoveFolderMemberErrorException;
import com.dropbox.core.v2.sharing.ShareFolderErrorException;
import com.dropbox.core.v2.sharing.ShareFolderLaunch;
import com.dropbox.core.v2.sharing.TransferFolderErrorException;
import com.unbidden.jvtaskmanagementsystem.dto.project.internal.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.project.internal.ProjectConnectedToDropboxResult;
import com.unbidden.jvtaskmanagementsystem.dto.task.internal.CreatedTaskFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult.ThirdPartyOperationStatus;
import com.unbidden.jvtaskmanagementsystem.exception.ThirdPartyApiException;
import com.unbidden.jvtaskmanagementsystem.exception.dropbox.GeneralDropboxException;
import com.unbidden.jvtaskmanagementsystem.exception.dropbox.SpecificDropboxException;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizedClientLoadingException;
import com.unbidden.jvtaskmanagementsystem.model.ClientRegistration;
import com.unbidden.jvtaskmanagementsystem.model.OAuth2AuthorizedClient;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.OAuth2Service;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

@Service
public class DropboxServiceImpl implements DropboxService {
    private static final Logger LOGGER = LogManager.getLogger(DropboxServiceImpl.class);

    private final ClientRegistration clientRegistration;

    private final OAuth2Service oauthService;

    private final DbxRequestConfig dbxRequestConfig;

    private final String dropboxRootPath;

    private final EntityUtil entityUtil;

    public DropboxServiceImpl(@Autowired EntityUtil entityUtil,
            @Autowired OAuth2Service oauthService,
            @Autowired DbxRequestConfig dbxRequestConfig,
            @Value("${dropbox.root.path}") String dropboxRootPath) {
        this.clientRegistration = entityUtil.getClientRegistrationByName("dropbox");
        this.oauthService = oauthService;
        this.dbxRequestConfig = dbxRequestConfig;
        this.entityUtil = entityUtil;
        this.dropboxRootPath = dropboxRootPath;
    }

    @Nullable
    @Override
    public CreatedProjectFolderResult createSharedProjectFolder(@NonNull User user, @NonNull Project project) {
        try {
            final OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(user,
                    clientRegistration);
            DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig,
                    authorizedClient.getToken());
            return createSharedProjectFolder0(dbxClient, project);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            LOGGER.warn("Since user " + user.getId() + " hasn't connected their dropbox account,"
                    + " the project will not have shared folder. It can be connected later.");
        }
        return null;
    }

    @NonNull
    @Override
    public ThirdPartyOperationResult deleteProjectFolder(@NonNull User user, @NonNull Project project) {
        if (project.isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                dbxClient.files().deleteV2(project.getDropboxProjectFolderId());
                return new ThirdPartyOperationResult(ThirdPartyOperationStatus.SUCCESS);
            } catch (DeleteErrorException e) {
                LOGGER.error("Uanble to delete project " + project.getId()
                        + "'s folder on dropbox. This might have happened because project folder does not exist.", e);
                return new ThirdPartyOperationResult(ThirdPartyOperationStatus.FAILED);
            } catch (DbxException e) {
                throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                LOGGER.warn("Cannot load authorized client for user " + user.getUsername()
                        + ". Action skipped.");
                return new ThirdPartyOperationResult(ThirdPartyOperationStatus.SKIPPED);
            }
        }
        return new ThirdPartyOperationResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @Nullable
    @Override
    public CreatedTaskFolderResult createTaskFolder(@NonNull User user, @NonNull Task task) {
        if (task.getProject().isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                return createTaskFolder0(dbxClient, task);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                LOGGER.warn("Cannot load authorized client for user " + user.getUsername()
                        + ". Action skipped.");
            }
        }
        return null;
    }

    @NonNull
    @Override
    public ThirdPartyOperationResult deleteTaskFolder(@NonNull User user, @NonNull Task task) {
        if (task.getProject().isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                if (task.getDropboxTaskFolderId() == null) {
                    LOGGER.error("Task %s does not have a Dropbox folder reference.".formatted(task.getName()));
                    return new ThirdPartyOperationResult(ThirdPartyOperationStatus.FAILED);
                }
                dbxClient.files().deleteV2(task.getDropboxTaskFolderId());
                return new ThirdPartyOperationResult(ThirdPartyOperationStatus.SUCCESS);
            } catch (CreateFolderErrorException e) {
                LOGGER.error("Unable to delete a folder for task " + task.getId()
                        + "This might have happened because task folder does not exist.", e);
                return new ThirdPartyOperationResult(ThirdPartyOperationStatus.FAILED);
            } catch (DbxException e) {
                throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                LOGGER.warn("Cannot load authorized client for user " + user.getUsername()
                        + ". Action skipped.");
                return new ThirdPartyOperationResult(ThirdPartyOperationStatus.SKIPPED);
            }
        }
        return new ThirdPartyOperationResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public ThirdPartyOperationResult addProjectMemberToSharedFolder(@NonNull User user, @NonNull User newMember,
            Project project) {
        if (project.isDropboxConnected()) {
            if (user.getId().equals(newMember.getId())) {
                throw new UnsupportedOperationException("User " + user.getId() 
                        + " cannot add themselfs to project " + project.getId() + ".");
            }

            try {
                if (!addUsersToDropboxFolder(getDbxClient(user), project, List.of(newMember)).isEmpty()) {
                    return new ThirdPartyOperationResult(ThirdPartyOperationStatus.SUCCESS);
                }
            } catch (OAuth2AuthorizedClientLoadingException e) {
                LOGGER.warn("Unable to load authorized client for user %s. Action skipped."
                        .formatted(user.getUsername()));      
                return new ThirdPartyOperationResult(ThirdPartyOperationStatus.SKIPPED);      
            }
        }
        return new ThirdPartyOperationResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public ThirdPartyOperationResult removeMemberFromSharedFolder(@NonNull User user, @NonNull User memberToRemove,
            Project project) {
        if (project.isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                if (user.getId().equals(memberToRemove.getId())) {
                    try {
                        dbxClient.sharing().relinquishFolderMembership(
                                project.getDropboxProjectSharedFolderId());
                        return new ThirdPartyOperationResult(ThirdPartyOperationStatus.SUCCESS);
                    } catch (RelinquishFolderMembershipErrorException e) {
                        LOGGER.warn("Failed to relinquish membership of the shared dropbox folder.");
                        return new ThirdPartyOperationResult(ThirdPartyOperationStatus.FAILED);
                    }
                }
                final OAuth2AuthorizedClient authorizedClientForUserToRemove =
                        oauthService.loadAuthorizedClient(memberToRemove, clientRegistration);
                try {
                    dbxClient.sharing().removeFolderMember(
                            project.getDropboxProjectSharedFolderId(),
                            MemberSelector.dropboxId(authorizedClientForUserToRemove
                            .getExternalAccountId()), false);
                    return new ThirdPartyOperationResult(ThirdPartyOperationStatus.SUCCESS);
                } catch (RemoveFolderMemberErrorException e) {
                    LOGGER.warn("Failed to remove user from the shared dropbox folder.");
                    return new ThirdPartyOperationResult(ThirdPartyOperationStatus.FAILED);
                }   
            } catch (DbxException e) {
                throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
            } catch (OAuth2AuthorizedClientLoadingException e1) {
                LOGGER.warn("User %s and/or user %s do not have an authorized client. Action skipped."
                        .formatted(user.getUsername(), memberToRemove.getUsername()));
                return new ThirdPartyOperationResult(ThirdPartyOperationStatus.SKIPPED);
            }
        }
        return new ThirdPartyOperationResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @Override
    public void transferOwnership(@NonNull User user, @NonNull User newOwner,
            Project project) {
        if (project.isDropboxConnected()) {
            if (user.getId().equals(newOwner.getId())) {
                throw new UnsupportedOperationException("User " + user.getId() 
                        + " cannot transfer project " + project.getId() 
                        + "'s folder to themselfs.");
            }

            OAuth2AuthorizedClient authorizedClientForNewUser;
            try {
                authorizedClientForNewUser = oauthService.loadAuthorizedClient(newOwner,
                        clientRegistration);        
            } catch (OAuth2AuthorizedClientLoadingException e) {
                throw new UnsupportedOperationException("Unable to transfer project "
                        + project.getId() + "'s fodler to user " + newOwner.getId()
                        + " because new owner does not have dropbox connected.");
            }

            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                dbxClient.sharing().transferFolder(project.getDropboxProjectSharedFolderId(),
                        authorizedClientForNewUser.getExternalAccountId());
            } catch (TransferFolderErrorException e) {
                throw new SpecificDropboxException("Unable to transfer project " 
                        + project.getId() + "'s folder to user " + newOwner.getId() + ".", e);
            } catch (DbxException e) {
                throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                LOGGER.warn("User %s does not have an authorized client. Action skipped."
                        .formatted(user.getUsername()));
            }
        }
    }

    @NonNull
    @Override
    public ProjectConnectedToDropboxResult connectProjectToDropbox(@NonNull User user,
            @NonNull Project project) {
        if (project.isDropboxConnected()) {
            throw new UnsupportedOperationException("No need to connect project " 
                    + project.getId() + " to dropbox because it is already connected.");
        }
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            final CreatedProjectFolderResult projectFolderResult =
                    createSharedProjectFolder0(dbxClient, project);
            final Map<Long, CreatedTaskFolderResult> taskFolderResults = new HashMap<>();

            // Only set for convenience in this service. Will not be persisted later.
            project.setDropboxProjectFolderId(projectFolderResult.getProjectFolderId());
            project.setDropboxProjectSharedFolderId(projectFolderResult.getProjectSharedFolderId());
            for (Task task : project.getTasks()) {
                taskFolderResults.put(task.getId(), createTaskFolder0(dbxClient, task));
            }
            final Set<Long> connectedUserIds = addUsersToDropboxFolder(dbxClient, project,
                    project.getProjectRoles().stream()
                    .filter(pr -> !pr.getRoleType().equals(ProjectRoleType.CREATOR))
                    .map(pr -> pr.getUser())
                    .toList());
            connectedUserIds.add(user.getId());
            return new ProjectConnectedToDropboxResult(projectFolderResult, taskFolderResults, connectedUserIds);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            throw new UnsupportedOperationException("Cannot connect project %s to Dropbox, because user %s does not have Dropbox connected."
                    .formatted(project.getName(), user.getId()));
        }
    }

    @Override
    public void joinDropbox(@NonNull User user, @NonNull Project project) {
        if (project.isDropboxConnected()) {
            final ProjectRole projectRole = entityUtil.getProjectRoleByProjectIdAndUserId(project.getId(), user.getId());

            if (projectRole.isDropboxConnected()) {
                throw new UnsupportedOperationException("No need to join Dropbox in this project, because the user %s is already connected.".formatted(user.getUsername()));
            }
            final User projectOwner = entityUtil.getProjectOwner(project);
            try {
                final DbxClientV2 dbxClient = getDbxClient(projectOwner);
    
                if (addUsersToDropboxFolder(dbxClient, project, List.of(user)).isEmpty()) {
                    throw new SpecificDropboxException("Failed to join Dropbox because there was no authorized client for user %s."
                            .formatted(user.getUsername()));
                }
            } catch (OAuth2AuthorizedClientLoadingException e) {
                throw new UnsupportedOperationException("Unable to join Dropbox in project %s, because user %s is not connected to Dropbox."
                        .formatted(project.getName(), user.getUsername(), projectOwner.getUsername()));
            }
        }
    }

    @NonNull
    @Override
    public FileMetadata uploadFileInTaskFolder(@NonNull User user, @NonNull Task task,
            MultipartFile file) {
        if (task.getProject().isDropboxConnected()) {
            
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                Metadata taskFolderMeta = dbxClient.files()
                        .getMetadata(task.getDropboxTaskFolderId());
                return dbxClient.files().uploadBuilder(taskFolderMeta.getPathLower()
                        + "/" + removeDangerousChars(file.getOriginalFilename()))
                        .withAutorename(true)
                        .uploadAndFinish(file.getInputStream());
            } catch (UploadErrorException e) {
                throw new SpecificDropboxException("Unable to upload a file "
                        + file.getOriginalFilename() + " to dropbox.", e);
            } catch (DbxException e) {
                throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
            } catch (IOException e) {
                throw new ThirdPartyApiException("Dropbox client was unable to read the byte array "
                        + "or it is unaccessable.", e);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                throw new UnsupportedOperationException("Unable to upload a file because user"
                        + " %s is not connected to Dropbox.".formatted(user.getUsername()));
            }
        }
        throw new UnsupportedOperationException("File upload for task " + task.getId()
                + " is currently unavailable since project " + task.getProject().getId()
                + " is not connected to dropbox. Project creator can connect the project.");
    }

    @NonNull
    @Override
    public FileMetadata downloadFile(@NonNull User user, @NonNull String dropboxId,
            @NonNull OutputStream os) {
                
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            return dbxClient.files().download(dropboxId).download(os);
        } catch (DownloadErrorException e) {
            throw new SpecificDropboxException("Unable to download a file " + dropboxId
                    + " from dropbox.", e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
        } catch (IOException e) {
            throw new ThirdPartyApiException("Dropbox client was unable to write to "
                    + "the output stream.", e);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            throw new UnsupportedOperationException("Unable to download the file, because "
                    + "user %s is not connected to Dropbox.".formatted(user.getUsername()));
        }
    }

    @Override
    @NonNull
    public void deleteFile(User user, String dropboxId) {
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            dbxClient.files().deleteV2(dropboxId);
        } catch (DeleteErrorException e1) {
            throw new SpecificDropboxException("Unable to delete a file. You might need to manually delete the file from Dropbox.", e1);
        } catch (DbxException e2) {
            throw new GeneralDropboxException("A general dropbox exception was thrown.", e2);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            throw new UnsupportedOperationException("Unable to delete the file, because "
                    + "user %s is not connected to Dropbox.".formatted(user.getUsername()));
        }
    }

    @NonNull
    @Override
    public EchoResult testDropboxUserConnection(@NonNull User user) {
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            return dbxClient.check().user("This is a test query. If you see this, that means "
                    + "that dropbox is probably operational.");
        } catch (DbxApiException e) {
            throw new SpecificDropboxException("Unable to test dropbox.", e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            throw new UnsupportedOperationException("Unable to check connection, because "
                    + "user %s is not connected to Dropbox.".formatted(user.getUsername()));
        }
    }
    
    @Override
    public void logout(@NonNull User user) {
        try {
            final OAuth2AuthorizedClient authorizedClient =
                    oauthService.loadAuthorizedClient(user, clientRegistration);
            final DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig,
                    authorizedClient.getToken());
            try {
                dbxClient.auth().tokenRevoke();
                oauthService.deleteAuthorizedClient(authorizedClient);
            } catch (DbxApiException e) {
                throw new SpecificDropboxException("Unable to logout.", e);
            } catch (DbxException e) {
                throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
            }
        } catch (OAuth2AuthorizedClientLoadingException e) {
            throw new UnsupportedOperationException("Unable to logout because user is not "
                    + "currently logged in.", e);
        }
    }

    private DbxClientV2 getDbxClient(User user) throws OAuth2AuthorizedClientLoadingException {
        final OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(
                user, clientRegistration);
        LOGGER.debug("Creating DbxClient object for dropbox access.");
        return new DbxClientV2(dbxRequestConfig, authorizedClient.getToken());
    }

    private CreatedProjectFolderResult createSharedProjectFolder0(DbxClientV2 dbxClient, Project project) {
        try {
            CreateFolderResult folderMeta = dbxClient.files()
                    .createFolderV2(dropboxRootPath + "/" + project.getName(),
                    true);
            ShareFolderLaunch sharedFolderMeta = dbxClient.sharing()
                    .shareFolderBuilder(folderMeta.getMetadata().getId())
                    .withAclUpdatePolicy(AclUpdatePolicy.EDITORS)
                    .start();

            return new CreatedProjectFolderResult(folderMeta.getMetadata().getId(),
                    sharedFolderMeta.getCompleteValue().getSharedFolderId());
        } catch (ShareFolderErrorException e) {
            if (project.getId() == null) {
                throw new SpecificDropboxException("Unable to create shared folder for "
                        + "new project " + project.getName() + ".", e);
            }
            throw new SpecificDropboxException("Unable to create shared folder for project "
                    + project.getId() + ".", e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
        }
    }

    private CreatedTaskFolderResult createTaskFolder0(DbxClientV2 dbxClient, Task task) {
        try {
            final Metadata projectFolderMeta = dbxClient.files().getMetadata(
                    task.getProject().getDropboxProjectFolderId());
            final CreateFolderResult taskFolderMeta = dbxClient.files().createFolderV2(
                    projectFolderMeta.getPathLower() + "/" + task.getName(), true);
            return new CreatedTaskFolderResult(taskFolderMeta.getMetadata().getId());
        } catch (CreateFolderErrorException e) {
            throw new SpecificDropboxException("Unable to create a folder for task "
                    + task.getId(), e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
        }
    }

    private Set<Long> addUsersToDropboxFolder(DbxClientV2 dbxClient, Project project, List<User> users) {
        final Set<Long> connectedUsers = new HashSet<>();

        for (User user : users) {
            try {
                final OAuth2AuthorizedClient authorizedClient =
                        oauthService.loadAuthorizedClient(user, clientRegistration);
                final DbxClientV2 dbxClientNewUser = new DbxClientV2(dbxRequestConfig,
                        authorizedClient.getToken());
                final MemberSelector newMemberSelector = MemberSelector.dropboxId(
                        authorizedClient.getExternalAccountId());

                if (newMemberSelector != null) {
                    final AddMember addMember = new AddMember(newMemberSelector,
                            AccessLevel.EDITOR);
                    dbxClient.sharing().addFolderMember(
                            project.getDropboxProjectSharedFolderId(), List.of(addMember));
                    dbxClientNewUser.sharing().mountFolder(
                            project.getDropboxProjectSharedFolderId());
                    connectedUsers.add(user.getId());
                }
            } catch (AddFolderMemberErrorException e) {
                throw new SpecificDropboxException("Unable to add user " + user
                        .getId() + " to project " + project.getId() + "'s folder.", e);
            } catch (MountFolderErrorException e) {
                throw new SpecificDropboxException("Unable to mount project "
                        + project.getId() + "'s shared folder for newly added user "
                        + user.getId() + ".", e);
            } catch (DbxException e) {
                throw new GeneralDropboxException("A general dropbox exception was thrown.", e);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                LOGGER.warn("Unable to load an authorized client for user %s. User will not be connected to Dropbox in project %s."
                        .formatted(user.getUsername(), project.getName()));
            }
        }
        return connectedUsers;
    }

    private String removeDangerousChars(String filename) {
        List<Character> forbidden = new ArrayList<>();
        forbidden.add('.');
        forbidden.add('\\');
        forbidden.add('/');
        forbidden.add('\"');
        forbidden.add('<');
        forbidden.add('>');
        forbidden.add('?');
        forbidden.add('|');
        forbidden.add('*');
        forbidden.add(':');

        String noExt = StringUtils.stripFilenameExtension(filename);
        for (Character ch : forbidden) {
            noExt = noExt.replace(ch, '_');
        }        
        return noExt + '.' + StringUtils.getFilenameExtension(filename);
    }
}
