package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.calendar;

import com.unbidden.jvtaskmanagementsystem.dto.thirdparty.ThirdPartyOperationResult;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarOperationResult extends ThirdPartyOperationResult {
    public CalendarOperationResult(ThirdPartyOperationStatus status) {
        super(status);
    }
}
