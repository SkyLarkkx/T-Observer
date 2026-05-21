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
        Integer groupCount = jdbcTemplate.queryForObject("select count(*) from org_teaching_group", Integer.class);
        Integer groupMemberCount = jdbcTemplate.queryForObject("select count(*) from org_teaching_group_member", Integer.class);
        List<String> groupTables = jdbcTemplate.query(
                "select table_name from information_schema.tables "
                        + "where lower(table_name) in ('org_teaching_group', 'org_teaching_group_member') "
                        + "order by table_name",
                (rs, rowNum) -> rs.getString("table_name"));
        List<String> groupConstraints = jdbcTemplate.query(
                "select constraint_name from information_schema.table_constraints "
                        + "where upper(constraint_name) in ('UK_GROUP_CODE', 'UK_GROUP_USER') "
                        + "order by constraint_name",
                (rs, rowNum) -> rs.getString("constraint_name"));
        List<String> groupIndexes = jdbcTemplate.query(
                "select index_name from information_schema.indexes "
                        + "where upper(index_name) = 'IDX_MEMBER_USER_ROLE' "
                        + "order by index_name",
                (rs, rowNum) -> rs.getString("index_name"));
        List<String> multiGroupRoles = jdbcTemplate.query(
                "select group_id, group_role_code from org_teaching_group_member where user_id = 1 order by group_id",
                (rs, rowNum) -> rs.getLong("group_id") + ":" + rs.getString("group_role_code"));
        List<String> seededUsers = jdbcTemplate.query(
                "select username, role_code from sys_user where username in ('admin01', 'leader01', 'member01', 'member02', 'member03') order by username",
                (rs, rowNum) -> rs.getString("username") + ":" + rs.getString("role_code"));
        List<String> seededDimensions = jdbcTemplate.query(
                "select dimension_code from evaluation_dimension where dimension_code in ('TEACHING_DESIGN', 'TEACHING_EFFECTIVENESS') order by dimension_code",
                (rs, rowNum) -> rs.getString("dimension_code"));

        assertThat(userCount).isEqualTo(5);
        assertThat(memberCount).isEqualTo(3);
        assertThat(dimensionCount).isEqualTo(5);
        assertThat(groupCount).isEqualTo(2);
        assertThat(groupMemberCount).isEqualTo(5);
        assertThat(groupTables).containsExactly("ORG_TEACHING_GROUP", "ORG_TEACHING_GROUP_MEMBER");
        assertThat(groupConstraints).containsExactly("UK_GROUP_CODE", "UK_GROUP_USER");
        assertThat(groupIndexes).containsExactly("IDX_MEMBER_USER_ROLE");
        assertThat(multiGroupRoles).containsExactly("101:LEADER", "102:MEMBER");
        assertThat(seededUsers).containsExactly(
                "admin01:ADMIN",
                "leader01:LEADER",
                "member01:MEMBER",
                "member02:MEMBER",
                "member03:MEMBER");
        assertThat(seededDimensions).containsExactly("TEACHING_DESIGN", "TEACHING_EFFECTIVENESS");
    }
}
