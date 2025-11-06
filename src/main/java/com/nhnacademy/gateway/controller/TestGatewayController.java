package com.nhnacademy.gateway.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller // View를 반환할 것이므로 @Controller를 사용합니다.
@RequiredArgsConstructor
public class TestGatewayController {
    
    private final RestTemplate accountRestTemplate;
    
    // yml에서 TaskApi 주소를 가져옵니다.
    @Value("${api.task.url}")
    private String taskApiUrl;

    @GetMapping("/test/task-communication")
    public String testCommunication(Model model) {
        String result;
        
        try {
            // TaskApi의 /test/ping 엔드포인트 호출
            String url = taskApiUrl + "/test/ping";
            
            // TaskApi로부터 String 응답을 받습니다.
            ResponseEntity<String> response = accountRestTemplate.getForEntity(url, String.class);
            
            // 통신 성공
            result = "✅ 통신 성공 (상태코드: " + response.getStatusCode() + "): " + response.getBody();
            
        } catch (Exception e) {
            // 통신 실패 (TaskApi 서버가 꺼져있거나 주소가 틀렸을 때)
            result = "❌ 통신 실패: " + e.getMessage();
        }

        model.addAttribute("testResult", result);

        return "testPage"; // testPage.html이라는 Thymeleaf 템플릿으로 결과를 전달
    }
}