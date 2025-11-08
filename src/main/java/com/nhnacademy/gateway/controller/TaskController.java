package com.nhnacademy.gateway.controller;

import com.nhnacademy.gateway.dto.create.ProjectCreateRequest;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
@Slf4j
public class TaskController {
    private final DataAggregationService aggregationService;

    public TaskController(DataAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    private String getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof UserDetails){
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }else if (authentication != null && authentication.getPrincipal() instanceof String){
            return (String)authentication.getPrincipal();
        }
        return null;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        String userId = getCurrentUserId();

        if (userId == null) {
            return "redirect:/login";
        }

        Map<String, Object> data = aggregationService.getDashboardData(userId);
        /*
        data={
        "account" : accountInfo(AccountDto)
        "accountError" : 회원 정보를 불러왔으나 데이터가 비어있습니다.
        "projects" : {List<ProjectDto>}
        "projectError" : 프로젝트 목록을 불러오는 데 실패했습니다.
        }
         */

        model.addAttribute("userId", userId);
        model.addAllAttributes(data);
        return "dashboard";
    }

    @GetMapping("/projects/{projectId}")
    public String projectDetails(@PathVariable Long projectId, Model model){
        String userId = getCurrentUserId();
        if(userId == null){
            return "redirect:/login";
        }

        try{
            ProjectDetailsDto projectDetails = aggregationService.getProjectDetails(projectId);
            model.addAttribute("projectDetails", projectDetails);
            model.addAttribute("userId", userId);

            //TODO: 해당 유저가 admin인지 확인하는게 필요함
//            if(projectDetails)
//            model.addAttribute("isAdmin", true);
            return "projectDetails";
        } catch (Exception e) {
            log.error("프로젝트 디테일을 가져오는데 실패했습니다 : {}", e.getMessage());
            return "redirect:/?error=project_not_found";
        }
    }

    @GetMapping("/projects/register")
    public String createProjectForm(Model model){
        model.addAttribute("request", new ProjectCreateRequest());
        return "projectCreate";
    }

    @PostMapping("/projects")
    public String createProject(@ModelAttribute ProjectCreateRequest request){
        String userId = getCurrentUserId();
        if(userId == null){
            return "redirect:/login";
        }

        try{
            aggregationService.createProject(request);
            return "redirect:/?message=project_created";
        } catch (Exception e) {
            log.error("프로젝트 생성에 실패했습니다: {}", e.getMessage());
            return "redirect:/projects/register?error_create_failed";
        }
    }



}
