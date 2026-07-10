package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.calendar;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EmailResult extends CalendarOperationResult {
    private final String email;

    public EmailResult(ThirdPartyOperationStatus status) {
        super(status);
        this.email = null;
    }

    public EmailResult(ThirdPartyOperationStatus status, String email) {
        super(status);
        this.email = email;
    }
}
