package com.nhnacademy.gateway.controller;

import com.nhnacademy.gateway.dto.basic.AccountDto;
import com.nhnacademy.gateway.service.AccountService;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/projects/{projectId}/members")
public class ProjectMemberController {

    private final DataAggregationService aggregationService;
    private final AccountService accountService;

    public ProjectMemberController(DataAggregationService aggregationService, AccountService accountService) {
        this.aggregationService = aggregationService;
        this.accountService = accountService;
    }


    @GetMapping("/manage")
    public String showMemberManagement(@PathVariable Long projectId,
                                       @RequestParam(required = false) String query,
                                       Model model){
        List<Long> memberIds = aggregationService.getProjectMembersIds(projectId);
        List<AccountDto> currentMembers = accountService.getAccountDetailsByIds(memberIds);

        if(query!= null && !query.isBlank()){
            List<AccountDto> searchResult = accountService.searchAccounts(query);
            model.addAttribute("searchResults", searchResult);
        }

        model.addAttribute("currentMembers", currentMembers);
        model.addAttribute("projectId", projectId);
        model.addAttribute("query", query);
        return "projectMembers";
    }

    @PostMapping
    public String addMemberToProject(@PathVariable Long projectId, @RequestParam Long userId){
        try{
            aggregationService.addProjectMember(projectId, userId);
        }catch(Exception e){
          log.warn("멤버 추가 실패: {}", e.getMessage());
        }
        return "redirect:/projects/"+ projectId +"/members/manage";
    }

    @DeleteMapping("/{userId}")
    public String removeMemberFromProject(@PathVariable Long projectId, @PathVariable Long userId){
        try{
            aggregationService.removeProjectMember(projectId, userId);
        }catch (Exception e){
            log.warn("프로젝트 속 멤버 삭제 실패: {}", e.getMessage());
        }
        return "redirect:/projects/"+ projectId + "/members/manage";
    }
}
