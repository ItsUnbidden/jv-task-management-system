package com.unbidden.jvtaskmanagementsystem.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMessageRequestDto {
    @NotBlank
    @Size(min = 1, max = 255)
    private String text;
}
