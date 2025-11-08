package com.nhnacademy.gateway.controller;

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
            if(projectDetails.getProject().getAdminUserId().equals(userId)){
                model.addAttribute("isAdmin", true);
            }else{
                model.addAttribute("isAdmin", false);
            }
            return "projectDetails";
        } catch (Exception e) {
            log.error("프로젝트 디테일을 가져오는데 실패했습니다 : {}", e.getMessage());
            return "redirect:/?error=project_not_found";
        }
    }

    @GetMapping("/projects/{projectId}/edit")
    public String projectUpdateForm(@PathVariable Long projectId, Model model){
        try{
            ProjectDto projectDto = aggregationService.getProject(projectId);
            model.addAttribute("project", projectDto);
            return "projectUpdateForm";
        }catch (HttpClientErrorException.NotFound ex){
            model.addAttribute("error", "프로젝트 정보를 찾을 수 없습니다.");
            return "errorPage";
        }
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
        }catch (Exception e){
            log.error("프로젝트 정보 변경 실패 (server): {}", e.getMessage());
            model.addAttribute("error", "서버 오류로 변경 실패");
            return "errorPage";
        }
    }



}
