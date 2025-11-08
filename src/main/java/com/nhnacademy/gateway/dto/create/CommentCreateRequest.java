package com.nhnacademy.gateway.dto.create;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Task에 새로운 comment를 생성하기 위한 요청본문
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {
    private Long taskId;
    private String content;
}
