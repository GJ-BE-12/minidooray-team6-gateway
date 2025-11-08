package com.nhnacademy.gateway.config;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

public class UserAuthInterceptor implements ClientHttpRequestInterceptor {
    public static final String USER_ID_HEADER = "X-USER-ID";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication !=null && authentication.isAuthenticated()){
            Object principal = authentication.getPrincipal();
            String userId = null;

            if(principal instanceof UserDetails){
                userId =((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                userId = (String) principal;
            }

            if(userId != null){
                request.getHeaders().set(USER_ID_HEADER, userId);
            }
        }
        return execution.execute(request,body);
    }
}
