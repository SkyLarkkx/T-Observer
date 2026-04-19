package com.edu.tobserver.auth.service;

import com.edu.tobserver.auth.dto.LoginRequest;
import com.edu.tobserver.auth.entity.SysUser;
import com.edu.tobserver.auth.mapper.UserMapper;
import com.edu.tobserver.auth.vo.CurrentUserVo;
import com.edu.tobserver.auth.vo.LoginResponse;
import com.edu.tobserver.common.context.LoginUser;
import com.edu.tobserver.common.context.LoginUserContext;
import com.edu.tobserver.common.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final TokenSessionService tokenSessionService;

    public AuthService(UserMapper userMapper, TokenSessionService tokenSessionService) {
        this.userMapper = userMapper;
        this.tokenSessionService = tokenSessionService;
    }

    public LoginResponse login(LoginRequest request) {
        SysUser sysUser = userMapper.findByUsername(request.getUsername());
        if (sysUser == null || !sysUser.getPassword().equals(request.getPassword()) || !"ACTIVE".equals(sysUser.getStatus())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        LoginUser loginUser = new LoginUser(
                sysUser.getId(),
                sysUser.getUsername(),
                sysUser.getRealName(),
                sysUser.getRoleCode()
        );
        String token = tokenSessionService.create(loginUser);
        return LoginResponse.builder()
                .token(token)
                .userId(sysUser.getId())
                .realName(sysUser.getRealName())
                .roleCode(sysUser.getRoleCode())
                .build();
    }

    public CurrentUserVo currentUser() {
        LoginUser loginUser = LoginUserContext.getRequired();
        return CurrentUserVo.builder()
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .realName(loginUser.getRealName())
                .roleCode(loginUser.getRoleCode())
                .build();
    }
}
