package com.unbidden.jvtaskmanagementsystem.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.project.CreateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.ProjectResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.project.UpdateProjectStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.ProjectRoleDto;
import com.unbidden.jvtaskmanagementsystem.dto.projectrole.UpdateProjectRoleRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.RoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ProjectControllerTest {
    protected static MockMvc mockMvc;

    private static ProjectRepository projectRepository;

    private static UserRepository userRepository;

    private static RoleRepository roleRepository;

    private static List<Project> projects;

    private static List<ProjectResponseDto> projectDtos;

    private static List<User> users;

    @Autowired 
    private ObjectMapper objectMapper;

    @BeforeAll
    static void init(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        projectRepository = applicationContext.getBean(ProjectRepository.class);
        userRepository = applicationContext.getBean(UserRepository.class);
        roleRepository = applicationContext.getBean(RoleRepository.class);
        projects = new ArrayList<>();
        users = new ArrayList<>();

        User user1 = new User();
        user1.setEmail("testCreator1@tms.com");
        user1.setUsername("testCreator1");
        user1.setFirstName("testCreatorName1");
        user1.setLastName("testCreatorLastName1");
        user1.setPassword("password123");
        user1.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        users.add(userRepository.save(user1));

        User user2 = new User();
        user2.setEmail("testCreator2@tms.com");
        user2.setUsername("testCreator2");
        user2.setFirstName("testCreatorName2");
        user2.setLastName("testCreatorLastName2");
        user2.setPassword("password321");
        user2.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        users.add(userRepository.save(user2));

        User user3 = new User();
        user3.setEmail("testCreator3@tms.com");
        user3.setUsername("testCreator3");
        user3.setFirstName("testCreatorName3");
        user3.setLastName("testCreatorLastName3");
        user3.setPassword("password231");
        user3.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        users.add(userRepository.save(user3));

        User user4 = new User();
        user4.setEmail("testCreator4@tms.com");
        user4.setUsername("testCreator4");
        user4.setFirstName("testCreatorName4");
        user4.setLastName("testCreatorLastName4");
        user4.setPassword("password132");
        user4.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        users.add(userRepository.save(user4));

        Project projectA = new Project();
        projectA.setName("projectA");
        projectA.setDescription("description1");
        projectA.setStartDate(LocalDate.now());
        projectA.setEndDate(LocalDate.now().plusDays(1));
        projectA.setStatus(ProjectStatus.IN_PROGRESS);
        ProjectRole projectRoleA = new ProjectRole();
        projectRoleA.setProject(projectA);
        projectRoleA.setRoleType(ProjectRoleType.CREATOR);
        projectRoleA.setUser(user1);
        projectA.setProjectRoles(Set.of(projectRoleA));
        projects.add(projectRepository.save(projectA));

        Project projectB = new Project();
        projectB.setName("projectB");
        projectB.setDescription("description2");
        projectB.setStartDate(LocalDate.now().plusDays(1));
        projectB.setEndDate(LocalDate.now().plusDays(3));
        projectB.setStatus(ProjectStatus.INITIATED);
        ProjectRole projectRoleB = new ProjectRole();
        projectRoleB.setProject(projectB);
        projectRoleB.setRoleType(ProjectRoleType.CREATOR);
        projectRoleB.setUser(user2);
        projectB.setProjectRoles(Set.of(projectRoleB));
        projects.add(projectRepository.save(projectB));

        Project projectAB = new Project();
        projectAB.setName("projectAB");
        projectAB.setDescription("description3");
        projectAB.setStartDate(LocalDate.now().plusDays(2));
        projectAB.setEndDate(LocalDate.now().plusDays(3));
        projectAB.setStatus(ProjectStatus.INITIATED);
        ProjectRole projectRoleAbUser1 = new ProjectRole();
        projectRoleAbUser1.setProject(projectAB);
        projectRoleAbUser1.setRoleType(ProjectRoleType.CREATOR);
        projectRoleAbUser1.setUser(user1);
        ProjectRole projectRoleAbUser2 = new ProjectRole();
        projectRoleAbUser2.setProject(projectAB);
        projectRoleAbUser2.setRoleType(ProjectRoleType.CONTRIBUTOR);
        projectRoleAbUser2.setUser(user2);
        projectAB.setProjectRoles(Set.of(projectRoleAbUser1, projectRoleAbUser2));
        projects.add(projectRepository.save(projectAB));

        projectDtos = projects.stream().map(p -> mapToDto(p)).toList();
    }

    @Test
    @WithUserDetails("testCreator1")
    void findProjectById_CorrectId_Project() throws Exception {
        MvcResult result = mockMvc.perform(get("/projects/" + projects.get(0).getId()))
                .andExpect(status().isOk())
                .andReturn();

        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);
        
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(projectDtos.get(0), actual);
    }

    @Test
    @WithUserDetails("testCreator1")
    void findAllProjectsForUser_ValidUser_ProjectList() throws Exception {
        MvcResult result = mockMvc.perform(get("/projects/me"))
                .andExpect(status().isOk())
                .andReturn();

        ProjectResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals(projectDtos.get(0), actual[0]);
    }

    @Test
    @WithUserDetails("testCreator1")
    void searchProjectsByName_ValidName_ProjectList() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/projects/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable))
                .param("name", "projectA"))
                .andExpect(status().isOk())
                .andReturn();

        ProjectResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals(projectDtos.get(0), actual[0]);
        Assertions.assertEquals(projectDtos.get(2), actual[1]);
    }

    @Test
    @WithUserDetails("testCreator3")
    void createProject_ValidRequest_NewProject() throws Exception {
        CreateProjectRequestDto requestDto = new CreateProjectRequestDto();
        requestDto.setName("createdProject");
        requestDto.setDescription("newDescription");
        requestDto.setStartDate(LocalDate.now());
        requestDto.setEndDate(LocalDate.now().plusDays(2));
        requestDto.setPrivate(false);
        
        MvcResult result = mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);
        
        Assertions.assertNotNull(actual);
        
        Project newProjectFromDb = projectRepository.findById(actual.getId()).get();
        ProjectResponseDto expected = mapToDto(newProjectFromDb);

        Assertions.assertEquals(expected, actual);

        Assertions.assertEquals(requestDto.getName(), actual.getName());
        Assertions.assertEquals(requestDto.getDescription(), actual.getDescription());
        Assertions.assertEquals(requestDto.getStartDate(), actual.getStartDate());
        Assertions.assertEquals(requestDto.getEndDate(), actual.getEndDate());
        Assertions.assertEquals(ProjectStatus.IN_PROGRESS, actual.getStatus());
    }

    @Test
    @WithUserDetails("testCreator3")
    void updateProject_ValidRequest_UpdatedProject() throws Exception {
        UpdateProjectRequestDto requestDto = new UpdateProjectRequestDto();
        requestDto.setName("updatedName");
        requestDto.setDescription("updatedDescription");
        requestDto.setStartDate(LocalDate.now().plusDays(1));
        requestDto.setEndDate(LocalDate.now().plusDays(2));
        
        final Project newProject = addNewProject();
        newProject.setName(requestDto.getName());
        newProject.setDescription(requestDto.getDescription());
        newProject.setStartDate(requestDto.getStartDate());
        newProject.setEndDate(requestDto.getEndDate());
        newProject.setStatus(ProjectStatus.INITIATED);
        
        MvcResult result = mockMvc.perform(put("/projects/" + newProject.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(mapToDto(newProject), actual);
    }

    @Test
    @WithUserDetails("testCreator3")
    void deleteProject_ValidRequest_Ok() throws Exception {
        final Project newProject = addNewProject();

        mockMvc.perform(delete("/projects/" + newProject.getId()))
                .andExpect(status().isNoContent())
                .andReturn();

        Optional<Project> newProjectOpt = projectRepository.findById(newProject.getId());
        Assertions.assertTrue(newProjectOpt.isEmpty());
    }

    @Test
    @WithUserDetails("testCreator3")
    void addUserToProject_ValidRequest_PatchedProject() throws Exception {
        final Project newProject = addNewProject();

        MvcResult result = mockMvc.perform(post("/projects/" + newProject.getId() + "/users/"
                + users.get(3).getId() + "/add"))
                .andExpect(status().isOk())
                .andReturn();
        
        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getProjectRoles());
        List<ProjectRoleDto> potentialProjectRole = actual.getProjectRoles().stream()
                .filter(pr -> pr.getRoleType().equals(ProjectRoleType.CONTRIBUTOR) 
                && pr.getUsername().equals(users.get(3).getUsername()))
                .toList();
        Assertions.assertEquals(1, potentialProjectRole.size());
    }

    @Test
    @WithUserDetails("testCreator3")
    void changeProjectMemberRole_ValidRequest_PatchedProject() throws Exception {
        final Project newProject = addNewProject();

        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(newProject);
        projectRole.setRoleType(ProjectRoleType.CONTRIBUTOR);
        projectRole.setUser(users.get(3));
        newProject.getProjectRoles().add(projectRole);
        projectRepository.save(newProject);

        UpdateProjectRoleRequestDto requestDto = new UpdateProjectRoleRequestDto();
        requestDto.setNewRole(ProjectRoleType.ADMIN);

        MvcResult result = mockMvc.perform(patch("/projects/" + newProject.getId() + "/users/"
                + users.get(3).getId() + "/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getProjectRoles());
        List<ProjectRoleDto> potentialProjectRole = actual.getProjectRoles().stream()
                .filter(pr -> pr.getRoleType().equals(ProjectRoleType.ADMIN)
                && pr.getUsername().equals(users.get(3).getUsername()))
                .toList();
        Assertions.assertEquals(1, potentialProjectRole.size());
    }

    @Test
    @WithUserDetails("testCreator3")
    void removeUserFromProject_ValidRequest_PatchedProject() throws Exception {
        final Project newProject = addNewProject();

        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(newProject);
        projectRole.setRoleType(ProjectRoleType.CONTRIBUTOR);
        projectRole.setUser(users.get(3));
        newProject.getProjectRoles().add(projectRole);
        projectRepository.save(newProject);

        MvcResult result = mockMvc.perform(delete("/projects/" + newProject.getId()
                + "/users/" + users.get(3).getId() + "/remove"))
                .andExpect(status().isOk())
                .andReturn();

        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getProjectRoles());
        List<ProjectRoleDto> potentialProjectRole = actual.getProjectRoles().stream()
                .filter(pr -> pr.getRoleType().equals(ProjectRoleType.CONTRIBUTOR)
                && pr.getUsername().equals(users.get(3).getUsername()))
                .toList();
        Assertions.assertTrue(potentialProjectRole.isEmpty());
    }

    @Test
    @WithUserDetails("testCreator3")
    void changeStatus_ValidRequest_PatchedProject() throws Exception {
        final Project newProject = addNewProject();
        final UpdateProjectStatusRequestDto requestDto = new UpdateProjectStatusRequestDto();
        requestDto.setNewStatus(ProjectStatus.COMPLETED);

        MvcResult result = mockMvc.perform(patch("/projects/" + newProject.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        ProjectResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ProjectResponseDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(ProjectStatus.COMPLETED, actual.getStatus());
    }

    @AfterAll
    static void clearDb(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/project/delete-projects.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/user/delete-test-users.sql"));
        }
    }

    private static ProjectResponseDto mapToDto(Project project) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCalendarConnected(false);
        dto.setDropboxConnected(false);
        dto.setEndDate(project.getEndDate());
        dto.setStartDate(project.getStartDate());
        dto.setStatus(project.getStatus());
        dto.setProjectRoles(new HashSet<>(project.getProjectRoles().stream()
                .map(pr -> mapProjectRoleToDto(pr))
                .toList()));
        return dto;
    }
    
    private static ProjectRoleDto mapProjectRoleToDto(ProjectRole projectRole) {
        ProjectRoleDto dto = new ProjectRoleDto();
        dto.setRoleType(projectRole.getRoleType());
        dto.setUsername(projectRole.getUser().getUsername());
        return dto;
    }

    private Project addNewProject() {
        Project project = new Project();
        project.setName("newProject");
        project.setDescription("newDescription");
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(1));
        project.setStatus(ProjectStatus.IN_PROGRESS);
        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(project);
        projectRole.setRoleType(ProjectRoleType.CREATOR);
        projectRole.setUser(users.get(2));
        Set<ProjectRole> projectRoles = new HashSet<>();
        projectRoles.add(projectRole);
        project.setProjectRoles(projectRoles);
        return projectRepository.save(project);
    }
}
