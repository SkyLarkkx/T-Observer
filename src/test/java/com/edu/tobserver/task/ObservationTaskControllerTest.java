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
    private String memberToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        leaderToken = tokenSessionService.create(new LoginUser(1L, "leader01", "Leader One", RoleCode.LEADER));
        memberToken = tokenSessionService.create(new LoginUser(2L, "member01", "Member One", RoleCode.MEMBER));
        jdbcTemplate.update("delete from observation_record");
        jdbcTemplate.update("delete from audit_log");
        jdbcTemplate.update("delete from observation_task");
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
                .andExpect(jsonPath("$.data.list[0].title").value("Seeded Observation Task"))
                .andExpect(jsonPath("$.data.list[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10));
    }

    @Test
    void memberShouldListTasksWithPaginationAndReturnedRecordReason() throws Exception {
        insertTask(
                1L,
                "Normal Pending Task",
                2L,
                "Teacher Zhao",
                "Function Concept",
                "PENDING",
                "Short remark");
        insertTask(
                2L,
                "Returned Pending Task",
                2L,
                "Teacher Wang",
                "Reading",
                "PENDING",
                "Long remark for member task cards");
        jdbcTemplate.update("""
                insert into observation_record
                    (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                11L,
                2L,
                2L,
                "Teacher Wang",
                "Strength",
                "Weakness",
                "Suggestion",
                "RETURNED",
                "Please add concrete classroom interaction evidence before resubmitting.",
                java.sql.Timestamp.valueOf("2026-04-20 10:00:00"),
                null);

        mockMvc.perform(get("/api/tasks")
                        .header("X-Auth-Token", memberToken)
                        .param("status", "PENDING")
                        .param("pageNum", "1")
                        .param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list.length()").value(1))
                .andExpect(jsonPath("$.data.list[0].title").value("Returned Pending Task"))
                .andExpect(jsonPath("$.data.list[0].recordStatus").value("RETURNED"))
                .andExpect(jsonPath("$.data.list[0].rejectReason").value("Please add concrete classroom interaction evidence before resubmitting."))
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(1));
    }

    private void insertTask(Long id,
                            String title,
                            Long observerId,
                            String teacherName,
                            String courseName,
                            String status,
                            String remark) {
        jdbcTemplate.update("""
                insert into observation_task
                    (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                title,
                1L,
                observerId,
                teacherName,
                courseName,
                java.sql.Timestamp.valueOf("2026-04-20 09:00:00"),
                java.sql.Timestamp.valueOf("2026-04-22 18:00:00"),
                status,
                remark);
    }
}
