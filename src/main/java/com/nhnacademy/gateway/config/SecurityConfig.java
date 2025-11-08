package com.nhnacademy.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthenticationProvider;

    public SecurityConfig(CustomAuthenticationProvider customAuthenticationProvider) {
        this.customAuthenticationProvider = customAuthenticationProvider;
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        // 1. 요청 인가 설정
        http.authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/join").permitAll()
                        .requestMatchers("/login/process").permitAll()
                        .requestMatchers("/members/register").permitAll()

                        .anyRequest().authenticated()
        );

        http.csrf(AbstractHttpConfigurer::disable); //csrf 방어목적
        http.authenticationProvider(customAuthenticationProvider);

        /*
        formLogin 설정
         */
        http.formLogin((formLogin) ->
                formLogin.loginPage("/login")
                        .usernameParameter("id")
                        .passwordParameter("password")
                        .loginProcessingUrl("/login/process")
        );

        http.logout(logout ->logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION")

                );

        http.servletApi(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
