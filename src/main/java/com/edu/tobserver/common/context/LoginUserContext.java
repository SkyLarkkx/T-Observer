package com.edu.tobserver.common.context;

import com.edu.tobserver.common.exception.BusinessException;

public final class LoginUserContext {

    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    private LoginUserContext() {
    }

    public static void set(LoginUser loginUser) {
        CONTEXT.set(loginUser);
    }

    public static LoginUser getRequired() {
        LoginUser loginUser = CONTEXT.get();
        if (loginUser == null) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return loginUser;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
