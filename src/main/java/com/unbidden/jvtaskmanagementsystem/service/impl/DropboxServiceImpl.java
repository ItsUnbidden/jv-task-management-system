package com.unbidden.jvtaskmanagementsystem.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.RetryException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
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
import com.dropbox.core.v2.users.FullAccount;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult.ThirdPartyOperationStatus;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AccountResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AddUserToProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedTaskFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DeleteResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.FileOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.FileOperationResult.FileOperationErrorTag;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.ProjectConnectedToDropboxResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.RemoveUserFromProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.TransferOwnershipResult;
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
import com.unbidden.jvtaskmanagementsystem.exception.ErrorType;
import com.unbidden.jvtaskmanagementsystem.exception.oauth2.OAuth2AuthorizedClientLoadingException;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.ThirdPartyApiException;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.ThirdPartyExpectedException;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.ThirdPartyInconsistentTokenException;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.ThirdPartyOutOfRetriesException;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.ThirdPartyRetryTooLongException;
import com.unbidden.jvtaskmanagementsystem.exception.thirdparty.ThirdPartyUnknownException;
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
@SuppressWarnings("null")
public class DropboxServiceImpl implements DropboxService {
    private static final Logger LOGGER = LogManager.getLogger(DropboxServiceImpl.class);

    private static final int MAX_RETRY_ATTEMPTS = 1;

    private static final int MAX_RETRY_DURATION = 5000;

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

