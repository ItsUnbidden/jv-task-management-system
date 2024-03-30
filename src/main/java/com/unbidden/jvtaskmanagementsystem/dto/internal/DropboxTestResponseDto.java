package com.unbidden.jvtaskmanagementsystem.dto.internal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DropboxTestResponseDto extends AbstractDropboxResponseDto {
    private String result;
}
