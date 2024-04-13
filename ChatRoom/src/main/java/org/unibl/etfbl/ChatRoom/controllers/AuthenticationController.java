package org.unibl.etfbl.ChatRoom.controllers;


import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.unibl.etfbl.ChatRoom.advices.ExceptionLoggingAdvice;
import org.unibl.etfbl.ChatRoom.models.dtos.AuthRequest;
import org.unibl.etfbl.ChatRoom.security.auth.AuthenticationService;


@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private ExceptionLoggingAdvice exceptionLoggingAdvice;

// TODO logout
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            authenticationService.login(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }

    }

    @PostMapping("/token")
    public ResponseEntity<?> verifyTwoFactorToken(@RequestBody @Valid AuthRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed");
        try {
            return ResponseEntity.ok(authenticationService.verifyTwoFactorToken(request));
        } catch (Exception e) {
            exceptionLoggingAdvice.afterThrowing(e);
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }

    @GetMapping("/oauth2")
    public String oauth2() {

        return "redirect:" ;
    }



    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}


