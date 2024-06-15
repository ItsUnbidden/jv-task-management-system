package com.unbidden.jvtaskmanagementsystem.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.task.CreateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.TaskResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.task.UpdateTaskStatusRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.Label;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskPriority;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskStatus;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.LabelRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.RoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
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
public class TaskControllerTest {
    protected static MockMvc mockMvc;

    private static ProjectRepository projectRepository;

    private static UserRepository userRepository;

    private static RoleRepository roleRepository;

    private static LabelRepository labelRepository;

    private static TaskRepository taskRepository;

    private static List<Project> projects;

    private static List<User> users;

    private static List<Task> tasks;

    private static Label label;

    private static List<TaskResponseDto> taskDtos;

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
        labelRepository = applicationContext.getBean(LabelRepository.class);
        taskRepository = applicationContext.getBean(TaskRepository.class);
        users = new ArrayList<>();
        tasks = new ArrayList<>();
        projects = new ArrayList<>();

        User user1 = new User();
        user1.setEmail("testUser1@tms.com");
        user1.setUsername("testUser1");
        user1.setFirstName("testUserName1");
        user1.setLastName("testUserLastName1");
        user1.setPassword("password123");
        user1.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        users.add(userRepository.save(user1));

        User user2 = new User();
        user2.setEmail("testUser2@tms.com");
        user2.setUsername("testUser2");
        user2.setFirstName("testUserName2");
        user2.setLastName("testUserLastName2");
        user2.setPassword("password321");
        user2.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        users.add(userRepository.save(user2));

        User user3 = new User();
        user3.setEmail("testUser3@tms.com");
        user3.setUsername("testUser3");
        user3.setFirstName("testUserName3");
        user3.setLastName("testUserLastName3");
        user3.setPassword("password321");
        user3.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        users.add(userRepository.save(user3));

        Project projectA = new Project();
        projectA.setName("projectA");
        projectA.setDescription("descriptionA");
        projectA.setStartDate(LocalDate.now());
        projectA.setEndDate(LocalDate.now().plusDays(3));
        projectA.setStatus(ProjectStatus.IN_PROGRESS);
        ProjectRole projectRole1 = new ProjectRole();
        projectRole1.setProject(projectA);
        projectRole1.setRoleType(ProjectRoleType.CREATOR);
        projectRole1.setUser(user1);
        ProjectRole projectRole2 = new ProjectRole();
        projectRole2.setProject(projectA);
        projectRole2.setRoleType(ProjectRoleType.CONTRIBUTOR);
        projectRole2.setUser(user2);
        projectA.setProjectRoles(Set.of(projectRole1, projectRole2));
        projects.add(projectRepository.save(projectA));

        Project projectB = new Project();
        projectB.setName("projectB");
        projectB.setDescription("descriptionB");
        projectB.setStartDate(LocalDate.now());
        projectB.setEndDate(LocalDate.now().plusDays(5));
        projectB.setStatus(ProjectStatus.IN_PROGRESS);
        ProjectRole projectRole3 = new ProjectRole();
        projectRole3.setProject(projectB);
        projectRole3.setRoleType(ProjectRoleType.CREATOR);
        projectRole3.setUser(user3);
        projectB.setProjectRoles(Set.of(projectRole3));
        projects.add(projectRepository.save(projectB));

        label = new Label();
        label.setName("label1");
        label.setColor("red");
        label.setProject(projectA);
        label = labelRepository.save(label);

        Task task1 = new Task();
        task1.setName("task1");
        task1.setDescription("description1");
        task1.setAssignee(user1);
        task1.setDueDate(LocalDate.now().plusDays(1));
        task1.setLabels(Set.of(label));
        task1.setPriority(TaskPriority.MEDIUM);
        task1.setStatus(TaskStatus.NOT_STARTED);
        task1.setProject(projectA);
        tasks.add(taskRepository.save(task1));
        
