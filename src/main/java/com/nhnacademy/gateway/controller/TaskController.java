//package com.nhnacademy.gateway.controller;
//
//import com.nhnacademy.gateway.service.DataAggregationService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
//import java.util.Map;
//
//@Controller
//@Slf4j
//public class TaskController {
//    private final DataAggregationService aggregationService;
//
//    public TaskController(DataAggregationService aggregationService) {
//        this.aggregationService = aggregationService;
//    }
//
//    private String getCurrentUserId(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(authentication != null && authentication.getPrincipal() instanceof UserDetails){
//            return ((UserDetails) authentication.getPrincipal()).getUsername();
//        }else if (authentication != null && authentication.getPrincipal() instanceof String){
//            return (String)authentication.getPrincipal();
//        }
//        return null;
//    }
//
//    @GetMapping("/")
//    public String dashboard(Model model){
//        String userId = getCurrentUserId();
//
//        if(userId == null){
//            return "redirect:/login";
//        }
//
//        Map<String, Object> data = aggregationService.getDashboardData(userId);
//
//        model.addAttribute("userId", userId);
//        model.addAllAttributes(data);
//        return "dashboard";
//    }
//
//    @GetMapping("/projects/{projectId}")
//}
