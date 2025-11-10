package com.nhnacademy.gateway.dto.create;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Project내에서 새로운 Task를 생성하기 위한 요청 본문 Task 생성시 creator과 project id가 필요함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {
    private String title;
    private String content;
    private Long mileStoneId;
}
