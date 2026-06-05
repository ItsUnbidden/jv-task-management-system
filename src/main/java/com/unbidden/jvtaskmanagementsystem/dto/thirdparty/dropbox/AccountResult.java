package com.unbidden.jvtaskmanagementsystem.dto.thirdparty.dropbox;

import org.springframework.lang.NonNull;

import com.dropbox.core.v2.users.FullAccount;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AccountResult extends DropboxOperationResult {
    private final FullAccount account;

    public AccountResult(@NonNull ThirdPartyOperationStatus status) {
        super(status);
        this.account = null;
    }

    public AccountResult(@NonNull ThirdPartyOperationStatus status, DropboxErrorTag tag, String errorMessage) {
        super(status, tag, errorMessage);
        this.account = null;
    }

    public AccountResult(@NonNull ThirdPartyOperationStatus status, FullAccount account) {
        super(status);
        this.account = account;
    }
}
