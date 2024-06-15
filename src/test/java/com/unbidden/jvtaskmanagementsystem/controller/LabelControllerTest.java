package com.unbidden.jvtaskmanagementsystem.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.label.CreateLabelRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.LabelResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.label.UpdateLabelRequestDto;
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
public class LabelControllerTest {
    protected static MockMvc mockMvc;

    private static ProjectRepository projectRepository;

    private static UserRepository userRepository;

    private static RoleRepository roleRepository;

    private static LabelRepository labelRepository;

    private static TaskRepository taskRepository;

    private static List<Project> projects;

    private static List<Task> tasks;

    private static List<Label> labels;

    private static List<LabelResponseDto> labelDtos;

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
        projects = new ArrayList<>();
        tasks = new ArrayList<>();
        labels = new ArrayList<>();

        User user = new User();
        user.setEmail("testCreator1@tms.com");
        user.setUsername("testCreator1");
        user.setFirstName("testCreatorName1");
        user.setLastName("testCreatorLastName1");
        user.setPassword("password123");
        user.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        userRepository.save(user);

        Project projectA = new Project();
        projectA.setName("projectA");
        projectA.setDescription("description1");
        projectA.setStartDate(LocalDate.now());
        projectA.setEndDate(LocalDate.now().plusDays(2));
        projectA.setStatus(ProjectStatus.IN_PROGRESS);
        ProjectRole projectRoleA = new ProjectRole();
        projectRoleA.setProject(projectA);
        projectRoleA.setRoleType(ProjectRoleType.CREATOR);
        projectRoleA.setUser(user);
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
        projectRoleB.setUser(user);
        projectB.setProjectRoles(Set.of(projectRoleB));
        projects.add(projectRepository.save(projectB));

        Label label1 = new Label();
        label1.setName("label1");
        label1.setColor("green");
        label1.setProject(projectA);
        labels.add(labelRepository.save(label1));

        Label label2 = new Label();
        label2.setName("label2");
        label2.setColor("yellow");
        label2.setProject(projectA);
        labels.add(labelRepository.save(label2));

        Task task1 = new Task();
        task1.setName("task1");
        task1.setDescription("description1");
        task1.setAssignee(user);
        task1.setDueDate(LocalDate.now().plusDays(1));
        task1.setLabels(Set.of(label1));
        task1.setPriority(TaskPriority.MEDIUM);
        task1.setStatus(TaskStatus.NOT_STARTED);
        task1.setProject(projectA);
        tasks.add(taskRepository.save(task1));

        Task task2 = new Task();
        task2.setName("task2");
        task2.setDescription("description2");
        task2.setAssignee(user);
        task2.setDueDate(LocalDate.now().plusDays(2));
        task2.setLabels(Set.of(label1, label2));
        task2.setPriority(TaskPriority.LOW);
        task2.setStatus(TaskStatus.NOT_STARTED);
        task2.setProject(projectA);
        tasks.add(taskRepository.save(task2));

        Task task3 = new Task();
        task3.setName("task3");
        task3.setDescription("description3");
        task3.setAssignee(user);
        task3.setDueDate(LocalDate.now().plusDays(1));
        task3.setLabels(Set.of());
        task3.setPriority(TaskPriority.HIGH);
        task3.setStatus(TaskStatus.NOT_STARTED);
        task3.setProject(projectB);
        tasks.add(taskRepository.save(task3));

        label1.setTasks(Set.of(task1, task2));
        label2.setTasks(Set.of(task2));
        labelDtos = labels.stream().map(l -> mapToDto(l)).toList();
    }

    @Test
    @WithUserDetails("testCreator1")
    void getLabelsForProject_ValidRequest_ListOfLabels() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/labels/projects/" + projects.get(0).getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andReturn();

        LabelResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), LabelResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals(labelDtos.get(0), actual[0]);
        Assertions.assertEquals(labelDtos.get(1), actual[1]);
    }

    @Test
    @WithUserDetails("testCreator1")
    void getLabelById_ValidRequest_Label() throws Exception {
        MvcResult result = mockMvc.perform(get("/labels/" + labels.get(0).getId()))
                .andExpect(status().isOk())
                .andReturn();

        LabelResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), LabelResponseDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(labelDtos.get(0), actual);
    }

    @Test
    @WithUserDetails("testCreator1")
    void createLabel_ValidRequest_NewLabel() throws Exception {
        final CreateLabelRequestDto requestDto = new CreateLabelRequestDto();
        requestDto.setName("newLabel");
        requestDto.setColor("brown");
        requestDto.setProjectId(projects.get(1).getId());
        requestDto.setTaskIds(Set.of(tasks.get(2).getId()));

        MvcResult result = mockMvc.perform(post("/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        LabelResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), LabelResponseDto.class);

        Assertions.assertNotNull(actual);

        Optional<Label> labelFromDbOpt = labelRepository.findById(actual.getId());
        Assertions.assertTrue(labelFromDbOpt.isPresent());
        LabelResponseDto labelFromDbDto = mapToDto(labelFromDbOpt.get());

        Assertions.assertEquals(labelFromDbDto, actual);
        Assertions.assertEquals(requestDto.getName(), actual.getName());
        Assertions.assertEquals(requestDto.getColor(), actual.getColor());
        Assertions.assertEquals(requestDto.getProjectId(), actual.getProjectId());
        Assertions.assertEquals(requestDto.getTaskIds(), actual.getTaskIds());
    }

    @Test
    @WithUserDetails("testCreator1")
    void updateLabel_ValidRequest_UpdatedLabel() throws Exception {
        final UpdateLabelRequestDto requestDto = new UpdateLabelRequestDto();
        requestDto.setName("updatedLabel");
        requestDto.setColor("white");
        requestDto.setTaskIds(Set.of());

        final Label newLabel = createNewLabel();

        MvcResult result = mockMvc.perform(put("/labels/" + newLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        LabelResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), LabelResponseDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(newLabel.getId(), actual.getId());
        Assertions.assertEquals(requestDto.getName(), actual.getName());
        Assertions.assertEquals(requestDto.getColor(), actual.getColor());
        Assertions.assertEquals(newLabel.getProject().getId(), actual.getProjectId());
        Assertions.assertEquals(requestDto.getTaskIds(), actual.getTaskIds());
    }

    @Test
    @WithUserDetails("testCreator1")
    void deleteLabel_ValidRequest_Ok() throws Exception {
        final Label newLabel = createNewLabel();

        mockMvc.perform(delete("/labels/" + newLabel.getId()))
                .andExpect(status().isNoContent())
                .andReturn();

        Optional<Label> potentialLabel = labelRepository.findById(newLabel.getId());

        Assertions.assertFalse(potentialLabel.isPresent());
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

    private static LabelResponseDto mapToDto(Label label) {
        LabelResponseDto dto = new LabelResponseDto();
        dto.setId(label.getId());
        dto.setName(label.getName());
        dto.setColor(label.getColor());
        dto.setProjectId(label.getProject().getId());
        dto.setTaskIds(new HashSet<>(label.getTasks().stream().map(t -> t.getId()).toList()));
        return dto;
    }

    private Label createNewLabel() {
        Label newLabel = new Label();
        newLabel.setName("newLabel");
        newLabel.setColor("black");
        newLabel.setProject(projects.get(1));
        newLabel.setTasks(Set.of(tasks.get(2)));
        return labelRepository.save(newLabel);
    }
}
