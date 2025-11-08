package com.nhnacademy.gateway.dto.create;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Project 생성시 기본 name과 status를 taskAPI에 전달하기 위한 요청 본문
 */
@Data
@NoArgsConstructor
public class ProjectCreateRequest {
    private String name;
    private String status = "활성";
}
