package com.unbidden.jvtaskmanagementsystem.controller;

import com.dropbox.core.v2.check.EchoResult;
import com.unbidden.jvtaskmanagementsystem.model.User;
import com.unbidden.jvtaskmanagementsystem.service.DropboxService;
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
@RequestMapping("/dropbox")
@Tag(name = "Exclusively dropbox related methods")
public class DropboxController {
    private final DropboxService dropboxService;

    @GetMapping("/test")
    @Operation(
            summary = "Initiate dropbox calendar test",
            description = "Uses standart dropbox test endpoint",
            responses = {
                @ApiResponse(
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = EchoResult.class)),
                    responseCode = "200",
                    description = "Dropbox response"),
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
    public EchoResult test(Authentication authentication) {
        return dropboxService.testDropboxUserConnection((User)authentication.getPrincipal());
    }

    @DeleteMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Completely logout from dropbox",
            description = "Revokes all tokens and deletes local authorized client. "
                + "If tokens cannot be revoked for some reason, a deadlock CAN "
                + "occur in the current implementation and user won't be able to logout in any way",
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
        dropboxService.logout((User)authentication.getPrincipal());
    }
}
