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
import com.dropbox.core.v2.files.CreateFolderError;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.DeleteError;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadError;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataError;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.LookupError;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadError;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteError;
import com.dropbox.core.v2.sharing.AccessLevel;
import com.dropbox.core.v2.sharing.AclUpdatePolicy;
import com.dropbox.core.v2.sharing.AddFolderMemberError;
import com.dropbox.core.v2.sharing.AddFolderMemberErrorException;
import com.dropbox.core.v2.sharing.AddMember;
import com.dropbox.core.v2.sharing.MemberSelector;
import com.dropbox.core.v2.sharing.MountFolderError;
import com.dropbox.core.v2.sharing.MountFolderErrorException;
import com.dropbox.core.v2.sharing.RelinquishFolderMembershipError;
import com.dropbox.core.v2.sharing.RelinquishFolderMembershipErrorException;
import com.dropbox.core.v2.sharing.RemoveFolderMemberError;
import com.dropbox.core.v2.sharing.RemoveFolderMemberErrorException;
import com.dropbox.core.v2.sharing.ShareFolderError;
import com.dropbox.core.v2.sharing.ShareFolderErrorException;
import com.dropbox.core.v2.sharing.ShareFolderLaunch;
import com.dropbox.core.v2.sharing.SharedFolderAccessError;
import com.dropbox.core.v2.sharing.SharedFolderMemberError;
import com.dropbox.core.v2.sharing.TransferFolderError;
import com.dropbox.core.v2.sharing.TransferFolderErrorException;
import com.dropbox.core.v2.users.FullAccount;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult.ThirdPartyOperationStatus;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AccountResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.AddUserToProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedProjectFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.CreatedTaskFolderResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DeleteResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.DropboxOperationResult.DropboxErrorTag;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.FileOperationResult;
import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox.FileUploadOperationResult;
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
import com.unbidden.jvtaskmanagementsystem.util.DisableLogging;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * This service is responsible for Dropbox interraction. The methods here never throw exceptions, 
 * but rather return results that contain all the necessary information about the response 
 * whether that was a success or an error. The general outcome can be indentified using the 
 * {@link ThirdPartyOperationStatus} field.
 * Possible values:
 * <ls>
 *  <li>{@code SUCCESS} — the operation was fully successfull. In this case, 
 * the error message and error tag are always {@code null}</li>
 * 
 *  <li>{@code PARTIAL_SUCCESS} — only a part of the operation was successfull. This usually 
 * does not require any specific action, and should often be considered a failure. 
 * There might be a {@link DropboxErrorTag} attached, but it's not guaranteed.</li>
 * 
 *  <li>{@code SKIPPED} — the operation was skipped due to the caller or a target, if 
 * applicable, not having an available {@link OAuth2AuthorizedClient}.</li>
 * 
 *  <li>{@code TOKEN_REJECTED} — the operation failed, because of an inconsistency between the 
 * locally stored token state and the actual Dropbox token state. This might happen 
 * if the user revoked access to the platform from their Dropbox account.</li>
 * 
 *  <li>{@code RAN_OUT_OF_RETRIES} — the operation failed, because the client ran out of retry 
 * attemts after being rate limited several times.</li>
 * 
 *  <li>{@code RETRY_TOO_LONG} — the operation failed, because the server 
 * had requested a retry delay that was too long.</li>
 * 
 *  <li>{@code NOT_APPLICABLE} — the operation was skipped due to it being impossible. 
 * This might happen when, for example, the project is not connected to Dropbox, 
 * but the user tries to upload a file.</li>
 * 
 *  <li>{@code FAILED} — the operation completely failed. The detailed reason will always be 
 * specified using a special {@link DropboxErrorTag}. It will also be accompanied by a text 
 * message which can be shown to the user in case no better option is available.</li>
 * </ls>
 */
@Service
@RequiredArgsConstructor
public class DropboxServiceImpl implements DropboxService {
    private static final Logger LOGGER = LogManager.getLogger(DropboxServiceImpl.class);

    private static final int MAX_RETRY_ATTEMPTS = 1;
    private static final int MAX_RETRY_DURATION = 5000;

    private final OAuth2Service oauthService;

    private final DbxRequestConfig dbxRequestConfig;

    private final EntityUtil entityUtil;
    
    @Value("${dropbox.root.path}")
    private String dropboxRootPath;

    private ClientRegistration clientRegistration;

    @PostConstruct
    protected void init() {
        this.clientRegistration = entityUtil.getClientRegistrationByName("dropbox");
    }

