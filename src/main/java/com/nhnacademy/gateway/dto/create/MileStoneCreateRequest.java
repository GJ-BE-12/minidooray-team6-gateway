package com.nhnacademy.gateway.dto.create;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MileStoneCreateRequest {
    private Long projectId;
    private String name;
}
