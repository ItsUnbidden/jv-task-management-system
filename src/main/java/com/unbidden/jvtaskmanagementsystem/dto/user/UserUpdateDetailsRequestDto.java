package com.unbidden.jvtaskmanagementsystem.dto.user;

import com.unbidden.jvtaskmanagementsystem.validation.ApplyMatching;
import com.unbidden.jvtaskmanagementsystem.validation.FieldMatch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@FieldMatch
public class UserUpdateDetailsRequestDto {
    @NotBlank
    @Size(min = 5, max = 25)
    private String username;

    @NotBlank
    @Email
    private String email;

    @ApplyMatching
    @Size(min = 8, max = 100)
    private String password;

    @ApplyMatching
    @Size(min = 8, max = 100)
    private String repeatPassword;
}
