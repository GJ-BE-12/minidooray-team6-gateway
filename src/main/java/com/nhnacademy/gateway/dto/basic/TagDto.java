package com.nhnacademy.gateway.dto.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * tag의 정보를 담고 ProjectDetailsDto 내의 Tage 목록에 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDto {
    private Long id;
    private String name;
}
