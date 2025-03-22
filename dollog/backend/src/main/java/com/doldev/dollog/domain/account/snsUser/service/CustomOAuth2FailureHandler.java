package com.doldev.dollog.domain.account.snsUser.service;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res, AuthenticationException exception)
            throws IOException, ServletException {
                
        String msg = exception.getMessage();

        if (msg != null && msg.startsWith("ACCOUNT_DISABLED:")) {
            String blockedUsername = msg.substring("ACCOUNT_DISABLED:".length());
            String redirectUrl = "http://localhost:3000/login?error=ACCOUNT_DISABLED&username=" + blockedUsername;
            res.sendRedirect(redirectUrl);
        }  else {
            String redirectUrl = "http://localhost:3000/login?error=oauth_failed";
            res.sendRedirect(redirectUrl);
        }
    }
}