        Task task2 = new Task();
        task2.setName("task2");
        task2.setDescription("description2");
        task2.setAssignee(user2);
        task2.setDueDate(LocalDate.now().plusDays(2));
        task2.setLabels(Set.of());
        task2.setPriority(TaskPriority.MEDIUM);
        task2.setStatus(TaskStatus.NOT_STARTED);
        task2.setProject(projectA);
        tasks.add(taskRepository.save(task2));
        taskDtos = tasks.stream().map(t -> mapToDto(t)).toList();
    }

    @Test
    @WithUserDetails("testUser2")
    void getTasksForUser_ValidRequest_ListOfTasks() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/tasks/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andReturn();

        TaskResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), TaskResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.length);
        Assertions.assertEquals(taskDtos.get(1), actual[0]);
    }

    @Test
    @WithUserDetails("testUser1")
    void getTasksForUserInProjectById_ValidRequest_ListOfTasks() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/tasks/projects/" + projects.get(0).getId()
                + "/users/" + users.get(0).getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andReturn();
        
        TaskResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), TaskResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.length);
        Assertions.assertEquals(taskDtos.get(0), actual[0]);
    }

    @Test
    @WithUserDetails("testUser1")
    void getProjectTasks_ValidRequest_ListOfTasks() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/tasks/projects/" + projects.get(0).getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andReturn();

        TaskResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), TaskResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals(taskDtos.get(0), actual[0]);
        Assertions.assertEquals(taskDtos.get(1), actual[1]);
    }
    
    @Test
    @WithUserDetails("testUser1")
    void getTaskById_ValidRequest_Task() throws Exception {
        MvcResult result = mockMvc.perform(get("/tasks/" + tasks.get(0).getId()))
                .andExpect(status().isOk())
                .andReturn();

        TaskResponseDto actual = objectMapper.readValue(
                    result.getResponse().getContentAsString(), TaskResponseDto.class);
    
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(taskDtos.get(0), actual);
    }

    @Test
    @WithUserDetails("testUser1")
    void getTasksByLabelId_ValidRequest_ListOfTasks() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/tasks/labels/" + label.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andReturn();

        TaskResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), TaskResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.length);
        Assertions.assertEquals(taskDtos.get(0), actual[0]);
    }

    @Test
    @WithUserDetails("testUser3")
    void createTaskInProject_ValidRequest_NewTask() throws Exception {
        CreateTaskRequestDto requestDto = new CreateTaskRequestDto();
        requestDto.setName("newTask");
        requestDto.setDescription("newDescription");
        requestDto.setDueDate(LocalDate.now().plusDays(1));
        requestDto.setPriority(TaskPriority.HIGH);
        requestDto.setProjectId(projects.get(1).getId());

        MvcResult result = mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        TaskResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), TaskResponseDto.class);

        Assertions.assertNotNull(actual);

        Optional<Task> newTaskFromDbOpt = taskRepository.findById(actual.getId());
        Assertions.assertFalse(newTaskFromDbOpt.isEmpty());
        Task newTaskFromDb = newTaskFromDbOpt.get();

        Assertions.assertEquals(newTaskFromDb.getId(), actual.getId());
        Assertions.assertEquals(newTaskFromDb.getName(), actual.getName());
        Assertions.assertEquals(newTaskFromDb.getDescription(), actual.getDescription());
        Assertions.assertEquals(newTaskFromDb.getAssignee().getId(), actual.getAssigneeId());
        Assertions.assertEquals(newTaskFromDb.getAssignee().getUsername(),
                actual.getAssigneeUsername());
        Assertions.assertEquals(newTaskFromDb.getDueDate(), actual.getDueDate());
        Assertions.assertEquals(newTaskFromDb.getPriority(), actual.getPriority());
        Assertions.assertEquals(newTaskFromDb.getProject().getId(), actual.getProjectId());
        Assertions.assertEquals(newTaskFromDb.getProject().getName(), actual.getProjectName());
        Assertions.assertEquals(newTaskFromDb.getStatus(), actual.getStatus());
    }

    @Test
    @WithUserDetails("testUser3")
    void updateTask_ValidRequest_UpdatedTask() throws Exception {
        UpdateTaskRequestDto requestDto = new UpdateTaskRequestDto();
        requestDto.setName("updatedTask");
        requestDto.setDescription("updatedDescription");
        requestDto.setDueDate(LocalDate.now().plusDays(1));
        requestDto.setNewAssigneeId(users.get(2).getId());
        requestDto.setPriority(TaskPriority.LOW);

        final Task newTask = createNewTask();

        MvcResult result = mockMvc.perform(put("/tasks/" + newTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andReturn();

        TaskResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), TaskResponseDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(newTask.getId(), actual.getId());
        Assertions.assertEquals(requestDto.getName(), actual.getName());
        Assertions.assertEquals(requestDto.getDescription(), actual.getDescription());
        Assertions.assertEquals(requestDto.getNewAssigneeId(), actual.getAssigneeId());
        Assertions.assertEquals(requestDto.getDueDate(), actual.getDueDate());
        Assertions.assertEquals(requestDto.getPriority(), actual.getPriority());
        Assertions.assertEquals(newTask.getProject().getId(), actual.getProjectId());
        Assertions.assertEquals(newTask.getProject().getName(), actual.getProjectName());
        Assertions.assertEquals(newTask.getStatus(), actual.getStatus());
    }

    @Test
    @WithUserDetails("testUser3")
    void deleteTask_ValidRequest_Ok() throws Exception {
        final Task newTask = createNewTask();

        mockMvc.perform(delete("/tasks/" + newTask.getId()))
                .andExpect(status().isNoContent())
                .andReturn();

        Assertions.assertTrue(taskRepository.findById(newTask.getId()).isEmpty());
    }

    @Test
    @WithUserDetails("testUser3")
    void changeStatus_ValidRequest_PatchedTask() throws Exception {
        UpdateTaskStatusRequestDto requestDto = new UpdateTaskStatusRequestDto();
        requestDto.setNewStatus(TaskStatus.IN_PROGRESS);

        final Task newTask = createNewTask();

        MvcResult result = mockMvc.perform(patch("/tasks/" + newTask.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();
                
        TaskResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), TaskResponseDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(newTask.getId(), actual.getId());
        Assertions.assertEquals(requestDto.getNewStatus(), actual.getStatus());
    }

    @AfterAll
    static void clearDb(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/label/delete-labels.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/task/delete-tasks.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/project/delete-projects.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/user/delete-test-users.sql"));
        }
    }

    private static TaskResponseDto mapToDto(Task task) {
        TaskResponseDto dto = new TaskResponseDto();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setAssigneeId(task.getAssignee().getId());
        dto.setAssigneeUsername(task.getAssignee().getUsername());
        dto.setDueDate(task.getDueDate());
        dto.setProjectId(task.getProject().getId());
        dto.setProjectName(task.getProject().getName());
        dto.setLabelIds(new HashSet<>(task.getLabels().stream().map(l -> l.getId()).toList()));
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        return dto;
    }

    private Task createNewTask() {
        Task task = new Task();
        task.setName("newTask");
        task.setDescription("newDescription");
        task.setAssignee(users.get(2));
        task.setDueDate(LocalDate.now().plusDays(2));
        task.setLabels(Set.of());
        task.setPriority(TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.NOT_STARTED);
        task.setProject(projects.get(1));
        return taskRepository.save(task);
    }
}
