package com.edu.tobserver.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BootstrapDataTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldLoadSeedUsersAndDimensions() {
        Integer userCount = jdbcTemplate.queryForObject("select count(*) from sys_user", Integer.class);
        Integer memberCount = jdbcTemplate.queryForObject(
                "select count(*) from sys_user where role_code = 'MEMBER'",
                Integer.class);
        Integer dimensionCount = jdbcTemplate.queryForObject("select count(*) from evaluation_dimension", Integer.class);
        List<String> seededUsers = jdbcTemplate.query(
                "select username, role_code from sys_user where username in ('admin01', 'leader01', 'member01', 'member02', 'member03') order by username",
                (rs, rowNum) -> rs.getString("username") + ":" + rs.getString("role_code"));
        List<String> seededDimensions = jdbcTemplate.query(
                "select dimension_code from evaluation_dimension where dimension_code in ('TEACHING_DESIGN', 'TEACHING_EFFECTIVENESS') order by dimension_code",
                (rs, rowNum) -> rs.getString("dimension_code"));

        assertThat(userCount).isEqualTo(5);
        assertThat(memberCount).isEqualTo(3);
        assertThat(dimensionCount).isEqualTo(5);
        assertThat(seededUsers).containsExactly(
                "admin01:ADMIN",
                "leader01:LEADER",
                "member01:MEMBER",
                "member02:MEMBER",
                "member03:MEMBER");
        assertThat(seededDimensions).containsExactly("TEACHING_DESIGN", "TEACHING_EFFECTIVENESS");
    }
}
