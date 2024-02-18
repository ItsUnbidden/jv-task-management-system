package com.unbidden.jvtaskmanagementsystem.service;

import com.unbidden.jvtaskmanagementsystem.dto.auth.RegistrationRequest;
import com.unbidden.jvtaskmanagementsystem.dto.user.UserResponseDto;
import com.unbidden.jvtaskmanagementsystem.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(RegistrationRequest request) throws RegistrationException;
}
