package com.unbidden.jvtaskmanagementsystem.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDetailsRequestDto {
    @NotBlank
    @Size(min = 5, max = 25)
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
