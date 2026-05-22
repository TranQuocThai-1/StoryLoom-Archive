package com.storyloom.archive.controller;

import com.storyloom.archive.model.User;
import com.storyloom.archive.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("currentUser")
    public String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<User> user = userRepository.findByEmail(email);
            if (user.isPresent()) {
                return user.get().getScreenName();
            }
        }
        return null; 
    }

    @ModelAttribute("_csrf")
    public Map<String, String> csrfTokenMap(HttpServletRequest request) {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        Map<String, String> csrfMap = new HashMap<>();
        
        if (csrf != null) {
            csrfMap.put("parameterName", csrf.getParameterName());
            csrfMap.put("token", csrf.getToken());
        }
        return csrfMap;
    }

    @ModelAttribute("_csrfToken")
    public String getCsrfTokenString(HttpServletRequest request) {
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrf != null) {
            return csrf.getToken();
        }
        return ""; 
    }
}