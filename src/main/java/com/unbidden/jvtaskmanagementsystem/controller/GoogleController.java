package com.unbidden.jvtaskmanagementsystem.controller;

import com.unbidden.jvtaskmanagementsystem.dto.google.GoogleSuccessfulTestResponseDto;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.GoogleCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/google")
@Tag(name = "Exclusively google related methods")
public class GoogleController {
    private final GoogleCalendarService calendarService;
    
    @GetMapping("/test")
    @Operation(
            summary = "Initiate google calendar test",
            description = "Google calendar has no official test endpoint. Therefore this"
                + " method does some testing manualy: 1) Creates a calendar 2) Creates an event "
                + "3) Deletes the event 4) Deletes the calendar",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = GoogleSuccessfulTestResponseDto.class)),
                    responseCode = "200",
                    description = "Success message"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Some issue occurred. This is not necessarily a user "
                            + "problem but can be basicaly anything"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized. Might be some other issue as well")
            }
    )
    public GoogleSuccessfulTestResponseDto test(Authentication authentication) {
        return calendarService.test((User)authentication.getPrincipal());
    }

    @DeleteMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Completely logout from google",
            description = "Revokes all tokens and deletes local authorized client. "
                + "If tokens cannot be revoked for some reason, to prevent deadlock"
                + " just authorized client will be deleted",
            responses = {
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "204",
                    description = "Successful logout"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "400",
                    description = "Some issue occurred. This is not necessarily a user "
                            + "problem but can be basicaly anything"),
                @ApiResponse(
                    content = @Content(schema = @Schema(hidden = true)),
                    responseCode = "401",
                    description = "Unauthorized. Might be some other issue as well")
            }
    )
    public void logout(Authentication authentication) {
        calendarService.logout((User)authentication.getPrincipal());
    }
}
