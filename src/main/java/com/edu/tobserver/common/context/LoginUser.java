package com.edu.tobserver.common.context;

import com.edu.tobserver.common.enums.RoleCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUser {

    private final Long userId;
    private final String username;
    private final String realName;
    private final RoleCode roleCode;
}
