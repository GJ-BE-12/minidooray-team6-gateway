package com.nhnacademy.gateway.dto.basic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Comment 정보를 담는 Dto (writerId는 TaskDetailsDto를 받고 내부 List<CommentDto>를 순회하여 writer_id 추출 ->
 *     이 아이디 목록으로 accountAPI에 AccountDto목록을 요청하여 두 정보를 조합한 후 id와 username 매칭 후 화면에 표시하는게 좋을듯
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private Long writerId;
    private Long taskId;
    private String content;
}
