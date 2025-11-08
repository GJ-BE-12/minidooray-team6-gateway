package com.nhnacademy.gateway.dto.basic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Project 목록 조회시 Task 개수 등 추가 정보를 표시하기 위한 DTO (프로젝트에 대한 테스크 요약)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskProjectDto {
    private Long projectId;
    private String name;
    private String status;
    private int taskCount;
}