    /**
     * Creates a new project folder, or reuses an already existing one. Can return 
     * {@code ThirdPartyOperationStatus.PARTIAL_SUCCESS} in case the folder was 
     * created (or already present), but sharing failed.
     * Possible error tags:
     * <ls>
     *  <li>Write group</li>
     *  <li>Share folder group</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
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

    /**
     * Deletes the project folder.
     * Possible error tags:
     * <ls>
     *  <li>Write group</li>
     *  <li>Lookup group</li>
     *  <li>{@code FILES_TOO_MANY_WRITE_OPERATIONS}</li>
     *  <li>{@code FILES_TOO_MANY_FILES}</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
    @NonNull
    @Override
    public DeleteResult deleteProjectFolder(@NonNull User user, @NonNull Project project) {
        if (project.isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);

                delete(dbxClient, user, dropboxRootPath);
                return new DeleteResult(ThirdPartyOperationStatus.SUCCESS);
            } catch (DeleteErrorException e) {
                return processDeleteError(e.errorValue);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new DeleteResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, DeleteResult::new);
            }
        }
        return new DeleteResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    /**
     * Creates a new task folder, or reuses an already existing one.
     * Possible error tags:
     * <ls>
     *  <li>Write group</li>
     *  <li>Lookup group</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
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

    /**
     * Deletes this task's folder.
     * Possible error tags:
     * <ls>
     *  <li>Write group</li>
     *  <li>Lookup group</li>
     *  <li>{@code FILES_TOO_MANY_WRITE_OPERATIONS}</li>
     *  <li>{@code FILES_TOO_MANY_FILES}</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
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
                return processDeleteError(e.errorValue);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new DeleteResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, DeleteResult::new);
            }
        }
        return new DeleteResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    /**
     * Adds a new user to the shared project folder. The user will have the EDITOR 
     * access level, which means they might have more authority outside of the platform.
     * This is because the VIEWER access level does not allow file upload. This method 
     * also automatically mounts the folder after the user is invited. In order to do this, 
     * the {@link OAuth2AuthorizedClient} of the target is used. In case mounting fails, 
     * {@code ThirdPartyOperationStatus.PARTIAL_SUCCESS} tag will be returned.
     * Possible error tags:
     * <ls>
     *  <li>Membership add folder member group</li>
     *  <li>Membership mount folder group</li>
     *  <li>Shared folder access group</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
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

    /**
     * Removes the target from the shared project folder. In case the target is the 
     * same user as the caller, will relinquish membership instead.
     * Possible error tags:
     * <ls>
     *  <li>Shared folder access group</li>
     *  <li>Shared folder member group</li>
     *  <li>Membership remove user group</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
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
                        return processRelinquishOwnershipError(e.errorValue);
                    }
                }
                try {
                    final OAuth2AuthorizedClient authorizedClientForUserToRemove =
                            oauthService.loadAuthorizedClient(memberToRemove, clientRegistration);

                    removeFolderMember(dbxClient, user, project.getDropboxProjectSharedFolderId(),
                            authorizedClientForUserToRemove.getExternalAccountId());
                    return new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.SUCCESS);
                } catch (RemoveFolderMemberErrorException e) {
                    return processRemoveFolderMemberError(e.errorValue);
                }
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, RemoveUserFromProjectFolderResult::new);
            }
        }
        return new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    /**
     * Transfers ownership of this folder to the target user.
     * Possible error tags:
     * <ls>
     *  <li>Shared folder access group</li>
     *  <li>Membership transfer ownership group</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
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
                return new TransferOwnershipResult(ThirdPartyOperationStatus.SUCCESS);
            } catch (TransferFolderErrorException e) {
                return processTransferFolderError(e.errorValue);
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new TransferOwnershipResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, TransferOwnershipResult::new);
            }
        }
        return new TransferOwnershipResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    /**
     * Connects an already existing project to Dropbox by creating its shared folder and 
     * adding all current users that have a valid {@link OAuth2AuthorizedClient}. Might return 
     * {@code ThirdPartyOperationStatus.PARTIAL_SUCCESS} if not all of the users were connected. This method 
     * is potentially dangerous, since it might force the server to rate limit if the 
     * project has too many members. It also takes forever to finish, so it'll likely be further revamped later.
     * Possible error tags:
     * <ls>
     *  <li>Write group</li>
     *  <li>Share folder group</li>
     * </ls>
     * Add user tags should be checked individually for each entry.
     * All tags are available here: {@link DropboxErrorTag}.
     */
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

            if (!projectFolderResult.getStatus().equals(ThirdPartyOperationStatus.SUCCESS)) {
                return new ProjectConnectedToDropboxResult(projectFolderResult.getStatus(),
                        projectFolderResult);
            }

            // Only set for convenience in this service. Will not be persisted later.
            project.setDropboxProjectFolderId(projectFolderResult.getProjectFolderId());
            project.setDropboxProjectSharedFolderId(projectFolderResult.getProjectSharedFolderId());

            final Map<Long, AddUserToProjectFolderResult> userConnectionResults = addUsersToDropboxFolder0(dbxClient, user, project,
                    project.getProjectRoles().stream()
                    .filter(pr -> !pr.getRoleType().equals(ProjectRoleType.CREATOR))
                    .map(pr -> pr.getUser())
                    .toList());
            userConnectionResults.put(user.getId(), new AddUserToProjectFolderResult(
                    ThirdPartyOperationStatus.SUCCESS, user.getId()));

