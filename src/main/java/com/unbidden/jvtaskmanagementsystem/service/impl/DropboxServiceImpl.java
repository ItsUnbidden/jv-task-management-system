package com.unbidden.jvtaskmanagementsystem.service.impl;

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
import com.unbidden.jvtaskmanagementsystem.exception.EntityNotFoundException;
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
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRoleRepository;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
import com.unbidden.jvtaskmanagementsystem.service.oauth2.OAuth2Service;
import com.unbidden.jvtaskmanagementsystem.util.EntityUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DropboxServiceImpl implements DropboxService {
    private static final Logger LOGGER = LogManager.getLogger(DropboxServiceImpl.class);

    private final ClientRegistration clientRegistration;

    private final OAuth2Service oauthService;

    private final ProjectRoleRepository projectRoleRepository;

    private final DbxRequestConfig dbxRequestConfig;

    private final String dropboxRootPath;

    public DropboxServiceImpl(@Autowired EntityUtil entityUtil,
            @Autowired OAuth2Service oauthService,
            @Autowired ProjectRoleRepository projectRoleRepository,
            @Autowired DbxRequestConfig dbxRequestConfig,
            @Value("${dropbox.root.path}") String dropboxRootPath) {
        this.clientRegistration = entityUtil.getClientRegistrationByName("dropbox");
        this.oauthService = oauthService;
        this.projectRoleRepository = projectRoleRepository;
        this.dbxRequestConfig = dbxRequestConfig;
        this.dropboxRootPath = dropboxRootPath;
    }

    @Override
    public void createSharedProjectFolder(User user, Project project) {
        try {
            OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(user,
                    clientRegistration);
            DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig,
                    authorizedClient.getToken());
            createSharedProjectFolder0(dbxClient, project);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            LOGGER.warn("Since user " + user.getId() + " hasn't connected their dropbox account,"
                    + " the project will not have shared folder. It can be connected later.", e);
        }
    }

    @Override
    public void deleteProjectFolder(User user, Project project) {
        if (project.getDropboxProjectFolderId() != null) {
            final DbxClientV2 dbxClient = getDbxClient(user);

            try {
                dbxClient.files().deleteV2(project.getDropboxProjectFolderId());
            } catch (DeleteErrorException e) {
                throw new SpecificDropboxException("Uanble to delete project " + project.getId()
                        + "'s folder on dropbox.", e);
            } catch (DbxException e) {
                throw new GeneralDropboxException("General dropbox exception was thrown.", e);
            }
        }
    }

    @Override
    public void createTaskFolder(User user, Task task) {
        if (task.getProject().getDropboxProjectFolderId() != null) {
            final DbxClientV2 dbxClient = getDbxClient(user);

            createTaskFolder0(dbxClient, task);
        }
    }

    @Override
    public void deleteTaskFolder(User user, Task task) {
        if (task.getProject().getDropboxProjectFolderId() != null) {
            final DbxClientV2 dbxClient = getDbxClient(user);

            try {
                dbxClient.files().deleteV2(task.getDropboxTaskFolderId());
            } catch (CreateFolderErrorException e) {
                throw new SpecificDropboxException("Unable to delete a folder for task "
                        + task.getId(), e);
            } catch (DbxException e) {
                throw new GeneralDropboxException("General dropbox exception was thrown.", e);
            }
        }
    }

    @Override
    public void addProjectMemberToSharedFolder(User user, User newMember,
            Project project) {
        if (project.getDropboxProjectFolderId() != null) {
            if (user.getId() == newMember.getId()) {
                throw new UnsupportedOperationException("User " + user.getId() 
                        + " cannot add themselfs to project " + project.getId() + ".");
            }

            final DbxClientV2 dbxClient = getDbxClient(user);
            final OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(newMember);        
            
            addUserByAuthorizedClientToDropboxFolder(dbxClient, authorizedClient, project);
        }
    }

    @Override
    public void removeMemberFromSharedFolder(User user, User memberToRemove,
            Project project) {
        if (project.getDropboxProjectFolderId() != null) {
            final DbxClientV2 dbxClient = getDbxClient(user);

            try {
                if (user.getId() == memberToRemove.getId()) {
                    try {
                        dbxClient.sharing().relinquishFolderMembership(
                                project.getDropboxProjectSharedFolderId());
                        return;
                    } catch (RelinquishFolderMembershipErrorException e) {
                        throw new SpecificDropboxException("Unable to relinquish folder "
                                + "membership for user " + user.getId() + " in project "
                                + project.getId(), e);
                    }
                }
                OAuth2AuthorizedClient authorizedClientForUserToRemove =
                        getAuthorizedClient(memberToRemove);
                try {
                    dbxClient.sharing().removeFolderMember(
                            project.getDropboxProjectSharedFolderId(),
                            MemberSelector.dropboxId(authorizedClientForUserToRemove
                            .getExternalAccountId()), false);
                } catch (RemoveFolderMemberErrorException e) {
                    throw new SpecificDropboxException("Unable to remove user "
                            + memberToRemove.getId() + " from project " + project.getId()
                            + "'s folder.", e);
                }   
            } catch (DbxException e) {
                throw new GeneralDropboxException("General dropbox exception was thrown.", e);
            }
        }
    }

    @Override
    public void transferOwnership(User user, User newOwner,
            Project project) {
        if (project.getDropboxProjectFolderId() != null) {
            if (user.getId() == newOwner.getId()) {
                throw new UnsupportedOperationException("User " + user.getId() 
                        + " cannot transfer project " + project.getId() 
                        + "'s folder to themselfs.");
            }

            final DbxClientV2 dbxClient = getDbxClient(user);

            OAuth2AuthorizedClient authorizedClientForNewUser = getAuthorizedClient(user);
            if (authorizedClientForNewUser == null) {
                throw new UnsupportedOperationException("Unable to transfer project "
                        + project.getId() + "'s fodler to user " + newOwner.getId()
                        + " because new owner does not have dropbox connected.");
            }

            try {
                dbxClient.sharing().transferFolder(project.getDropboxProjectSharedFolderId(),
                        authorizedClientForNewUser.getExternalAccountId());
            } catch (TransferFolderErrorException e) {
                throw new SpecificDropboxException("Unable to transfer project " 
                        + project.getId() + "'s folder to user " + newOwner.getId() + ".", e);
            } catch (DbxException e) {
                throw new GeneralDropboxException("General dropbox exception was thrown.", e);
            }
        }
    }

    @Override
    public void connectProjectToDropbox(User user,
            Project project) {
        if (project.getDropboxProjectFolderId() != null) {
            throw new UnsupportedOperationException("No need to connect project " 
                    + project.getId() + " to dropbox because it is already connected.");
        }
        final DbxClientV2 dbxClient = getDbxClient(user);
        List<OAuth2AuthorizedClient> authorizedClients = new ArrayList<>();

        for (ProjectRole projectRole : project.getProjectRoles()) {
            if (!projectRole.getRoleType().equals(ProjectRoleType.CREATOR)) {
                try {
                    authorizedClients.add(oauthService.loadAuthorizedClient(projectRole.getUser(),
                            clientRegistration));
                } catch (OAuth2AuthorizedClientLoadingException e) {
                    throw new UnsupportedOperationException("User " + projectRole.getUser()
                            .getId() + " is missing valid authorized client and is a member of "
                            + "project " + project.getId() + ". Therefore project "
                            + project.getId() + " cannot be connected to dropbox until the user"
                            + " logs in or is removed.", e);
                }
            }
        }
        createSharedProjectFolder0(dbxClient, project);
        for (Task task : project.getTasks()) {
            createTaskFolder0(dbxClient, task);
        }
        for (OAuth2AuthorizedClient authorizedClient : authorizedClients) {
            addUserByAuthorizedClientToDropboxFolder(dbxClient, authorizedClient, project);
        }
    }

    @Override
    public FileMetadata uploadFileInTaskFolder(User user, Task task,
            MultipartFile file) {
        final DbxClientV2 dbxClient = getDbxClient(user);

        try {
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
            throw new GeneralDropboxException("General dropbox exception was thrown.", e);
        } catch (IOException e) {
            throw new ThirdPartyApiException("Dropbox client was unable to read the byte array "
                    + "or it is unaccessable.", e);
        }
    }

    @Override
    public FileMetadata downloadFile(User user, String dropboxId,
            OutputStream os) {
        final DbxClientV2 dbxClient = getDbxClient(user);

        try {
            return dbxClient.files().download(dropboxId).download(os);
        } catch (DownloadErrorException e) {
            throw new SpecificDropboxException("Unable to download a file " + dropboxId
                    + " from dropbox.", e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("General dropbox exception was thrown.", e);
        } catch (IOException e) {
            throw new ThirdPartyApiException("Dropbox client was unable to write to "
                    + "the output stream.", e);
        }
    }

    @Override
    public EchoResult testDropboxUserConnection(User user) {
        final DbxClientV2 dbxClient = getDbxClient(user);

        try {
            return dbxClient.check().user("This is a test query. If you see this, that means "
                    + "that dropbox is probably operational.");
        } catch (DbxApiException e) {
            throw new SpecificDropboxException("Unable to test dropbox.", e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("General dropbox exception was thrown.", e);
        }
    }

    @Override
    public void logout(User user) {
        List<ProjectRole> projectRoles = projectRoleRepository.findByUserId(user.getId());
        if (projectRoles.isEmpty()) {
            try {
            
                final OAuth2AuthorizedClient authorizedClient =
                        oauthService.loadAuthorizedClient(user, clientRegistration);
                final DbxClientV2 dbxClient = new DbxClientV2(dbxRequestConfig,
                        authorizedClient.getToken());
                try {
                    dbxClient.auth().tokenRevoke();
                    oauthService.deleteAuthorizedClient(authorizedClient);
                    return;
                } catch (DbxApiException e) {
                    throw new SpecificDropboxException("Unable to logout.", e);
                } catch (DbxException e) {
                    throw new ThirdPartyApiException("General dropbox exception was thrown.", e);
                }
            } catch (OAuth2AuthorizedClientLoadingException e) {
                throw new UnsupportedOperationException("Unable to logout because user is not "
                        + "currently logged in.", e);
            }
        }
        List<Long> projectIds = projectRoles.stream().map(pr -> pr.getProject().getId()).toList();
        throw new UnsupportedOperationException("Unable to log out because user "
                + user.getId() + " is a part of one or several projects " + projectIds
                + ". User has to quit all projects to log out.");
    }

    private DbxClientV2 getDbxClient(User user) {
        OAuth2AuthorizedClient authorizedClient;
        try {
            authorizedClient = oauthService.loadAuthorizedClient(
                    user, clientRegistration);
            LOGGER.info("Creating DbxClient object for dropbox access.");
            return new DbxClientV2(dbxRequestConfig, authorizedClient.getToken());
        } catch (OAuth2AuthorizedClientLoadingException e) {
            throw new ThirdPartyApiException("Unable to create DropboxClient because "
                    + "authorized client for user " + user.getId()
                    + " is not available. User should log in first.", e);
        }
    }

    private OAuth2AuthorizedClient getAuthorizedClient(User user) {
        try {
            return oauthService.loadAuthorizedClient(user, clientRegistration);
        } catch (OAuth2AuthorizedClientLoadingException e) {
            throw new EntityNotFoundException("Unable to find valid authorized client for user "
                    + user.getId() + ". The user will have to login into dropbox for this "
                    + "operation to become available.");
        }
    }

    private void createSharedProjectFolder0(DbxClientV2 dbxClient, Project project) {
        try {
            CreateFolderResult folderMeta = dbxClient.files()
                    .createFolderV2(dropboxRootPath + "/" + project.getName(),
                    true);
            ShareFolderLaunch sharedFolderMeta = dbxClient.sharing()
                    .shareFolderBuilder(folderMeta.getMetadata().getId())
                    .withAclUpdatePolicy(AclUpdatePolicy.EDITORS)
                    .start();
            project.setDropboxProjectFolderId(folderMeta.getMetadata().getId());
            project.setDropboxProjectSharedFolderId(sharedFolderMeta.getCompleteValue()
                    .getSharedFolderId());
        } catch (ShareFolderErrorException e) {
            if (project.getId() == null) {
                throw new SpecificDropboxException("Unable to create shared folder for "
                        + "new project " + project.getName() + ".", e);
            }
            throw new SpecificDropboxException("Unable to create shared folder for project "
                    + project.getId() + ".", e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("General dropbox exception was thrown.", e);
        }
    }

    private void createTaskFolder0(DbxClientV2 dbxClient, Task task) {
        try {
            Metadata projectFolderMeta = dbxClient.files()
                    .getMetadata(task.getProject().getDropboxProjectFolderId());
            CreateFolderResult taskFolderMeta = dbxClient.files().createFolderV2(
                    projectFolderMeta.getPathLower() + "/" + task.getName(), true);
            task.setDropboxTaskFolderId(taskFolderMeta.getMetadata().getId());
        } catch (CreateFolderErrorException e) {
            throw new SpecificDropboxException("Unable to create a folder for task "
                    + task.getId(), e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("General dropbox exception was thrown.", e);
        }
    }

    private void addUserByAuthorizedClientToDropboxFolder(DbxClientV2 dbxClient,
            OAuth2AuthorizedClient authorizedClient, Project project) {
        final DbxClientV2 dbxClientNewUser = new DbxClientV2(dbxRequestConfig,
                authorizedClient.getToken());
        final AddMember addMember = new AddMember(MemberSelector.dropboxId(
                authorizedClient.getExternalAccountId()), AccessLevel.EDITOR);
        try {
            dbxClient.sharing().addFolderMember(
                    project.getDropboxProjectSharedFolderId(), List.of(addMember));
            dbxClientNewUser.sharing().mountFolder(
                    project.getDropboxProjectSharedFolderId());
        } catch (AddFolderMemberErrorException e) {
            throw new SpecificDropboxException("Unable to add user " + authorizedClient.getUser()
                    .getId() + " to project " + project.getId() + "'s folder.", e);
        } catch (MountFolderErrorException e) {
            throw new SpecificDropboxException("Unable to mount project "
                    + project.getId() + "'s shared folder for newly added user "
                    + authorizedClient.getUser().getId() + ".", e);
        } catch (DbxException e) {
            throw new GeneralDropboxException("General dropbox exception was thrown.", e);
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
}
