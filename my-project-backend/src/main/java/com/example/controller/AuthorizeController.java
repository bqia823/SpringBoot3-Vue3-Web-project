package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.ConfirmResetVO;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.entity.vo.request.EmailResetVO;
import com.example.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

/**
 * Controller for handling validation-related actions, including user registration, password reset, etc.
 */
@Validated
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Login Verification Related", description = "Includes operations such as user login, registration, and verification code requests.")
public class AuthorizeController {

    @Resource
    AccountService accountService;

    /**
     * Request email verification code
     * @param email the email to send the verification code to
     * @param type the type of request
     * @param request the HTTP request
     * @return whether the request was successful
     */
    @GetMapping("/ask-code")
    @Operation(summary = "Request email verification code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset)")  String type,
                                        HttpServletRequest request){
        return this.messageHandle(() ->
                accountService.registerEmailVerifyCode(type, String.valueOf(email), request.getRemoteAddr()));
    }

    /**
     * Perform user registration, email verification code is required first
     * @param vo registration information
     * @return whether the registration was successful
     */
    @PostMapping("/register")
    @Operation(summary = "User registration")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterVO vo){
        return this.messageHandle(() ->
                accountService.registerEmailAccount(vo));
    }

    /**
     * Confirm password reset by checking the verification code
     * @param vo password reset information
     * @return whether the operation was successful
     */
    @PostMapping("/reset-confirm")
    @Operation(summary = "Password reset confirmation")
    public RestBean<Void> resetConfirm(@RequestBody @Valid ConfirmResetVO vo){
        return this.messageHandle(() -> accountService.resetConfirm(vo));
    }

    /**
     * Perform password reset
     * @param vo password reset information
     * @return whether the operation was successful
     */
    @PostMapping("/reset-password")
    @Operation(summary = "Password reset")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVO vo){
        return this.messageHandle(() ->
                accountService.resetEmailAccountPassword(vo));
    }

    /**
     * Handles methods returning a String as an error message
     * @param action the specific operation
     * @return the response result
     * @param <T> the response result type
     */
    private <T> RestBean<T> messageHandle(Supplier<String> action){
        String message = action.get();
        if(message == null)
            return RestBean.success();
        else
            return RestBean.failure(400, message);
    }
}
