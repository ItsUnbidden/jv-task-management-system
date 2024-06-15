package com.unbidden.jvtaskmanagementsystem.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CommentWithTaskIdResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.CreateMessageRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.MessageResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.message.ReplyResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Comment;
import com.unbidden.jvtaskmanagementsystem.model.Message;
import com.unbidden.jvtaskmanagementsystem.model.Project;
import com.unbidden.jvtaskmanagementsystem.model.Project.ProjectStatus;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole;
import com.unbidden.jvtaskmanagementsystem.model.ProjectRole.ProjectRoleType;
import com.unbidden.jvtaskmanagementsystem.model.Reply;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.Task;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskPriority;
import com.unbidden.jvtaskmanagementsystem.model.Task.TaskStatus;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.CommentRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ProjectRepository;
import com.unbidden.jvtaskmanagementsystem.repository.ReplyRepository;
import com.unbidden.jvtaskmanagementsystem.repository.RoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.TaskRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class MessageControllerTest {
    protected static MockMvc mockMvc;

    private static ProjectRepository projectRepository;

    private static UserRepository userRepository;

    private static RoleRepository roleRepository;

    private static TaskRepository taskRepository;

    private static CommentRepository commentRepository;

    private static ReplyRepository replyRepository;

    private static List<Project> projects;

    private static List<Task> tasks;

    private static List<User> users;

    private static List<Message> messages;

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
        taskRepository = applicationContext.getBean(TaskRepository.class);
        commentRepository = applicationContext.getBean(CommentRepository.class);
        replyRepository = applicationContext.getBean(ReplyRepository.class);
        projects = new ArrayList<>();
        tasks = new ArrayList<>();
        messages = new ArrayList<>();
        users = new ArrayList<>();

        User user1 = new User();
        user1.setEmail("alice@example.com");
        user1.setUsername("alice");
        user1.setFirstName("alice");
        user1.setLastName("alice");
        user1.setPassword("password123");
        user1.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        users.add(userRepository.save(user1));

        User user2 = new User();
        user2.setEmail("bob@tms.com");
        user2.setUsername("bob");
        user2.setFirstName("bob");
        user2.setLastName("bob");
        user2.setPassword("password321");
        user2.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        users.add(userRepository.save(user2));

        Project projectA = new Project();
        projectA.setName("projectA");
        projectA.setDescription("description1");
        projectA.setStartDate(LocalDate.now());
        projectA.setEndDate(LocalDate.now().plusDays(2));
        projectA.setStatus(ProjectStatus.IN_PROGRESS);
        ProjectRole projectRoleA1 = new ProjectRole();
        projectRoleA1.setProject(projectA);
        projectRoleA1.setRoleType(ProjectRoleType.CREATOR);
        projectRoleA1.setUser(user1);
        ProjectRole projectRoleA2 = new ProjectRole();
        projectRoleA2.setProject(projectA);
        projectRoleA2.setRoleType(ProjectRoleType.ADMIN);
        projectRoleA2.setUser(user2);
        projectA.setProjectRoles(Set.of(projectRoleA1, projectRoleA2));
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
        projectRoleB.setUser(user1);
        projectB.setProjectRoles(Set.of(projectRoleB));
        projects.add(projectRepository.save(projectB));

        Task task1 = new Task();
        task1.setName("task1");
        task1.setDescription("description1");
        task1.setAssignee(user1);
        task1.setDueDate(LocalDate.now().plusDays(1));
        task1.setLabels(Set.of());
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
        task2.setPriority(TaskPriority.LOW);
        task2.setStatus(TaskStatus.NOT_STARTED);
        task2.setProject(projectA);
        tasks.add(taskRepository.save(task2));

        Task task3 = new Task();
        task3.setName("task3");
        task3.setDescription("description3");
        task3.setAssignee(user1);
        task3.setDueDate(LocalDate.now().plusDays(1));
        task3.setLabels(Set.of());
        task3.setPriority(TaskPriority.HIGH);
        task3.setStatus(TaskStatus.NOT_STARTED);
        task3.setProject(projectB);
        tasks.add(taskRepository.save(task3));

        Comment comment1 = new Comment();
        comment1.setTask(task1);
        comment1.setTimestamp(LocalDateTime.now());
        comment1.setUser(user2);
        comment1.setText("Comment on task 1 from bob.");
        comment1.setAmountOfReplies(2);
        messages.add(commentRepository.save(comment1));

        Comment comment2 = new Comment();
        comment2.setTask(task2);
        comment2.setTimestamp(LocalDateTime.now());
        comment2.setUser(user1);
        comment2.setText("Comment on task 2 from alice.");
        messages.add(commentRepository.save(comment2));

        Reply reply1 = new Reply();
        reply1.setParent(comment1);
        reply1.setTimestamp(LocalDateTime.now());
        reply1.setUser(user1);
        reply1.setText("Reply to bob's comment on task 1 from alice.");
        
        Reply reply2 = new Reply();
        reply2.setParent(reply1);
        reply2.setTimestamp(LocalDateTime.now());
        reply2.setUser(user2);
        reply2.setText("Reply to alice's reply to comment 1 from bob.");
        reply2.setReplies(List.of());
        reply1.setReplies(List.of(reply2));
        messages.add(replyRepository.save(reply1));
        messages.add(replyRepository.save(reply2));
    }

    @Test
    @WithUserDetails("alice")
    void getCommentsForTask_ValidRequest_ListOfComments() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/messages/comments/tasks/" + tasks.get(0).getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andReturn();

        CommentResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CommentResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.length);
        assertMessageDtosEqual((CommentResponseDto)mapCommentToDto((Comment)messages.get(0)),
                actual[0]);
    }

    @Test
    @WithUserDetails("alice")
    void getCommentsForProject_ValidRequest_ListOfComments() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/messages/comments/projects/"
                + projects.get(0).getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andReturn();

        CommentWithTaskIdResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CommentWithTaskIdResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.length);
        assertMessageDtosEqual(mapCommentToDtoWithTask(
                (Comment)messages.get(0)), actual[0]);
        assertMessageDtosEqual(mapCommentToDtoWithTask(
                (Comment)messages.get(1)), actual[1]);
    }

    @Test
    @WithUserDetails("alice")
    void getCommentById_ValidRequest_Comment() throws Exception {
        MvcResult result = mockMvc.perform(get("/messages/comments/" + messages.get(0).getId()))
                .andExpect(status().isOk())
                .andReturn();

        CommentWithTaskIdResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CommentWithTaskIdResponseDto.class);

        Assertions.assertNotNull(actual);
        assertMessageDtosEqual(mapCommentToDtoWithTask(
                (Comment)messages.get(0)), actual);
    }

    @Test
    @WithUserDetails("alice")
    void getReplyById_ValidRequest_Reply() throws Exception {
        MvcResult result = mockMvc.perform(get("/messages/replies/" + messages.get(2).getId()))
                .andExpect(status().isOk())
                .andReturn();

        ReplyResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ReplyResponseDto.class);

        Assertions.assertNotNull(actual);
        assertMessageDtosEqual(mapReplyToDto((Reply)messages.get(2)), actual);
    }

    @Test
    @WithUserDetails("alice")
    void getRepliesForComment_ValidRequest_ListOfReplies() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/messages/comments/" 
                + messages.get(0).getId() + "/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andReturn();

        ReplyResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ReplyResponseDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.length);
        assertMessageDtosEqual(mapReplyToDto((Reply)messages.get(2)), actual[0]);
    }

    @Test
    @WithUserDetails("alice")
    void leaveComment_ValidRequest_NewComment() throws Exception {
        CreateMessageRequestDto requestDto = new CreateMessageRequestDto();
        requestDto.setText("New comment.");

        MvcResult result = mockMvc.perform(post("/messages/comments/tasks/"
                + tasks.get(2).getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        CommentResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CommentResponseDto.class);

        Assertions.assertNotNull(actual);
        
        Optional<Comment> potentialComment = commentRepository.findById(actual.getId());

        Assertions.assertTrue(potentialComment.isPresent());
        assertMessageDtosEqual(mapCommentToDto(
                potentialComment.get()), actual);
        Assertions.assertEquals(requestDto.getText(), actual.getText());
    }

    @Test
    @WithUserDetails("alice")
    void replyToMessage_ValidRequest_NewReply() throws Exception {
        CreateMessageRequestDto requestDto = new CreateMessageRequestDto();
        requestDto.setText("New reply.");

        final Comment newComment = createNewComment();

        MvcResult result = mockMvc.perform(post("/messages/" + newComment.getId() + "/replies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        ReplyResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), ReplyResponseDto.class);

        Assertions.assertNotNull(actual);

        Optional<Reply> potentialReply = replyRepository.findById(actual.getId());

        Assertions.assertTrue(potentialReply.isPresent());
        assertMessageDtosEqual(mapReplyToDto(potentialReply.get()), actual);
        Assertions.assertEquals(requestDto.getText(), actual.getText());
    }

    @Test
    @WithUserDetails("alice")
    void updateMessage_ValidRequest_UpdatedMessage() throws Exception {
        CreateMessageRequestDto requestDto = new CreateMessageRequestDto();
        requestDto.setText("Updated comment.");

        final Comment newComment = createNewComment();
        newComment.setText(requestDto.getText());

        MvcResult result = mockMvc.perform(put("/messages/" + newComment.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        CommentResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), CommentResponseDto.class);
        
        Assertions.assertNotNull(actual);
        assertMessageDtosEqual(mapCommentToDto(newComment), actual);
    }

    @Test
    @WithUserDetails("alice")
    void deleteMessage_ValidRequest_Ok() throws Exception {
        final Comment newComment = createNewComment();

        mockMvc.perform(delete("/messages/" + newComment.getId()))
                .andExpect(status().isNoContent())
                .andReturn();

        Optional<Comment> potentialComment = commentRepository.findById(newComment.getId());
        Assertions.assertFalse(potentialComment.isPresent());
    }

    @AfterAll
    static void clearDb(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);

            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/message/delete-messages.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/task/delete-tasks.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/project/delete-projects.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/user/delete-test-users.sql"));
        }
    }

    private static CommentResponseDto mapCommentToDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setTimestamp(comment.getTimestamp());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setAmountOfReplies(comment.getAmountOfReplies());
        return dto;
    }

    private static CommentWithTaskIdResponseDto mapCommentToDtoWithTask(Comment comment) {
        CommentWithTaskIdResponseDto dto = new CommentWithTaskIdResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setTimestamp(comment.getTimestamp());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setAmountOfReplies(comment.getAmountOfReplies());
        dto.setTaskId(comment.getTask().getId());
        return dto;
    }

    private static ReplyResponseDto mapReplyToDto(Reply reply) {
        
        ReplyResponseDto dto = new ReplyResponseDto();
        dto.setId(reply.getId());
        dto.setTimestamp(reply.getTimestamp());
        dto.setUserId(reply.getUser().getId());
        dto.setUsername(reply.getUser().getUsername());
        dto.setText(reply.getText());
        dto.setReplyDtos((reply.getReplies() == null) ? new ArrayList<>()
                : reply.getReplies().stream().map(r -> mapReplyToDto(r)).toList());
        return dto;
    }

    private Comment createNewComment() {
        Comment newComment = new Comment();
        newComment.setTask(tasks.get(2));
        newComment.setTimestamp(LocalDateTime.now());
        newComment.setUser(users.get(0));
        newComment.setText("New comment.");
        newComment.setAmountOfReplies(0);
        return commentRepository.save(newComment);
    }

    private void assertMessageDtosEqual(MessageResponseDto dto1, MessageResponseDto dto2) {
        Assertions.assertEquals(dto1.getClass(), dto2.getClass());
        Assertions.assertEquals(dto1.getId(), dto2.getId());
        Assertions.assertEquals(dto1.getUserId(), dto2.getUserId());
        Assertions.assertEquals(dto1.getText(), dto2.getText());
        Assertions.assertEquals(dto1.getUsername(), dto2.getUsername());
        if (dto1 instanceof CommentResponseDto) {
            CommentResponseDto dto1Comment = (CommentResponseDto)dto1;
            CommentResponseDto dto2Comment = (CommentResponseDto)dto2;
            Assertions.assertEquals(dto1Comment.getAmountOfReplies(),
                    dto2Comment.getAmountOfReplies());
            if (dto1 instanceof CommentWithTaskIdResponseDto) {
                CommentWithTaskIdResponseDto dto1CommentWithTask =
                        (CommentWithTaskIdResponseDto)dto1;
                CommentWithTaskIdResponseDto dto2CommentWithTask =
                        (CommentWithTaskIdResponseDto)dto2;
                Assertions.assertEquals(dto1CommentWithTask.getTaskId(),
                        dto2CommentWithTask.getTaskId());
            }
        } else {
            ReplyResponseDto dto1Reply = (ReplyResponseDto)dto1;
            ReplyResponseDto dto2Reply = (ReplyResponseDto)dto2;
            for (int i = 0; i < dto1Reply.getReplyDtos().size(); i++) {
                assertMessageDtosEqual(dto1Reply.getReplyDtos().get(i),
                        dto2Reply.getReplyDtos().get(i));
            }
        }
    }
}
