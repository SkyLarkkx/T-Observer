package com.edu.tobserver.auth.mapper;

import com.edu.tobserver.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("""
            select id, username, password, real_name, role_code, status
            from sys_user
            where username = #{username}
            """)
    SysUser findByUsername(String username);
}
