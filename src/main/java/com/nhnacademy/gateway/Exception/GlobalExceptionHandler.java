package com.nhnacademy.gateway.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@ControllerAdvice("com.nhnacademy.gateway.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public String handleNotFoundException(HttpClientErrorException.NotFound ex, Model model){
        log.warn("리소스(404)를 찾을 수 없습니다: {}", ex.getMessage());
        model.addAttribute("error", "요청하신 정보를 찾을 수 없습니다.");
        model.addAttribute("status", 404);
        return "errorPage";
    }

    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception ex, Model model){
        log.error("서버 내부 오류 발생: {}", ex.getMessage(), ex);
        model.addAttribute("error", "서버 내부 오류가 발생했습니다. 관리자에게 문의하세요");
        model.addAttribute("status", 500);
        return "errorPage";
    }
}
