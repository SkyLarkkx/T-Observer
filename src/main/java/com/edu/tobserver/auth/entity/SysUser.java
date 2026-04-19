package com.edu.tobserver.auth.entity;

import com.edu.tobserver.common.enums.RoleCode;
import lombok.Data;

@Data
public class SysUser {

    private Long id;
    private String username;
    private String password;
    private String realName;
    private RoleCode roleCode;
    private String status;
}
