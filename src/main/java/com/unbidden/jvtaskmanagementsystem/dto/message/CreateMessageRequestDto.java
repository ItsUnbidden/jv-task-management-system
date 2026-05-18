package com.unbidden.jvtaskmanagementsystem.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMessageRequestDto {
    @NotBlank
    @Size(max = 2000)
    private String text;
}
