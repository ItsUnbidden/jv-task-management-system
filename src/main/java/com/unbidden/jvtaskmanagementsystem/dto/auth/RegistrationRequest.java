package com.unbidden.jvtaskmanagementsystem.dto.auth;

import com.unbidden.jvtaskmanagementsystem.validation.ApplyMatching;
import com.unbidden.jvtaskmanagementsystem.validation.FieldMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@FieldMatch
public class RegistrationRequest {
    @NotBlank
    @Size(min = 5, max = 25)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    @ApplyMatching
    private String password;

    @NotBlank
    @Size(min = 6, max = 100)
    @ApplyMatching
    private String repeatPassword;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
