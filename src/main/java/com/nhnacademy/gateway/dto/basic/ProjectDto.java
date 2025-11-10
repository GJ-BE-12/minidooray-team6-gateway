package com.nhnacademy.gateway.dto.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * project의 기본 정보를 담는 DTO 프로젝트 목록 조회 시 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String name;
    private String status;
    private String adminUserId;
}
