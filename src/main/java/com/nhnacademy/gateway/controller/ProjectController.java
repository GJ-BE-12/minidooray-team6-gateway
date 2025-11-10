package com.nhnacademy.gateway.controller;

import com.nhnacademy.gateway.config.CustomUserPrincipal;
import com.nhnacademy.gateway.dto.basic.ProjectDto;
import com.nhnacademy.gateway.dto.create.ProjectCreateRequest;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.update.AccountUpdateRequest;
import com.nhnacademy.gateway.dto.update.ProjectUpdateRequest;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@Slf4j
public class ProjectController {
    private final DataAggregationService aggregationService;

    public ProjectController(DataAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    private String getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null){
            return null;
        }

        Object principal =authentication.getPrincipal();

        if(principal instanceof CustomUserPrincipal){
            return ((CustomUserPrincipal) principal).getUsername();
        }else if (principal instanceof  UserDetails){
            return ((UserDetails) principal).getUsername();
        }else if (principal instanceof String){
            return (String) principal;
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
        aggregationService.createProject(request);
        return "redirect:/?message=project_created";
    }

    @GetMapping("/projects/{projectId}")
    public String projectDetails(@PathVariable Long projectId, Model model){
        String userId = getCurrentUserId();
        if(userId == null){
            return "redirect:/login";
        }

        ProjectDetailsDto projectDetails = aggregationService.getProjectDetails(projectId);
        model.addAttribute("projectDetails", projectDetails);
        model.addAttribute("userId", userId);

        if(projectDetails.getProject().getAdminUserId().equals(userId)){
            model.addAttribute("isAdmin", true);
        }else{
            model.addAttribute("isAdmin", false);
        }
        return "projectDetails";
    }

    @GetMapping("/projects/{projectId}/edit")
    public String projectUpdateForm(@PathVariable Long projectId, Model model){

        ProjectDto projectDto = aggregationService.getProject(projectId);
        model.addAttribute("project", projectDto);
        return "projectUpdateForm";
    }

    @PutMapping("/projects/{projectId}")
    public String projectUpdate(@PathVariable Long projectId, @ModelAttribute("project") ProjectDto projectDto,
                                Model model){
        ProjectUpdateRequest updateRequest = new ProjectUpdateRequest(projectDto.getName(), projectDto.getStatus());
        try{
            aggregationService.updateProject(projectId, updateRequest);
            return "redirect:/projects/"+ projectId;
        }catch (IllegalArgumentException | HttpClientErrorException ex){
            log.warn("프로젝트 정보 변경 실패 (client): {}", ex.getMessage());
            model.addAttribute("error", "정보 변경에 실패했습니다. ");
            return "projectUpdateForm";
        }
    }

    @DeleteMapping("/projects/{projectId}")
    public String projectDelete(@PathVariable Long projectId, RedirectAttributes redirectAttributes){
        try{
            aggregationService.deleteProject(projectId);
            redirectAttributes.addFlashAttribute("message", "프로젝트가 성공적으로 삭제되었습니다.");
            return "redirect:/";
        }catch (IllegalArgumentException | HttpClientErrorException ex){
            log.warn("프로젝트 정보 변경 실패 (client): {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("error", "정보 변경에 실패했습니다. ");
            return "projectUpdateForm";
        }
    }

}
