package com.nhnacademy.gateway.dto.detail;

import com.nhnacademy.gateway.dto.basic.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 특정 프로젝트의 모든 정보를 종합적으로 담는 DTO / gateway에서 화면을 표시하기위해사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailsDto {
    private ProjectDto project;
    private MileStoneDto mileStone;

    private List<TaskDto> tasks;
    private List<TagDto> tags;
    private List<AccountDto> members;
}
