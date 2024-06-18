package com.unbidden.jvtaskmanagementsystem.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginRequestDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.LoginResponseDto;
import com.unbidden.jvtaskmanagementsystem.dto.auth.RegistrationRequest;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.Role.RoleType;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.repository.RoleRepository;
import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import javax.crypto.SecretKey;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {
    protected static MockMvc mockMvc;

    private static UserRepository userRepository;

    private static RoleRepository roleRepository;

    private static PasswordEncoder passwordEncoder;

    private static User user;

    private static SecretKey secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Autowired 
    private ObjectMapper objectMapper;

    @BeforeAll
    static void init(@Autowired WebApplicationContext applicationContext,
            @Value("${jwt.secret}") String secretString) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();

        userRepository = applicationContext.getBean(UserRepository.class);
        roleRepository = applicationContext.getBean(RoleRepository.class);
        passwordEncoder = applicationContext.getBean(PasswordEncoder.class);

        secret = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

        user = new User();
        user.setEmail("testUser@tms.com");
        user.setUsername("testUser");
        user.setFirstName("testUser");
        user.setLastName("testUser");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRoles(Set.of(roleRepository.findAll().stream()
                .filter(r -> r.getRoleType().equals(RoleType.USER))
                .toList()
                .get(0)));
        user = userRepository.save(user);
    }

    @Test
    void register_ValidRequest_NewUser() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("newUser@tms.com");
        request.setFirstName("newUser");
        request.setLastName("newUser");
        request.setPassword("password321");
        request.setRepeatPassword("password321");
        request.setUsername("newUser");

        MvcResult result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponseDto.class);

        Assertions.assertNotNull(actual);

        Optional<User> potentialUser = userRepository.findById(actual.getId());
        Assertions.assertTrue(potentialUser.isPresent());

        Assertions.assertEquals(request.getEmail(), request.getEmail());
        Assertions.assertEquals(request.getFirstName(), request.getFirstName());
        Assertions.assertEquals(request.getLastName(), request.getLastName());
        Assertions.assertEquals(request.getPassword(), request.getPassword());
        Assertions.assertEquals(request.getUsername(), request.getUsername());
    }

    @Test
    void login_ValidRequest_Token() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setUsername("testUser");
        requestDto.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), LoginResponseDto.class);

        Assertions.assertNotNull(actual);

        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(secret)
                    .build()
                    .parseSignedClaims(actual.token());
            Assertions.assertTrue(claimsJws.getPayload().getExpiration().after(new Date()));
        } catch (JwtException | IllegalArgumentException e) {
            Assertions.assertTrue(false);
        }
    }

    @AfterAll
    static void clearDb(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("db/user/delete-test-users.sql"));
        }
    }
}
