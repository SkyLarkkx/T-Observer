package com.edu.tobserver.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.tobserver.auth.service.TokenSessionService;
import com.edu.tobserver.common.context.LoginUser;
import com.edu.tobserver.common.enums.RoleCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class ObservationTaskControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TokenSessionService tokenSessionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    private String leaderToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        leaderToken = tokenSessionService.create(new LoginUser(1L, "leader01", "Leader One", RoleCode.LEADER));
    }

    @Test
    void leaderShouldCreateTaskAndWriteAuditLog() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .header("X-Auth-Token", leaderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"Grade 1 Math Observation",
                                  "observerId":2,
                                  "teacherName":"Teacher Zhao",
                                  "courseName":"Function Concept",
                                  "lessonTime":"2026-04-20T09:00:00",
                                  "deadline":"2026-04-22T18:00:00",
                                  "remark":"Focus on classroom interaction"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.title").value("Grade 1 Math Observation"));

        Integer taskCount = jdbcTemplate.queryForObject("select count(*) from observation_task", Integer.class);
        Integer auditCount = jdbcTemplate.queryForObject("select count(*) from audit_log", Integer.class);

        assertThat(taskCount).isEqualTo(1);
        assertThat(auditCount).isEqualTo(1);
    }

    @Test
    void leaderShouldListTasks() throws Exception {
        jdbcTemplate.update("""
                insert into observation_task
                    (title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "Seeded Observation Task",
                1L,
                2L,
                "Teacher Zhao",
                "Function Concept",
                java.sql.Timestamp.valueOf("2026-04-20 09:00:00"),
                java.sql.Timestamp.valueOf("2026-04-22 18:00:00"),
                "PENDING",
                "Seeded for query");

        mockMvc.perform(get("/api/tasks")
                        .header("X-Auth-Token", leaderToken)
                        .param("observerId", "2")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Seeded Observation Task"))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }
}
