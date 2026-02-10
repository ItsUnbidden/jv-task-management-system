package com.unbidden.jvtaskmanagementsystem.security;

import java.util.regex.Pattern;

import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.unbidden.jvtaskmanagementsystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CustomUserDetailsService implements UserDetailsService {
    private static final String REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)"
            + "*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    private final UserRepository repository;

    @NonNull
    @Override
    public UserDetails loadUserByUsername(@NonNull String input)
            throws UsernameNotFoundException {
        return isEmail(input) ? repository.findByEmail(input).orElseThrow(() -> 
                new UsernameNotFoundException("Can't find user by email."))
                : repository.findByUsername(input).orElseThrow(() -> 
                new UsernameNotFoundException("There is no user with such username."));
    }

    private boolean isEmail(@NonNull String username) {
        return PATTERN.matcher(username).matches();
    }   
}
