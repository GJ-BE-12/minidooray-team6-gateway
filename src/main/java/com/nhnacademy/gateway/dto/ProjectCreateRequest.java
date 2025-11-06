package com.nhnacademy.gateway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectCreateRequest {
    private String name;
    private String status = "활성";
}
