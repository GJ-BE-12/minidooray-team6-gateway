package com.nhnacademy.gateway.controller;

import com.nhnacademy.gateway.dto.AccountRegisterRequest;
import com.nhnacademy.gateway.service.AccountService;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
@Slf4j
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    @GetMapping("/join")
    public String joinForm(Model model){
        if(!model.containsAttribute("request")){
            model.addAttribute("request", new AccountRegisterRequest());
        }

        return "join";
    }

    @PostMapping("/join")
    public String register(@ModelAttribute AccountRegisterRequest request, Model model){
        try{
            boolean success = accountService.registerAccount(request);

            if(success){
                return "redirect:/login?message=success";
            }else{
                model.addAttribute("error", "회원가입 처리 중 알 수없는 오류가 발생했습니다.");
                model.addAttribute("request", request);
                return "forward:/join";
            }


        }catch (IllegalArgumentException ex){
            log.warn("register failed (client): {}", ex.getMessage());
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("request", request);
            return "forward:/join";
        } catch (RuntimeException e) {
            log.error("register failed (server): {}", e.getMessage());
            model.addAttribute("error", "회원가입 중 서버 오류 발생");
            model.addAttribute("request", request);
            return "forward:/join";
        }
    }
}
