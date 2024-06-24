package com.unbidden.jvtaskmanagementsystem.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserUpdateDetailsRequestDto;
import com.unbidden.jvtaskmanagementsystem.model.Role;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.RoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    protected static MockMvc mockMvc;
    
    private static UserRepository userRepository;

    private static RoleRepository roleRepository;

    private static User testUser;

    private static UserResponseDto testUserDto;

    private static PasswordEncoder encoder;

    @Autowired 
    private ObjectMapper objectMapper;

    @BeforeAll
    static void init(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        
        userRepository = applicationContext.getBean(UserRepository.class);
        roleRepository = applicationContext.getBean(RoleRepository.class);
        encoder = applicationContext.getBean(PasswordEncoder.class);

        createDefautTestUser();
    }

    @BeforeEach
    void resetDto() {
        testUserDto = new UserResponseDto();
        testUserDto.setId(testUser.getId());
        testUserDto.setEmail(testUser.getEmail());
        testUserDto.setFirstName(testUser.getFirstName());
        testUserDto.setLastName(testUser.getLastName());
        testUserDto.setUsername(testUser.getUsername());
        testUserDto.setRoles(testUser.getRoles());
        testUserDto.setLocked(testUser.isLocked());
    }

    @Test
    void noMethod_WhetherOwnerHasBeenCreated_Success() {
        List<User> usersFromDb = userRepository.findAll();
        List<User> filtered = usersFromDb.stream()
                .filter(user -> user.getUsername().equals("owner") 
                && user.getEmail().equals("owner@taskmanagement.com"))
                .toList();
        Assertions.assertEquals(1, filtered.size());
    }

    @Test
    void noMethod_WhetherRolesHaveBeenCreated_Success() {
        List<Role> rolesFromDb = roleRepository.findAll();
        Assertions.assertEquals(3, rolesFromDb.size());
    }

    @Test
    @WithUserDetails("testUser")
    void getCurrentUser_validRequest_CurrentUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                UserResponseDto.class);
        
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(testUserDto, actual);
    }

    @Test
    @WithUserDetails("owner")
    void updateRoles_TestUserToManager_UpdatedUser() throws Exception {
        final Role managerRole = roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.MANAGER))
                .toList()
                .get(0);
        testUserDto.setRoles(Set.of(managerRole));

        MvcResult result = mockMvc.perform(patch("/users/" + testUser.getId() + "/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Set.of(managerRole))))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(testUserDto, actual);
    }

    @Test
    @WithUserDetails("testUser")
    void updateUserDetails_WithUpdatedDetails_UpdatedUser() throws Exception {
        UserUpdateDetailsRequestDto requestDto = new UserUpdateDetailsRequestDto();
        requestDto.setUsername("updatedTestUser");
        requestDto.setFirstName("updatedFirstName");
        requestDto.setLastName("updatedLastName");
        requestDto.setPassword("newPassword123");
        requestDto.setRepeatPassword("newPassword123");
        testUserDto.setUsername(requestDto.getUsername());
        testUserDto.setFirstName(requestDto.getFirstName());
        testUserDto.setLastName(requestDto.getLastName());

        MvcResult result = mockMvc.perform(put("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(testUserDto, actual);
    }

    @Test
    @WithUserDetails("owner")
    void getAllUsers_ValidRequest_UserList() throws Exception {
        final Pageable pageable = PageRequest.of(0, 10);

        MvcResult result = mockMvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pageable)))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto[].class);
        
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(2, actual.length);
    }

    @Test
    @WithUserDetails("testUser")
    void deleteCurrentUser_ValidRequest_Ok(@Autowired DataSource dataSource) throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setUsername("testUser");
        requestDto.setPassword("password123");

        mockMvc.perform(delete("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNoContent())
                .andReturn();
        List<User> potentialUser = userRepository.findAll().stream()
                .filter(u -> u.getUsername().equals("testUser"))
                .toList();
        
        Assertions.assertNotNull(potentialUser);
        Assertions.assertTrue(potentialUser.isEmpty());

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/user/delete-test-users.sql"));
        }
    }

    @Test
    @WithUserDetails("owner")
    void changeLockedStatus_ValidRequest_UpdatedUser() throws Exception {
        testUserDto.setLocked(true);

        MvcResult result = mockMvc.perform(patch("/users/" + testUser.getId() + "/lock"))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);
        
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(testUserDto, actual);
    }

    @AfterEach
    void resetUser() {
        createDefautTestUser();
    }

    @AfterAll
    static void clearDb(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/user/delete-test-users.sql"));
        }
    }

    private static void createDefautTestUser() {
        Long id = null;
        if (testUser != null) {
            id = testUser.getId();
        }
        testUser = new User();
        testUser.setId(id);
        testUser.setEmail("user123@test.com");
        testUser.setFirstName("testName");
        testUser.setLastName("testLastName");
        testUser.setPassword(encoder.encode("password123"));
        testUser.setUsername("testUser");
        Role userRole = roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0);
        testUser.setRoles(Set.of(userRole));
        testUser = userRepository.save(testUser);
    }
}
