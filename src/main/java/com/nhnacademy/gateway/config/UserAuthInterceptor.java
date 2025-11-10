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


            if(principal instanceof CustomUserPrincipal){
                CustomUserPrincipal userPrincipal = (CustomUserPrincipal) principal;
                Long numericId = userPrincipal.getNumericId();

                if(numericId != null){
                    request.getHeaders().set(USER_ID_HEADER, String.valueOf(numericId));
                }

            }else if (principal instanceof  String){
                request.getHeaders().set(USER_ID_HEADER, (String) principal);
            }


        }
        return execution.execute(request,body);
    }
}
