package com.nhnacademy.gateway.controller;

import com.nhnacademy.gateway.dto.basic.AccountDto;
import com.nhnacademy.gateway.dto.create.AccountRegisterRequest;
import com.nhnacademy.gateway.dto.update.AccountUpdateRequest;
import com.nhnacademy.gateway.dto.update.PasswordUpdateRequest;
import com.nhnacademy.gateway.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@Controller
@Slf4j
public class AccountController {
    private final AccountService accountService;
    private final SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    @GetMapping("/users") //회원가입 폼
    public String joinForm(Model model){
        if(!model.containsAttribute("request")){
            model.addAttribute("request", new AccountRegisterRequest());
        }

        return "join";
    }

    @PostMapping("/users") // 회원가입
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

    @GetMapping("/users/{userId}")
    public String getAccount(@PathVariable String userId, Model model){
        try{
            AccountDto account = accountService.getAccountDetails(userId);
            model.addAttribute("user", account);
            return "userProfile";
        }catch (HttpClientErrorException.NotFound ex){
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "errorPage";
        }
    }

    @GetMapping("/user/{userId}/edit")
    public String updateAccountForm(@PathVariable String userId, Model model){
        try{
            AccountDto account = accountService.getAccountDetails(userId);
            model.addAttribute("user", account);
            return "userProfileUpdate";
        }catch (HttpClientErrorException.NotFound ex){
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "errorPage";
        }
    }

    @PutMapping("/user/{userId}")
    public String updateAccount(@PathVariable String userId, @ModelAttribute("user") AccountDto userDto,
                                Model model){
        AccountUpdateRequest updateRequest = new AccountUpdateRequest(userDto.getEmail());

        try{
            accountService.updateAccount(userId, updateRequest);
            return "redirect:/users/"+ userId;
        }catch (IllegalArgumentException | HttpClientErrorException ex){
            log.warn("유저 정보 변경 실패 (client): {}", ex.getMessage());
            model.addAttribute("error", "정보 변경에 실패했습니다. ");
            return "userProfileUpdate";
        }catch (Exception e){
            log.error("유저 정보 변경 실패 (server): {}", e.getMessage());
            model.addAttribute("error", "서버 오류로 비밀번호 변경 실패");
            return "errorPage";
        }
    }

    @GetMapping("/users/{userId}/password")
    public String showPasswordEditForm(@PathVariable String userId, Model model){
        model.addAttribute("request", new PasswordUpdateRequest());
        model.addAttribute("userId", userId);
        return "passwordEditForm";
    }

    @PutMapping("/users/{userId}/password")
    public String updateAccountPassword(@PathVariable String userId,
                                        @ModelAttribute("request") PasswordUpdateRequest request,
                                        Model model){
        try{
            accountService.updatePassword(userId, request);
            return "redirect:/users/" + userId;
        }catch (IllegalArgumentException | HttpClientErrorException ex){
            log.warn("비밀번호 변경 실패 (client): {}", ex.getMessage());
            model.addAttribute("userId", userId);
            model.addAttribute("error", "비밀번호 변경에 실패했습니다. ");
            return "passwordEditForm";
        }catch (Exception e){
            log.error("비밀번호 변경 실패 (server): {}", e.getMessage());
            model.addAttribute("error", "서버 오류로 비밀번호 변경 실패");
            return "errorPage";
        }
    }

    @DeleteMapping("users/{userId}")
    public String deleteAccount (@PathVariable String userId, HttpServletRequest req, HttpServletResponse response,
                                 Model model){
        try{
            accountService.deleteAccount(userId);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication !=null){
                this.logoutHandler.logout(req,response, authentication);
            }
            return "redirect:/login?message=deleted";
        }catch (Exception ex){
            log.error("account 삭제 실패 (server): {}", ex.getMessage());
            model.addAttribute("error", "서버오류로 회원 탈퇴 실패");
            return "errorPage";
        }
    }
}
