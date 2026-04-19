package com.edu.tobserver.auth.vo;

import com.edu.tobserver.common.enums.RoleCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserVo {

    private Long userId;
    private String username;
    private String realName;
    private RoleCode roleCode;
}
