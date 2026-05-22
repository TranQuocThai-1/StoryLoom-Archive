package com.storyloom.archive.controller;

import com.storyloom.archive.model.User;
import com.storyloom.archive.model.UserRegistrationDto;
import com.storyloom.archive.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "expired", required = false) String expired,
            @RequestParam(value = "ratelimit", required = false) String ratelimit, 
            Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password.");
        }
        
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! You can now log in.");
        }

        if (expired != null) {
            model.addAttribute("errorMessage", "Your session has expired or you logged in from another device.");
        }


        if (ratelimit != null) {
            model.addAttribute("errorMessage", "Too many login attempts. Please try again in 1 minute.");
        }
        
        return "login";
    }

    @GetMapping("/register")
    public String showSignupForm() {
        return "signup";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute UserRegistrationDto userDto, 
            BindingResult result, 
            Model model) {

        if (result.hasErrors()) {
            String validationError = result.getAllErrors().get(0).getDefaultMessage();
            model.addAttribute("errorMessage", validationError);
            return "signup"; 
        }

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            model.addAttribute("errorMessage", "An account with that email already exists.");
            return "signup";
        }

        if (userRepository.findByScreenName(userDto.getScreenName()).isPresent()) {
            model.addAttribute("errorMessage", "That screen name is already taken.");
            return "signup";
        }

        User newUser = new User();
        newUser.setEmail(userDto.getEmail());
        newUser.setScreenName(userDto.getScreenName());
        
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword())); 

        userRepository.save(newUser);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/privacy")
    public String showPrivacyPage() { return "privacy"; }

    @GetMapping("/permissions")
    public String showPermissionsPage() { return "permissions"; }

    @GetMapping("/terms")
    public String showTermsPage() { return "terms"; }

    @GetMapping("/contact")
    public String showContactPage() { return "contact"; }

    @GetMapping("/faq")
    public String showFaqPage() { return "faq"; }
}