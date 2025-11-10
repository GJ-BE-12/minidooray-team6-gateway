package com.nhnacademy.gateway.controller;

import com.nhnacademy.gateway.dto.create.TagCreateRequest;
import com.nhnacademy.gateway.dto.update.TagUpdateRequest;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/projects/{projectId}/tags")
public class ProjectTagController {
    private final DataAggregationService aggregationService;

    public ProjectTagController(DataAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @PostMapping
    public String createTag(@PathVariable Long projectId, @ModelAttribute TagCreateRequest request, RedirectAttributes redirectAttributes){
        try{
            aggregationService.createTag(projectId, request);
            redirectAttributes.addFlashAttribute("message", "새 태그가 생성되었습니다.");
        }catch (Exception ex){
            log.warn("태그생성 실패: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("error", "태그 생성에 실패했습니다.");
        }
        return "redirect:/projects/"+projectId;
    }

    @PutMapping("/{tagId}")
    public String UpdateTag(@PathVariable Long projectId, @PathVariable Long tagId, @ModelAttribute TagUpdateRequest request, RedirectAttributes redirectAttributes){
        try{
            aggregationService.updateTag(projectId,tagId, request);
        }catch (Exception e){
            log.warn("태그 수정 실패 (tagId: {}): {}", tagId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "태그 수정 실패");
        }
        return "redirect:/projects/"+projectId;
    }

    @DeleteMapping("/{tagId}")
    public String DeleteTag(@PathVariable Long projectId, @PathVariable Long tagId, RedirectAttributes redirectAttributes){
        try{
            aggregationService.deleteTag(projectId, tagId);
        }catch (Exception e){
            log.warn("태그 삭제 실패 tagId={}: {}", tagId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "태그 삭제 실패");
        }
        return "redirect:/projects/"+projectId;
    }
}