    @NonNull
    @Override
    public CreatedProjectFolderResult createSharedProjectFolder(@NonNull User user, @NonNull Project project) {
        try {
            final OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(user,
                    clientRegistration);
            DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig,
                    authorizedClient.getToken());
            return createSharedProjectFolder0(dbxClient, user, project);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new CreatedProjectFolderResult(ThirdPartyOperationStatus.SKIPPED);
        }
    }

    @NonNull
    @Override
    public DeleteResult deleteProjectFolder(@NonNull User user, @NonNull Project project) {
        if (project.isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                delete(dbxClient, user, dropboxRootPath);
                return new DeleteResult(ThirdPartyOperationStatus.SUCCESS);
            } catch (DeleteErrorException e) {
                return new DeleteResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new DeleteResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, DeleteResult::new);
            }
        }
        return new DeleteResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public CreatedTaskFolderResult createTaskFolder(@NonNull User user, @NonNull Task task) {
        if (task.getProject().isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                return createTaskFolder0(dbxClient, user, task);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new CreatedTaskFolderResult(ThirdPartyOperationStatus.SKIPPED);
            }
        }
        return new CreatedTaskFolderResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public DeleteResult deleteTaskFolder(@NonNull User user, @NonNull Task task) {
        if (task.getProject().isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                if (task.getDropboxTaskFolderId() == null) {
                    LOGGER.debug("Task %s does not have a Dropbox folder reference.".formatted(task.getName()));
                    return new DeleteResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
                }
                delete(dbxClient, user, task.getDropboxTaskFolderId());
                return new DeleteResult(ThirdPartyOperationStatus.SUCCESS);
            } catch (DeleteErrorException e) {
                return new DeleteResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new DeleteResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, DeleteResult::new);
            }
        }
        return new DeleteResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public AddUserToProjectFolderResult addProjectMemberToSharedFolder(@NonNull User user, @NonNull User newMember,
            Project project) {
        if (project.isDropboxConnected()) {
            if (user.getId().equals(newMember.getId())) {
                return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
            }

            try {
                return addUserToDropboxFolder0(getDbxClient(user), user, project, newMember);
            } catch (OAuth2AuthorizedClientLoadingException e) { 
                return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.SKIPPED);      
            }
        }
        return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public RemoveUserFromProjectFolderResult removeMemberFromSharedFolder(@NonNull User user, @NonNull User memberToRemove,
            Project project) {
        if (project.isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                if (user.getId().equals(memberToRemove.getId())) {
                    try {
                        relinquishFolderMembership(dbxClient, user, project.getDropboxProjectSharedFolderId());
                        return new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.SUCCESS);
                    } catch (RelinquishFolderMembershipErrorException e) {
                        LOGGER.debug("Failed to relinquish membership of the shared dropbox folder.", e);
                        return new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
                    }
                }
                try {
                    final OAuth2AuthorizedClient authorizedClientForUserToRemove =
                            oauthService.loadAuthorizedClient(memberToRemove, clientRegistration);

                    removeFolderMember(dbxClient, user, project.getDropboxProjectSharedFolderId(),
                            authorizedClientForUserToRemove.getExternalAccountId());
                    return new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.SUCCESS);
                } catch (RemoveFolderMemberErrorException e) {
                    LOGGER.debug("Failed to remove user from the shared dropbox folder.", e);
                    return new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
                }
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, RemoveUserFromProjectFolderResult::new);
            }
        }
        return new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public TransferOwnershipResult transferOwnership(@NonNull User user, @NonNull User newOwner,
            Project project) {
        if (project.isDropboxConnected()) {
            if (user.getId().equals(newOwner.getId())) {
                return new TransferOwnershipResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
            }
    
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);
                final OAuth2AuthorizedClient authorizedClientForNewOwner =
                        oauthService.loadAuthorizedClient(newOwner, clientRegistration);        

                transferFolderOwnership(dbxClient, user, project.getDropboxProjectSharedFolderId(),
                        authorizedClientForNewOwner.getExternalAccountId());
            } catch (TransferFolderErrorException e) {
                return new TransferOwnershipResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new TransferOwnershipResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, TransferOwnershipResult::new);
            }
        }
        return new TransferOwnershipResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public ProjectConnectedToDropboxResult connectProjectToDropbox(@NonNull User user,
            @NonNull Project project) {
        if (project.isDropboxConnected()) {
            return new ProjectConnectedToDropboxResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
        }
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            final CreatedProjectFolderResult projectFolderResult =
                    createSharedProjectFolder0(dbxClient, user, project);
            final Map<Long, CreatedTaskFolderResult> taskFolderResults = new HashMap<>();

            // Only set for convenience in this service. Will not be persisted later.
            project.setDropboxProjectFolderId(projectFolderResult.getProjectFolderId());
            project.setDropboxProjectSharedFolderId(projectFolderResult.getProjectSharedFolderId());
            project.getTasks().forEach(t -> taskFolderResults.put(t.getId(), createTaskFolder0(dbxClient, user, t)));
            final Map<Long, AddUserToProjectFolderResult> userConnectionResults = addUsersToDropboxFolder0(dbxClient, user, project,
                    project.getProjectRoles().stream()
                    .filter(pr -> !pr.getRoleType().equals(ProjectRoleType.CREATOR))
                    .map(pr -> pr.getUser())
                    .toList());
            userConnectionResults.put(user.getId(), new AddUserToProjectFolderResult(
                    ThirdPartyOperationStatus.SUCCESS, user.getId()));
            return new ProjectConnectedToDropboxResult(ThirdPartyOperationStatus.SUCCESS,
                    projectFolderResult, taskFolderResults, userConnectionResults);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new ProjectConnectedToDropboxResult(ThirdPartyOperationStatus.SKIPPED);
        }
    }

    @NonNull
    @Override
    public AddUserToProjectFolderResult joinDropbox(@NonNull User user, @NonNull Project project) {
        if (project.isDropboxConnected()) {
            final ProjectRole projectRole = entityUtil.getProjectRoleByProjectIdAndUserId(project.getId(), user.getId());

            if (projectRole.isDropboxConnected()) {
                return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
            }
            final User projectOwner = entityUtil.getProjectOwner(project);
            try {
                final DbxClientV2 dbxClient = getDbxClient(projectOwner);
    
                return addUserToDropboxFolder0(dbxClient, projectOwner, project, user);                
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.SKIPPED);
            }
        }
        return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public FileOperationResult uploadFileInTaskFolder(@NonNull User user, @NonNull Task task,
            MultipartFile file) {
        if (task.getDropboxTaskFolderId() == null) {
            return new FileOperationResult(ThirdPartyOperationStatus.FAILED,
                    FileOperationErrorTag.NO_TASK_FOLDER_ID, 
                    "Cannot upload the file because the task does not have a folder on Dropbox.");
        }
        if (task.getProject().isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                final Metadata taskFolderMeta = getMetadata(dbxClient, user, task.getDropboxTaskFolderId());
                final FileMetadata fileMetadata = uploadFile(dbxClient, user, taskFolderMeta, file);
                return new FileOperationResult(ThirdPartyOperationStatus.SUCCESS, fileMetadata);
            } catch (UploadErrorException e) {
                return new FileOperationResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
            } catch (GetMetadataErrorException e) {
                return new FileOperationResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
            } catch (IOException e) {
                return new FileOperationResult(ThirdPartyOperationStatus.FAILED,
                        FileOperationResult.FileOperationErrorTag.IOEXCEPTION,
                    "An IO issue has prevented the file from being uploaded.");
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new FileOperationResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, FileOperationResult::new);
            }
        }
        return new FileOperationResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    @NonNull
    @Override
    public FileOperationResult downloadFile(@NonNull User user, @NonNull String dropboxId,
            @NonNull OutputStream os) {
                
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            return new FileOperationResult(ThirdPartyOperationStatus.SUCCESS,
                    downloadFile(dbxClient, user, dropboxId, os));
        } catch (DownloadErrorException e) {
            return new FileOperationResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
        } catch (IOException e) {
            return new FileOperationResult(ThirdPartyOperationStatus.FAILED,
                    FileOperationResult.FileOperationErrorTag.IOEXCEPTION,
                    "An IO issue has prevented the file from being downloaded.");
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new FileOperationResult(ThirdPartyOperationStatus.SKIPPED);
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, FileOperationResult::new);
        }
    }

    @NonNull
    @Override
    public DeleteResult deleteFile(User user, String dropboxId) {
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            delete(dbxClient, user, dropboxId);
            return new DeleteResult(ThirdPartyOperationStatus.SUCCESS);
        } catch (DeleteErrorException e) {
            return new DeleteResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new DeleteResult(ThirdPartyOperationStatus.SKIPPED);
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, DeleteResult::new);
        }
    }

    @NonNull
    @Override
    public DropboxOperationResult testDropboxUserConnection(@NonNull User user) {
        try {
            final String query = UUID.randomUUID().toString();
            final DbxClientV2 dbxClient = getDbxClient(user);

            LOGGER.debug("Testing Dropbox with a query: %s.".formatted(query));
            final String result = echo(dbxClient, user, query);

            if (result.equals(query)) {
                return new DropboxOperationResult(ThirdPartyOperationStatus.SUCCESS);
            }
            LOGGER.warn("Somehow, a Dropbox echo did not return the same query. Sent: %s; Received: %s;"
                    .formatted(query, result));
            return new DropboxOperationResult(ThirdPartyOperationStatus.PARTIAL_SUCCESS);
        } catch (DbxApiException e) {
            return new DropboxOperationResult(ThirdPartyOperationStatus.FAILED);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new DropboxOperationResult(ThirdPartyOperationStatus.SKIPPED);
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, DropboxOperationResult::new);
        }
    }
    
    @NonNull
    @Override
    public DropboxOperationResult logout(@NonNull User user) {
        try {
            final OAuth2AuthorizedClient authorizedClient =
                    oauthService.loadAuthorizedClient(user, clientRegistration);
            final DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig,
                    authorizedClient.getToken());

            logout(dbxClient, user);
            oauthService.deleteAuthorizedClient(authorizedClient.getId());
            return new DropboxOperationResult(ThirdPartyOperationStatus.SUCCESS);
        } catch (DbxApiException e) {
            return new DropboxOperationResult(ThirdPartyOperationStatus.FAILED); //TODO: make special tags
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new DropboxOperationResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, DropboxOperationResult::new);
        }
    }

    @NonNull
    @Override
    public AccountResult getUserAccount(@NonNull User user) {
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            return new AccountResult(ThirdPartyOperationStatus.SUCCESS, getAccount(dbxClient, user));
        } catch (DbxApiException e) {
            return new AccountResult(ThirdPartyOperationStatus.FAILED, e.getMessage());
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new AccountResult(ThirdPartyOperationStatus.SKIPPED);
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, AccountResult::new);
        }
    }

    private DbxClientV2 getDbxClient(User user) throws OAuth2AuthorizedClientLoadingException {
        final OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(
                user, clientRegistration);
        LOGGER.debug("Creating DbxClient object for dropbox access.");
        return new DbxClientV2(dbxRequestConfig, authorizedClient.getToken());
    }

    private CreatedProjectFolderResult createSharedProjectFolder0(DbxClientV2 dbxClient, User user,
            Project project) {
        try {
            final String folderPath = dropboxRootPath + "/" + removeDangerousChars(project.getName());
            final FolderMetadata folderMeta;
            try {
                folderMeta = createOrGetFolder(dbxClient, user, folderPath);
            } catch (CreateFolderErrorException e) {
                return new CreatedProjectFolderResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
            }
            try {
                final ShareFolderLaunch sharedFolderMeta = shareFolder(dbxClient, user, folderMeta.getId());

                return new CreatedProjectFolderResult(ThirdPartyOperationStatus.SUCCESS,
                        folderMeta.getId(),
                        sharedFolderMeta.getCompleteValue().getSharedFolderId());
            } catch (ShareFolderErrorException e) {
                return new CreatedProjectFolderResult(ThirdPartyOperationStatus.PARTIAL_SUCCESS); //TODO: parse tags
            }
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, CreatedProjectFolderResult::new);
        }
    }

    private CreatedTaskFolderResult createTaskFolder0(DbxClientV2 dbxClient, User user,
            Task task) {
        try {
            final Metadata projectFolderMeta = getMetadata(dbxClient, user,
                    task.getProject().getDropboxProjectFolderId());
            final FolderMetadata taskFolderMeta = createOrGetFolder(dbxClient, user,
                    projectFolderMeta.getPathLower() + "/" + removeDangerousChars(task.getName()));
            return new CreatedTaskFolderResult(ThirdPartyOperationStatus.SUCCESS,
                    taskFolderMeta.getId());
        } catch (GetMetadataErrorException | CreateFolderErrorException e) {
            return new CreatedTaskFolderResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, CreatedTaskFolderResult::new);
        }
    }

    private FolderMetadata createOrGetFolder(DbxClientV2 dbxClient, User user, String path)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   CreateFolderErrorException {
        try {
            return createFolder(dbxClient, user, path, false).getMetadata();
        } catch (CreateFolderErrorException e1) {
            try {
                final Metadata unknownMeta = getMetadata(dbxClient, user, path);
                
                if (unknownMeta instanceof FolderMetadata castMetadata) {
                    return castMetadata;
                } else {
                    LOGGER.warn("Instead of a folder, found something else.");
                }
            } catch (GetMetadataErrorException e2) {
                LOGGER.error("Failed to find a folder on path " + path + ".");
            }
        }
        LOGGER.info("Attempting to create a folder with autorename...");
        return createFolder(dbxClient, user, path, true).getMetadata();
    }

    private Map<Long, AddUserToProjectFolderResult> addUsersToDropboxFolder0(DbxClientV2 dbxClient, User caller,
            Project project, List<User> users) {
        final Map<Long, AddUserToProjectFolderResult> results = new HashMap<>();

        users.forEach(u -> results.put(u.getId(), addUserToDropboxFolder0(dbxClient, caller, project, u)));
        return results;
    }

    private AddUserToProjectFolderResult addUserToDropboxFolder0(DbxClientV2 dbxClient, User caller,
            Project project, User target) {
        try {
            final OAuth2AuthorizedClient authorizedClientForTarget =
                    oauthService.loadAuthorizedClient(target, clientRegistration);
            final DbxClientV2 dbxClientNewUser = new DbxClientV2(dbxRequestConfig,
                    authorizedClientForTarget.getToken());
            
            addFolderMember(dbxClient, caller, project.getDropboxProjectSharedFolderId(),
                    List.of(authorizedClientForTarget.getExternalAccountId()));
            mountFolder(dbxClientNewUser, target, project.getDropboxProjectSharedFolderId());
            return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.SUCCESS, target.getId());
            
        } catch (AddFolderMemberErrorException e) {
            return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.FAILED); //TODO: parse tags
        } catch (MountFolderErrorException e) {
            return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.PARTIAL_SUCCESS); //TODO: parse tags
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new AddUserToProjectFolderResult(ThirdPartyOperationStatus.SKIPPED);
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, AddUserToProjectFolderResult::new);
        }
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

    private CreateFolderResult createFolder(DbxClientV2 dbxClient, User caller, String path,
            boolean isAutorenameAllowed)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   CreateFolderErrorException {
        int attemptCounter = 0;
        while (true) { 
            try {
                return dbxClient.files().createFolderV2(path, isAutorenameAllowed);
            } catch (CreateFolderErrorException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private Metadata getMetadata(DbxClientV2 dbxClient, User caller, String pathOrId)
            throws ThirdPartyInconsistentTokenException, ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException, ThirdPartyOutOfRetriesException,
                   GetMetadataErrorException {
        int attemptCounter = 0;
        while (true) { 
            try {
                return dbxClient.files().getMetadata(pathOrId);
            } catch (GetMetadataErrorException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private ShareFolderLaunch shareFolder(DbxClientV2 dbxClient, User caller, String folderId)
            throws ThirdPartyInconsistentTokenException, ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException, ThirdPartyOutOfRetriesException,
                   ShareFolderErrorException {
        int attemptCounter = 0;
        while (true) { 
            try {
                return dbxClient.sharing()
                        .shareFolderBuilder(folderId)
                        .withAclUpdatePolicy(AclUpdatePolicy.EDITORS)
                        .start();
            } catch (ShareFolderErrorException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private void delete(DbxClientV2 dbxClient, User caller, String itemId)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   DeleteErrorException {
        int attemptCounter = 0;
        while (true) {
            try {
                dbxClient.files().deleteV2(itemId);
                return;
            } catch (DeleteErrorException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private void relinquishFolderMembership(DbxClientV2 dbxClient, User caller, String sharedFolderId)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException, 
                   RelinquishFolderMembershipErrorException {
        int attemptCounter = 0;
        while (true) {
            try {
                dbxClient.sharing().relinquishFolderMembership(sharedFolderId);
                return;
            } catch (RelinquishFolderMembershipErrorException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private void removeFolderMember(DbxClientV2 dbxClient, User caller,
            String sharedFolderId, String targetDropboxId)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   RemoveFolderMemberErrorException {
        int attemptCounter = 0;
        while (true) {
            try {
                dbxClient.sharing().removeFolderMember(sharedFolderId,
                        MemberSelector.dropboxId(targetDropboxId), false);
                return;
            } catch (RemoveFolderMemberErrorException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private void transferFolderOwnership(DbxClientV2 dbxClient, User caller,
            String sharedFolderId, String targetDropboxId)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   TransferFolderErrorException {
        int attemptCounter = 0;
        while (true) {
            try {
                dbxClient.sharing().transferFolder(sharedFolderId, targetDropboxId);
                return;
            } catch (TransferFolderErrorException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private void addFolderMember(DbxClientV2 dbxClient, User caller,
            String sharedFolderId, List<String> dropboxUserIds)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   AddFolderMemberErrorException {
        final List<AddMember> addMembers = dropboxUserIds.stream()
                .map(id -> new AddMember(MemberSelector.dropboxId(id), AccessLevel.EDITOR))
                .toList();

        int attemptCounter = 0;
        while (true) {
            try {
                dbxClient.sharing().addFolderMemberBuilder(sharedFolderId, addMembers)
                        .withQuiet(true).start();
                return;
            } catch (AddFolderMemberErrorException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private void mountFolder(DbxClientV2 dbxClient, User caller,
            String sharedFolderId)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   MountFolderErrorException {
        int attemptCounter = 0;
        while (true) {
            try {
                dbxClient.sharing().mountFolder(sharedFolderId);
                return;
            } catch (MountFolderErrorException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private FileMetadata uploadFile(DbxClientV2 dbxClient, User caller,
            Metadata taskFolderMeta, MultipartFile file)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   UploadErrorException,
                   IOException {
        int attemptCounter = 0;
        while (true) {
            try {
                return dbxClient.files().uploadBuilder(taskFolderMeta.getPathLower()
                        + "/" + removeDangerousChars(file.getOriginalFilename()))
                        .withAutorename(true)
                        .uploadAndFinish(file.getInputStream());
            } catch (UploadErrorException | IOException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private FileMetadata downloadFile(DbxClientV2 dbxClient, User caller,
            String dropboxFileId, OutputStream os)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   DownloadErrorException,
                   IOException {
        int attemptCounter = 0;
        while (true) {
            try {
                return dbxClient.files().download(dropboxFileId).download(os);
            } catch (DownloadErrorException | IOException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private String echo(DbxClientV2 dbxClient, User caller,
            String value) throws ThirdPartyInconsistentTokenException,
                                 ThirdPartyUnknownException,
                                 ThirdPartyRetryTooLongException,
                                 ThirdPartyOutOfRetriesException,
                                 DbxApiException {
        int attemptCounter = 0;
        while (true) {
            try {
                return dbxClient.check().user(value).getResult();
            } catch (DbxApiException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private void logout(DbxClientV2 dbxClient, User caller)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   DbxApiException {
        int attemptCounter = 0;
        while (true) {
            try {
                dbxClient.auth().tokenRevoke();
                return;
            } catch (DbxApiException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }
    
    private FullAccount getAccount(DbxClientV2 dbxClient, User caller)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   DbxApiException {
        int attemptCounter = 0;
        while (true) {
            try {
                return dbxClient.users().getCurrentAccount();
            } catch (DbxApiException e) {
                throw e;
            } catch (DbxException e) {
                handleDropboxException(e, caller, attemptCounter);
                ++attemptCounter;
            }
        }
    }

    private void handleDropboxException(DbxException exception, User caller, int currentAttemptCounter)
            throws ThirdPartyInconsistentTokenException, ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException, ThirdPartyOutOfRetriesException {
        switch (exception) {
            case RetryException retryException -> {
                if (currentAttemptCounter < MAX_RETRY_ATTEMPTS) {
                    final long sleepMillis = retryException.getBackoffMillis();

                    if (sleepMillis > MAX_RETRY_DURATION) {
                        throw new ThirdPartyRetryTooLongException("Requested retry time is " + sleepMillis
                                + " milliseconds which is higher than the upper limit of "
                                + MAX_RETRY_DURATION + " milliseconds for Dropbox service.");
                    }
                    LOGGER.warn("Dropbox has thrown a RetryException. The thread will sleep for "
                            + sleepMillis + " milliseconds and then retry.");
                    if (retryException.getBackoffMillis() > 0L) {
                        try {
                            Thread.sleep(sleepMillis);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new ThirdPartyApiException("The thread was interrupted "
                                    + "during waiting for a new retry attempt.",
                                    ErrorType.EXTERNAL_INTERRUPTED, e);
                        }
                    }
                    return;
                }
                throw new ThirdPartyOutOfRetriesException("Dropbox service has exceeded the max number "
                        + "of retry attempts, which is currently " + MAX_RETRY_ATTEMPTS + ".");
            }
            case InvalidAccessTokenException tokenException -> {
                LOGGER.warn("Access to Dropbox failed, because the token is invalid. "
                        + "User %s might have revoked access to the app. Their authorized client will be invalidated."
                        .formatted(caller.getUsername()), tokenException);
                try {
                    final OAuth2AuthorizedClient client = oauthService.getAuthorizedClientForUser(caller, clientRegistration);
                    oauthService.deleteAuthorizedClient(client.getId());
                    throw new ThirdPartyInconsistentTokenException("Stored token was invalid. "
                            + "Authorized client for user " + caller.getUsername()
                            + " has been deleted.");
                } catch (EntityNotFoundException e) {
                    throw new ThirdPartyInconsistentTokenException("Stored token was invalid. " 
                            + "No authorized client for user " + caller.getUsername()
                            + " found, so nothing will be deleted.");
                }
            }
            default -> {
                throw new ThirdPartyUnknownException("An unusual exception was thrown by Dropbox.", exception);
            }
        }
    }

    private <T extends DropboxOperationResult> T handleThirdPartyException(
            ThirdPartyExpectedException exception, Function<ThirdPartyOperationStatus, T> resultFactory) {
        switch (exception) {
            case ThirdPartyInconsistentTokenException tokenException -> {
                LOGGER.debug("Stored Dropbox token is not up to date.", tokenException);
                return resultFactory.apply(ThirdPartyOperationStatus.TOKEN_REJECTED);
            }
            case ThirdPartyOutOfRetriesException outOfRetriesException -> {
                LOGGER.debug("Too many retries failed for a Dropbox request.", outOfRetriesException);
                return resultFactory.apply(ThirdPartyOperationStatus.RAN_OUT_OF_RETRIES);
            }
            case ThirdPartyRetryTooLongException retryTooLongException -> {
                LOGGER.debug("Dropbox is requesting a delay that is longer than the max value allowed.", retryTooLongException);
                return resultFactory.apply(ThirdPartyOperationStatus.RETRY_TOO_LONG);
            }
            default -> {
                LOGGER.debug("Stored Dropbox token is not up to date.", exception);
                return resultFactory.apply(ThirdPartyOperationStatus.FAILED);
            }
        }
    }
}
