package com.edu.tobserver.auth.mapper;

import com.edu.tobserver.auth.entity.SysUser;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("""
            select id, username, password, real_name, role_code, status
            from sys_user
            where username = #{username}
            """)
    SysUser findByUsername(String username);

    @Select("""
            <script>
            select id, username, password, real_name, role_code, status
            from sys_user
            where role_code = 'MEMBER'
              and status = 'ACTIVE'
            <if test="keyword != null and keyword != ''">
              and real_name like concat('%', #{keyword}, '%')
            </if>
            order by real_name asc, id asc
            </script>
            """)
    List<SysUser> findActiveMembers(@Param("keyword") String keyword);

    @Select("""
            select real_name
            from sys_user
            where id = #{id}
            """)
    String findRealNameById(@Param("id") Long id);
}