            ThirdPartyOperationStatus finalStatus = ThirdPartyOperationStatus.SUCCESS;
            for (final var entry : userConnectionResults.entrySet()) {
                if (!entry.getValue().getStatus().equals(ThirdPartyOperationStatus.SUCCESS)) {
                    finalStatus = ThirdPartyOperationStatus.PARTIAL_SUCCESS;
                    break;
                }
            }
            return new ProjectConnectedToDropboxResult(finalStatus,
                    projectFolderResult, userConnectionResults);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new ProjectConnectedToDropboxResult(ThirdPartyOperationStatus.SKIPPED);
        }
    }

    /**
     * Allows the caller to join the project. Might return {@code ThirdPartyOperationStatus.PARTIAL_SUCCESS} if mouting fails.
     * Possible error tags:
     * <ls>
     *  <ls>
     *  <li>Membership add folder member group</li>
     *  <li>Membership mount folder group</li>
     *  <li>Shared folder access group</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
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

    /**
     * Uploads a new file. If there's no task folder, it will be created.
     * Possible error tags:
     * <ls>
     *  <ls>
     *  <li>Write group</li>
     *  <li>Lookup group</li>
     *  <li>{@code FILES_UPLOAD_PROPERTIES}</li>
        <li>{@code FILES_UPLOAD_PAYLOAD_TOO_LARGE}</li>
        <li>{@code FILES_UPLOAD_CONTENT_HASH_MISMATCH}</li>
     *  <li>{@code FILES_IOEXCEPTION}</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
    @NonNull
    @Override
    @SuppressWarnings("null")
    public FileUploadOperationResult uploadFileInTaskFolder(@NonNull User user, @NonNull Task task,
            MultipartFile file) {
        if (task.getProject().isDropboxConnected()) {
            try {
                final DbxClientV2 dbxClient = getDbxClient(user);
                final CreatedTaskFolderResult folderResult = task.getDropboxTaskFolderId() == null
                        ? createTaskFolder0(dbxClient, user, task) : null;

                if (folderResult != null && !folderResult.getStatus().equals(ThirdPartyOperationStatus.SUCCESS)) {
                    return new FileUploadOperationResult(folderResult.getStatus(), folderResult);
                }
                final FileMetadata fileMetadata = uploadFile(dbxClient, user,
                        task.getDropboxTaskFolderId() != null
                        ? task.getDropboxTaskFolderId() 
                        : folderResult.getTaskFolderId(), file);
                return new FileUploadOperationResult(ThirdPartyOperationStatus.SUCCESS, fileMetadata, folderResult);
            } catch (UploadErrorException e) {
                return processUploadError(e.errorValue);
            } catch (IOException e) {
                return new FileUploadOperationResult(ThirdPartyOperationStatus.FAILED,
                        DropboxErrorTag.FILES_IOEXCEPTION,
                        "An IO issue has prevented the file from being uploaded.");
            } catch (OAuth2AuthorizedClientLoadingException e) {
                return new FileUploadOperationResult(ThirdPartyOperationStatus.SKIPPED);
            } catch (ThirdPartyExpectedException e) {
                return handleThirdPartyException(e, FileUploadOperationResult::new);
            }
        }
        return new FileUploadOperationResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
    }

    /**
     * Downloads a file.
     * Possible error tags:
     * <ls>
     *  <ls>
     *  <li>Lookup group</li>
     *  <li>{@code FILES_DOWNLOAD_UNSUPPORTED}</li>
     *  <li>{@code FILES_IOEXCEPTION}</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
    @NonNull
    @Override
    public FileOperationResult downloadFile(@NonNull User user, @NonNull String dropboxId,
            @NonNull OutputStream os) {
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            return new FileOperationResult(ThirdPartyOperationStatus.SUCCESS,
                    downloadFile(dbxClient, user, dropboxId, os));
        } catch (DownloadErrorException e) {
            return processDownloadError(e.errorValue);
        } catch (IOException e) {
            return new FileOperationResult(ThirdPartyOperationStatus.FAILED,
                    DropboxErrorTag.FILES_IOEXCEPTION,
                    "An IO issue has prevented the file from being downloaded.");
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new FileOperationResult(ThirdPartyOperationStatus.SKIPPED);
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, FileOperationResult::new);
        }
    }

    /**
     * Deletes a file.
     * Possible error tags:
     * <ls>
     *  <li>Write group</li>
     *  <li>Lookup group</li>
     *  <li>{@code FILES_TOO_MANY_WRITE_OPERATIONS}</li>
     *  <li>{@code FILES_TOO_MANY_FILES}</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
    @NonNull
    @Override
    public DeleteResult deleteFile(User user, String dropboxId) {
        try {
            final DbxClientV2 dbxClient = getDbxClient(user);

            delete(dbxClient, user, dropboxId);
            return new DeleteResult(ThirdPartyOperationStatus.SUCCESS);
        } catch (DeleteErrorException e) {
            return processDeleteError(e.errorValue);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new DeleteResult(ThirdPartyOperationStatus.SKIPPED);
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, DeleteResult::new);
        }
    }

    /**
     * Tests Dropbox health. Might return {@code ThirdPartyOperationStatus.PARTIAL_SUCCESS} 
     * if the returned query does not equal to the sent one, which should never happen.
     * Possible error tags:
     * <ls>
     *  <li>{@code GENERAL_TEST}</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
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
            return new DropboxOperationResult(ThirdPartyOperationStatus.FAILED,
                    DropboxErrorTag.GENERAL_TEST, "An unknown error has occured while testing Dropbox.");
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new DropboxOperationResult(ThirdPartyOperationStatus.SKIPPED);
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, DropboxOperationResult::new);
        }
    }
    
    /**
     * Logs the user out of Dropbox and deletes their {@link OAuth2AuthorizedClient}.
     * Possible error tags:
     * <ls>
     *  <li>{@code GENERAL_LOGOUT}</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
    @NonNull
    @Override
    public DropboxOperationResult logout(@NonNull User user) {
        try {
            final OAuth2AuthorizedClient authorizedClient =
                    oauthService.loadAuthorizedClient(user, clientRegistration);
            final DropboxOperationResult result = logout(user, authorizedClient.getToken());

            oauthService.deleteAuthorizedClient(authorizedClient.getId());
            return result;
        } catch (OAuth2AuthorizedClientLoadingException e) {
            return new DropboxOperationResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
        }
    }
    
    /**
     * Logs the user out of Dropbox directly using their token.
     * Possible error tags:
     * <ls>
     *  <li>{@code GENERAL_LOGOUT}</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
    @NonNull
    @Override
    public DropboxOperationResult logout(@NonNull User user, @DisableLogging @NonNull String token) {
        try {
            final DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig, token);

            logout(dbxClient, user);
            return new DropboxOperationResult(ThirdPartyOperationStatus.SUCCESS);
        } catch (DbxApiException e) {
            return new DropboxOperationResult(ThirdPartyOperationStatus.FAILED,
                    DropboxErrorTag.GENERAL_LOGOUT, "An unknown error has occured during logout.");
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, DropboxOperationResult::new);
        }
    }

    /**
     * Loads the user account details.
     * Possible error tags:
     * <ls>
     *  <li>{@code GENERAL_ACCOUNT}</li>
     * </ls>
     * All tags are available here: {@link DropboxErrorTag}.
     */
    @NonNull
    @Override
    public AccountResult getUserAccount(@NonNull User user, @DisableLogging @NonNull String token) {
        try {
            final DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig, token);

            return new AccountResult(ThirdPartyOperationStatus.SUCCESS, getAccount(dbxClient, user));
        } catch (DbxApiException e) {
            return new AccountResult(ThirdPartyOperationStatus.FAILED,
                    DropboxErrorTag.GENERAL_ACCOUNT, "An unknown error has occured while "
                    + "attempting to fetch Dropbox account for user "
                    + user.getUsername());
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, AccountResult::new);
        }
    }

    private DbxClientV2 getDbxClient(User user) throws OAuth2AuthorizedClientLoadingException {
        final OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(
                user, clientRegistration);
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
                return processCreateFolderError(e.errorValue, CreatedProjectFolderResult::new);
            }
            try {
                final ShareFolderLaunch sharedFolderMeta = shareFolder(dbxClient, user, folderMeta.getId());

                return new CreatedProjectFolderResult(ThirdPartyOperationStatus.SUCCESS,
                        folderMeta.getId(),
                        sharedFolderMeta.getCompleteValue().getSharedFolderId());
            } catch (ShareFolderErrorException e) {
                final ShareFolderError error = e.errorValue;

                if (error.isBadPath() && error.getBadPathValue().isAlreadyShared()) {
                    LOGGER.info("A shared folder for project " + project.getName()
                            + " already exists and is shared. It will be used as is.");
                    return new CreatedProjectFolderResult(ThirdPartyOperationStatus.SUCCESS,
                        folderMeta.getId(),
                        error.getBadPathValue().getAlreadySharedValue().getSharedFolderId());
                }
                return processShareFolderError(e.errorValue);
            }
        } catch (ThirdPartyExpectedException e) {
            return handleThirdPartyException(e, CreatedProjectFolderResult::new);
        }
    }

    private CreatedTaskFolderResult createTaskFolder0(DbxClientV2 dbxClient, User user,
            Task task) {
        try {
            if (task.getDropboxTaskFolderId() != null) {
                return new CreatedTaskFolderResult(ThirdPartyOperationStatus.NOT_APPLICABLE);
            }
            final Metadata projectFolderMeta = getMetadata(dbxClient, user,
                    task.getProject().getDropboxProjectFolderId());
            final FolderMetadata taskFolderMeta = createOrGetFolder(dbxClient, user,
                    projectFolderMeta.getPathLower() + "/" + removeDangerousChars(task.getName()));
            return new CreatedTaskFolderResult(ThirdPartyOperationStatus.SUCCESS,
                    taskFolderMeta.getId());
        } catch (CreateFolderErrorException e) {
            return processCreateFolderError(e.errorValue, CreatedTaskFolderResult::new);
        } catch (GetMetadataErrorException e) {
            return processGetMetadataError(e.errorValue, CreatedTaskFolderResult::new);
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
            if (e1.errorValue.getPathValue().isConflict()) {
                try {
                    final Metadata unknownMeta = getMetadata(dbxClient, user, path);
                    
                    if (unknownMeta instanceof FolderMetadata castMetadata) {
                        return castMetadata;
                    } else {
                        LOGGER.debug("Instead of a folder, found something else.");
                    }
                } catch (GetMetadataErrorException e2) {
                    LOGGER.warn("Failed to find a folder on path " + path + ".");
                }
            }
        }
        LOGGER.debug("Attempting to create a folder with autorename...");
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
            return processAddFolderMemberError(e.errorValue);
        } catch (MountFolderErrorException e) {
            return processMountFolderError(e.errorValue);
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
        final String extension = StringUtils.getFilenameExtension(filename);

        String noExt = StringUtils.stripFilenameExtension(filename);
        for (Character ch : forbidden) {
            noExt = noExt.replace(ch, '_');
        }        

        return noExt + (extension != null ? '.' + extension : "");
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

    @SuppressWarnings("null")
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
            String taskFolderId, MultipartFile file)
            throws ThirdPartyInconsistentTokenException,
                   ThirdPartyUnknownException,
                   ThirdPartyRetryTooLongException,
                   ThirdPartyOutOfRetriesException,
                   UploadErrorException,
                   IOException {
        int attemptCounter = 0;
        while (true) {
            try {
                return dbxClient.files().uploadBuilder(taskFolderId
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

    private void processLookupErrorTags(LookupError error, DropboxOperationResult result) {
        switch (error.tag()) {
            case LookupError.Tag.MALFORMED_PATH -> {
                result.setTag(DropboxErrorTag.PATH_LOOKUP_MALFORMED_PATH);
                result.setErrorMessage("The supplied path is invalid.");
            }
            case LookupError.Tag.NOT_FOUND -> {
                result.setTag(DropboxErrorTag.PATH_LOOKUP_NOT_FOUND);
                result.setErrorMessage("There is nothing at the supplied path.");
            }
            case LookupError.Tag.NOT_FILE -> {
                result.setTag(DropboxErrorTag.PATH_LOOKUP_NOT_FILE);
                result.setErrorMessage("Was expecting a file, but found something else.");
            }
            case LookupError.Tag.NOT_FOLDER -> {
                result.setTag(DropboxErrorTag.PATH_LOOKUP_NOT_FOLDER);
                result.setErrorMessage("Was expecting a folder, but found something else.");
            }
            case LookupError.Tag.RESTRICTED_CONTENT -> {
                result.setTag(DropboxErrorTag.PATH_LOOKUP_RESTRICTED_CONTENT);
                result.setErrorMessage("The content is restricted. Might be due to the legal status of said content.");
            }
            case LookupError.Tag.UNSUPPORTED_CONTENT_TYPE -> {
                result.setTag(DropboxErrorTag.PATH_LOOKUP_UNSUPPORTED_CONTENT_TYPE);
                result.setErrorMessage("The requested operation is not supported for this content type.");
            }
            case LookupError.Tag.LOCKED -> {
                result.setTag(DropboxErrorTag.PATH_LOOKUP_LOCKED);
                result.setErrorMessage("The supplied path is locked.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
    }

    private void processWriteErrorTags(WriteError error, DropboxOperationResult result) {
        switch (error.tag()) {
            case WriteError.Tag.CONFLICT -> {
                result.setTag(DropboxErrorTag.PATH_WRITE_CONFLICT);
                result.setErrorMessage("Something is in the way of the write operation.");
            }
            case WriteError.Tag.MALFORMED_PATH -> {
                result.setTag(DropboxErrorTag.PATH_WRITE_MALFORMED_PATH);
                result.setErrorMessage("The supplied path is invalid.");
            }
            case WriteError.Tag.NO_WRITE_PERMISSION -> {
                result.setTag(DropboxErrorTag.PATH_WRITE_NO_PERMISSION);
                result.setErrorMessage("The user does not have permissions for this action.");
            }
            case WriteError.Tag.INSUFFICIENT_SPACE -> {
                result.setTag(DropboxErrorTag.PATH_WRITE_NOT_ENOUGH_SPACE);
                result.setErrorMessage("The user does not have enough space to perform this write operation.");
            }
            case WriteError.Tag.DISALLOWED_NAME -> {
                result.setTag(DropboxErrorTag.PATH_WRITE_DISALLOWED_NAME);
                result.setErrorMessage("The provided name is invalid.");
            }
            case WriteError.Tag.TEAM_FOLDER -> {
                result.setTag(DropboxErrorTag.PATH_WRITE_TEAM_FOLDER);
                result.setErrorMessage("Cannot perform this action on team folders.");
            }
            case WriteError.Tag.OPERATION_SUPPRESSED -> {
                result.setTag(DropboxErrorTag.PATH_WRITE_SUPPRESSED);
                result.setErrorMessage("This operation is not allowed on this path.");
            }
            case WriteError.Tag.TOO_MANY_WRITE_OPERATIONS -> {
                result.setTag(DropboxErrorTag.PATH_WRITE_TOO_MANY_OPERATIONS);
                result.setErrorMessage("The user is performing too many write operations.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
    }

    private void processSharedFolderAccessErrorTags(SharedFolderAccessError error, DropboxOperationResult result) {
        switch (error) {
            case SharedFolderAccessError.INVALID_ID -> {
                result.setTag(DropboxErrorTag.SHARED_FOLDER_ACCESS_INVALID_ID);
                result.setErrorMessage("The shared folder id is invalid.");
            }
            case SharedFolderAccessError.NOT_A_MEMBER -> {
                result.setTag(DropboxErrorTag.SHARED_FOLDER_ACCESS_NOT_A_MEMBER);
                result.setErrorMessage("The user cannot access this shared folder since they are not a member.");
            }
            case SharedFolderAccessError.INVALID_MEMBER -> {
                result.setTag(DropboxErrorTag.SHARED_FOLDER_ACCESS_INVALID_MEMBER);
                result.setErrorMessage("The user does not exist or their account is disabled.");
            }
            case SharedFolderAccessError.UNMOUNTED -> {
                result.setTag(DropboxErrorTag.SHARED_FOLDER_ACCESS_UNMOUNTED);
                result.setErrorMessage("The shared folder is unmounted.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
    }

    private void processSharedFolderMemberErrorTags(SharedFolderMemberError error, DropboxOperationResult result) {
        switch (error.tag()) {
            case SharedFolderMemberError.Tag.INVALID_DROPBOX_ID -> {
                result.setTag(DropboxErrorTag.SHARED_FOLDER_ACCESS_INVALID_ID);
                result.setErrorMessage("The target id is invalid.");
            }
            case SharedFolderMemberError.Tag.NOT_A_MEMBER -> {
                result.setTag(DropboxErrorTag.SHARED_FOLDER_ACCESS_NOT_A_MEMBER);
                result.setErrorMessage("The target user is not a member of the shared folder.");
            }
            case SharedFolderMemberError.Tag.NO_EXPLICIT_ACCESS -> {
                result.setTag(DropboxErrorTag.SHARED_FOLDER_ACCESS_INVALID_MEMBER);
                result.setErrorMessage("The target member only has inherited access to the shared folder.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
    }

    private DeleteResult processDeleteError(DeleteError error) {
        final DeleteResult result = new DeleteResult(ThirdPartyOperationStatus.FAILED);
                
        switch (error.tag()) {
            case DeleteError.Tag.TOO_MANY_WRITE_OPERATIONS -> {
                result.setTag(DropboxErrorTag.FILES_TOO_MANY_WRITE_OPERATIONS);
                result.setErrorMessage("There are too many write operations.");
            }
            case DeleteError.Tag.TOO_MANY_FILES -> {
                result.setTag(DropboxErrorTag.FILES_TOO_MANY_FILES);
                result.setErrorMessage("The request has too many files. Should not be possible.");
            }
            case DeleteError.Tag.PATH_LOOKUP -> {
                processLookupErrorTags(error.getPathLookupValue(), result);
            }
            case DeleteError.Tag.PATH_WRITE -> {
                processWriteErrorTags(error.getPathWriteValue(), result);
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }           
        return result;
    }

    private RemoveUserFromProjectFolderResult processRelinquishOwnershipError(RelinquishFolderMembershipError error) {
        final RemoveUserFromProjectFolderResult result = new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.FAILED);

        switch (error.tag()) {
            case RelinquishFolderMembershipError.Tag.ACCESS_ERROR -> {
                processSharedFolderAccessErrorTags(error.getAccessErrorValue(), result);
            }
            case RelinquishFolderMembershipError.Tag.FOLDER_OWNER -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_FOLDER_OWNER);
                result.setErrorMessage("Folder owner cannot relinquish membership.");
            }
            case RelinquishFolderMembershipError.Tag.MOUNTED -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_MOUNTED);
                result.setErrorMessage("Cannot relinquish a mounted folder.");
            }
            case RelinquishFolderMembershipError.Tag.GROUP_ACCESS -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_GROUP_ACCESS);
                result.setErrorMessage("The current user has access to the shared folder via a group. You can't relinquish membership to folders shared via groups.");
            }
            case RelinquishFolderMembershipError.Tag.TEAM_FOLDER -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_TEAM_FOLDER);
                result.setErrorMessage("This action cannot be performed on a team shared folder.");
            }
            case RelinquishFolderMembershipError.Tag.NO_PERMISSION -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_NO_PREMISSION);
                result.setErrorMessage("The current user does not have permission to perform this action.");
            }
            case RelinquishFolderMembershipError.Tag.NO_EXPLICIT_ACCESS -> {
                result.setTag(DropboxErrorTag. MEMBERSHIP_REMOVE_NO_EXPLICIT_ACCESS);
                result.setErrorMessage("The current user only has inherited access to the shared folder. You can't relinquish inherited membership to folders.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
    }

    private RemoveUserFromProjectFolderResult processRemoveFolderMemberError(RemoveFolderMemberError error) {
        final RemoveUserFromProjectFolderResult result = new RemoveUserFromProjectFolderResult(ThirdPartyOperationStatus.FAILED);

        switch (error.tag()) {
            case RemoveFolderMemberError.Tag.ACCESS_ERROR -> {
                processSharedFolderAccessErrorTags(error.getAccessErrorValue(), result);
            }
            case RemoveFolderMemberError.Tag.MEMBER_ERROR -> {
                processSharedFolderMemberErrorTags(error.getMemberErrorValue(), result);
            }
            case RemoveFolderMemberError.Tag.FOLDER_OWNER -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_FOLDER_OWNER);
                result.setErrorMessage("Cannot remove the owner of the folder.");
            }
            case RemoveFolderMemberError.Tag.GROUP_ACCESS -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_GROUP_ACCESS);
                result.setErrorMessage("The current user has access to the shared folder via a group. You can't relinquish membership to folders shared via groups.");
            }
            case RemoveFolderMemberError.Tag.TEAM_FOLDER -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_TEAM_FOLDER);
                result.setErrorMessage("This action cannot be performed on a team shared folder.");
            }
            case RemoveFolderMemberError.Tag.NO_PERMISSION -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_NO_PREMISSION);
                result.setErrorMessage("The current user does not have permission to perform this action.");
            }
            case RemoveFolderMemberError.Tag.TOO_MANY_FILES -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_REMOVE_TOO_MANY_FILES);
                result.setErrorMessage("The folder has too many files to leave a copy. Should not happen in this version.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
    }

    private TransferOwnershipResult processTransferFolderError(TransferFolderError error) {
        final TransferOwnershipResult result = new TransferOwnershipResult(ThirdPartyOperationStatus.FAILED);

        switch (error.tag()) {
            case TransferFolderError.Tag.ACCESS_ERROR -> {
                processSharedFolderAccessErrorTags(error.getAccessErrorValue(), result);
            }
            case TransferFolderError.Tag.INVALID_DROPBOX_ID -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_TRANSFER_INVALID_DROPBOX_ID);
                result.setErrorMessage("Target user ID is invalid.");
            }
            case TransferFolderError.Tag.NEW_OWNER_NOT_A_MEMBER -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_TRANSFER_NEW_OWNER_NOT_A_MEMBER);
                result.setErrorMessage("The target user is not a member of the folder.");
            }
            case TransferFolderError.Tag.NEW_OWNER_UNMOUNTED -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_TRANSFER_NEW_OWNER_UNMOUNTED);
                result.setErrorMessage("The target user does not have the folder mounted.");
            }
            case TransferFolderError.Tag.NEW_OWNER_EMAIL_UNVERIFIED -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_TRANSFER_NEW_OWNER_EMAIL_UNVERIFIED);
                result.setErrorMessage("The target user has an unverified email. This action is not allowed for unverified accounts.");
            }
            case TransferFolderError.Tag.NO_PERMISSION -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_TRANSFER_NO_PERMISSION);
                result.setErrorMessage("The current user does not have permission to perform this action.");
            }
            case TransferFolderError.Tag.TEAM_FOLDER -> {
                result.setTag(DropboxErrorTag. MEMBERSHIP_TRANSFER_TEAM_FOLDER);
                result.setErrorMessage("This action cannot be performed on a team shared folder.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
    }

    private FileUploadOperationResult processUploadError(UploadError error) {
        final FileUploadOperationResult result = new FileUploadOperationResult(ThirdPartyOperationStatus.FAILED);

        switch (error.tag()) {
            case UploadError.Tag.PATH -> {
                processWriteErrorTags(error.getPathValue().getReason(), result);
            }
            case UploadError.Tag.PROPERTIES_ERROR -> {
                result.setTag(DropboxErrorTag.FILES_UPLOAD_PROPERTIES);
                result.setErrorMessage("The supplied property group is invalid. The file has uploaded without property groups. Should not be possible.");
            }
            case UploadError.Tag.PAYLOAD_TOO_LARGE -> {
                result.setTag(DropboxErrorTag.FILES_UPLOAD_PAYLOAD_TOO_LARGE);
                result.setErrorMessage("The file is too large. Max size is 150 MiB.");
            }
            case UploadError.Tag.CONTENT_HASH_MISMATCH -> {
                result.setTag(DropboxErrorTag.FILES_UPLOAD_CONTENT_HASH_MISMATCH);
                result.setErrorMessage("The content received by the Dropbox server in this call does not match the provided content hash.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
    }

    private FileOperationResult processDownloadError(DownloadError error) {
        final FileOperationResult result = new FileOperationResult(ThirdPartyOperationStatus.FAILED);

        switch (error.tag()) {
            case DownloadError.Tag.PATH -> {
                processLookupErrorTags(error.getPathValue(), result);
            }
            case DownloadError.Tag.UNSUPPORTED_FILE -> {
                result.setTag(DropboxErrorTag.FILES_DOWNLOAD_UNSUPPORTED);
                result.setErrorMessage("This file type cannot be downloaded directly.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
    }

    private <T extends DropboxOperationResult> T processCreateFolderError(CreateFolderError error,
            Function<ThirdPartyOperationStatus, T> resultFactory) {
        final T result = resultFactory.apply(ThirdPartyOperationStatus.FAILED);

        switch (error.tag()) {
            case CreateFolderError.Tag.PATH -> {
                processWriteErrorTags(error.getPathValue(), result);
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
    }

    private CreatedProjectFolderResult processShareFolderError(ShareFolderError error) {
        final CreatedProjectFolderResult result = new CreatedProjectFolderResult(ThirdPartyOperationStatus.PARTIAL_SUCCESS);

        switch (error.tag()) {
            case ShareFolderError.Tag.EMAIL_UNVERIFIED -> {
                result.setTag(DropboxErrorTag.SHARE_FOLDER_EMIAL_UNVERIFIED);
                result.setErrorMessage("This user's email address is not verified. This "
                        + "functionality is only available on accounts with a verified email address.");
            }
            case ShareFolderError.Tag.BAD_PATH -> {
                result.setTag(DropboxErrorTag.SHARE_FOLDER_BAD_PATH);
                result.setErrorMessage("The request is invalid. This is likely due to the user's "
                        + "actions outside of the platform. There might be another folder with the same name.");
            }
            case ShareFolderError.Tag.TEAM_POLICY_DISALLOWS_MEMBER_POLICY -> {
                result.setTag(DropboxErrorTag.SHARE_FOLDER_TEAM_POLICY);
                result.setErrorMessage("Team policy or group sharing settings are more "
                        + "restrictive than ShareFolderArg.member_policy.");
            }
            case ShareFolderError.Tag.DISALLOWED_SHARED_LINK_POLICY -> {
                result.setTag(DropboxErrorTag.SHARE_FOLDER_DISALLOWED_SHARED_LINK_POLICY);
                result.setErrorMessage("The current user's account is not allowed to "
                        + "select the specified ShareFolderArg.shared_link_policy.");
            }
            case ShareFolderError.Tag.NO_PERMISSION -> {
                result.setTag(DropboxErrorTag.SHARE_FOLDER_NO_PERMISSION);
                result.setErrorMessage("The current user does not have permission to perform this action.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
    }

    private AddUserToProjectFolderResult processAddFolderMemberError(AddFolderMemberError error) {
        final AddUserToProjectFolderResult result = new AddUserToProjectFolderResult(ThirdPartyOperationStatus.FAILED);

        switch (error.tag()) {
            case AddFolderMemberError.Tag.ACCESS_ERROR -> {
                processSharedFolderAccessErrorTags(error.getAccessErrorValue(), result);
            }
            case AddFolderMemberError.Tag.EMAIL_UNVERIFIED -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_EMAIL_UNVERIFIED);
                result.setErrorMessage("This user's email address is not verified. This "
                        + "functionality is only available on accounts with a verified email address.");
            }
            case AddFolderMemberError.Tag.BANNED_MEMBER -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_BANNED_MEMBER);
                result.setErrorMessage("The current user has been banned.");
            }
            case AddFolderMemberError.Tag.BAD_MEMBER -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_BAD_MEMBER);
                result.setErrorMessage("The target user is not available. This is "
                        + "likely because their email address is not verified.");
            }
            case AddFolderMemberError.Tag.CANT_SHARE_OUTSIDE_TEAM -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_CANT_SHARE_OUTSIDE_TEAM);
                result.setErrorMessage("Your team policy does not allow sharing outside of the team.");
            }
            case AddFolderMemberError.Tag.TOO_MANY_MEMBERS -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_TOO_MANY_MEMBERS);
                result.setErrorMessage("This folder's member limit has been reached.");
            }
            case AddFolderMemberError.Tag.TOO_MANY_PENDING_INVITES -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_TOO_MANY_PENDING_INVITES);
                result.setErrorMessage("This folder's pending invite limit has been reached.");
            }
            case AddFolderMemberError.Tag.RATE_LIMIT -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_RATE_LIMIT);
                result.setErrorMessage("The current user has hit the limit of invites they can "
                        + "send per day. Try again in 24 hours.");
            }
            case AddFolderMemberError.Tag.TOO_MANY_INVITEES -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_TOO_MANY_INVITEES);
                result.setErrorMessage("The current user is trying to share with too many people at once.");
            }
            case AddFolderMemberError.Tag.INSUFFICIENT_PLAN -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_INSUFFICIENT_PLAN);
                result.setErrorMessage("The current user's account doesn't support this action.");
            }
            case AddFolderMemberError.Tag.TEAM_FOLDER -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_TEAM_FOLDER);
                result.setErrorMessage("This action cannot be performed on a team shared folder.");
            }
            case AddFolderMemberError.Tag.NO_PERMISSION -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_ADD_NO_PERMISSION);
                result.setErrorMessage("The current user does not have permission to perform this action.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
    }

    private AddUserToProjectFolderResult processMountFolderError(MountFolderError error) {
        final AddUserToProjectFolderResult result = new AddUserToProjectFolderResult(ThirdPartyOperationStatus.PARTIAL_SUCCESS);

        switch (error.tag()) {
            case MountFolderError.Tag.ACCESS_ERROR -> {
                processSharedFolderAccessErrorTags(error.getAccessErrorValue(), result);
            }
            case MountFolderError.Tag.INSIDE_SHARED_FOLDER -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_MOUNT_INSIDE_SHARED_FOLDER);
                result.setErrorMessage("Mounting would cause a shared folder to be inside another, which is disallowed.");
            }
            case MountFolderError.Tag.INSUFFICIENT_QUOTA -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_MOUNT_INSUFFICIENT_QUOTA);
                result.setErrorMessage("The user does not have enough space to mount the folder.");
            }
            case MountFolderError.Tag.ALREADY_MOUNTED -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_MOUNT_ALREADY_MOUNTED);
                result.setErrorMessage("The shared folder is already mounted.");
            }
            case MountFolderError.Tag.NO_PERMISSION -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_MOUNT_NO_PERMISSION);
                result.setErrorMessage("The current user does not have permission to perform this action.");
            }
            case MountFolderError.Tag.NOT_MOUNTABLE -> {
                result.setTag(DropboxErrorTag.MEMBERSHIP_MOUNT_NOT_MOUNTABLE);
                result.setErrorMessage("The shared folder is not mountable. One example where "
                        + "this can occur is when the shared folder belongs within a team folder in the user's Dropbox.");
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
    }

    private <T extends DropboxOperationResult> T processGetMetadataError(GetMetadataError error,
            Function<ThirdPartyOperationStatus, T> resultFactory) {
        final T result = resultFactory.apply(ThirdPartyOperationStatus.FAILED);

        switch (error.tag()) {
            case GetMetadataError.Tag.PATH -> {
                processLookupErrorTags(error.getPathValue(), result);
            }
            default -> {
                result.setTag(DropboxErrorTag.GENERAL_UNKNOWN);
                result.setErrorMessage("An unknown error has occured with Dropbox.");
            }
        }
        return result;
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
                                    ErrorType.EXTERNAL_INTERRUPTED, e,
                                    new DropboxOperationResult(ThirdPartyOperationStatus.FAILED));
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
