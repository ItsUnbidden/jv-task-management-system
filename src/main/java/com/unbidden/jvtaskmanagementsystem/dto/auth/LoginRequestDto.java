package com.unbidden.jvtaskmanagementsystem.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank
    @Size(min = 5, max = 25)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}
