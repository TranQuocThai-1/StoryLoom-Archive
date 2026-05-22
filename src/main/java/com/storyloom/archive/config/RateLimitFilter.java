package com.storyloom.archive.config;

import com.storyloom.archive.service.RateLimitingService; // This import links to the file above!
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {

    @Autowired
    private RateLimitingService rateLimitingService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // We ONLY care about someone hitting the "Submit" button on the login page (a POST request)
        if (httpRequest.getRequestURI().equals("/login") && httpRequest.getMethod().equalsIgnoreCase("POST")) {
            
            String ip = getClientIP(httpRequest);
            Bucket bucket = rateLimitingService.resolveBucket(ip);

            // tryConsume(1) takes 1 token. If it returns true, let them pass.
            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {
                // Out of tokens! Kick them back to the login page with a warning.
                httpResponse.sendRedirect("/login?ratelimit=true");
                return;
            }
        } else {
            // Not a login attempt? Let them browse the site normally.
            chain.doFilter(request, response);
        }
    }

    // Helper method to get the user's real IP address
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}