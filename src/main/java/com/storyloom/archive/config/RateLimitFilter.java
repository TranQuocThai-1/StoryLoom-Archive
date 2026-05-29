package com.storyloom.archive.config;

import com.storyloom.archive.service.RateLimitingService; 
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

        if (httpRequest.getRequestURI().equals("/login") && httpRequest.getMethod().equalsIgnoreCase("POST")) {
            
            String ip = getClientIP(httpRequest);
            Bucket bucket = rateLimitingService.resolveBucket(ip);

            if (bucket.tryConsume(1)) {
                chain.doFilter(request, response);
            } else {

                httpResponse.sendRedirect("/login?ratelimit=true");
                return;
            }
        } else {

            chain.doFilter(request, response);
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}