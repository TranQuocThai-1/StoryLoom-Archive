package com.storyloom.archive.controller;

import com.storyloom.archive.service.AnnotationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/annotations")
public class AnnotationController {

    @Autowired
    private AnnotationService annotationService;

    @PostMapping("/save")
    public String saveAnnotation(@RequestParam("bookId") Long bookId, 
                                 @RequestParam("content") String content, 
                                 Authentication authentication,
                                 HttpServletRequest request) { 
                                     
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        annotationService.saveOrUpdateAnnotation(authentication.getName(), bookId, content);

        String referer = request.getHeader("Referer");
        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/book/" + bookId; 
    }
}