package com.edu.tobserver.record;

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
class ObservationRecordControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TokenSessionService tokenSessionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    private String memberToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        memberToken = tokenSessionService.create(new LoginUser(2L, "member01", "Member One", RoleCode.MEMBER));

        jdbcTemplate.update("delete from audit_log");
        jdbcTemplate.update("delete from observation_task");
        jdbcTemplate.update("""
                insert into observation_task
                    (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L,
                "Grade 1 Math Observation",
                1L,
                2L,
                "Teacher Zhao",
                "Function Concept",
                java.sql.Timestamp.valueOf("2026-04-20 09:00:00"),
                java.sql.Timestamp.valueOf("2026-04-22 18:00:00"),
                "PENDING",
                "Seeded for record tests");
    }

    @Test
    void shouldRejectSubmitWhenScoresAreIncomplete() throws Exception {
        mockMvc.perform(post("/api/records/submit")
                        .header("X-Auth-Token", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId":1,
                                  "teacherName":"Teacher Zhao",
                                  "strengths":"Classroom rhythm is good",
                                  "weaknesses":"Board writing can be clearer",
                                  "suggestions":"Add more in-class follow-up questions",
                                  "scores":[
                                    {"dimensionCode":"TEACHING_DESIGN","scoreValue":4.5}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("提交时必须填写 5 个维度评分"));
    }

    @Test
    void shouldSaveDraftAndPersistRecord() throws Exception {
        mockMvc.perform(post("/api/records/save-draft")
                        .header("X-Auth-Token", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId":1,
                                  "teacherName":"Teacher Zhao",
                                  "strengths":"Classroom rhythm is good",
                                  "weaknesses":"Board writing can be clearer",
                                  "suggestions":"Add more in-class follow-up questions",
                                  "scores":[
                                    {"dimensionCode":"TEACHING_DESIGN","scoreValue":4.5}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        Integer recordCount = jdbcTemplate.queryForObject("select count(*) from observation_record", Integer.class);
        assertThat(recordCount).isEqualTo(1);
    }

    @Test
    void shouldSubmitRecordAndPersistScores() throws Exception {
        mockMvc.perform(post("/api/records/submit")
                        .header("X-Auth-Token", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId":1,
                                  "teacherName":"Teacher Zhao",
                                  "strengths":"Classroom rhythm is good",
                                  "weaknesses":"Board writing can be clearer",
                                  "suggestions":"Add more in-class follow-up questions",
                                  "scores":[
                                    {"dimensionCode":"TEACHING_DESIGN","scoreValue":4.5},
                                    {"dimensionCode":"CLASSROOM_ORGANIZATION","scoreValue":4.0},
                                    {"dimensionCode":"TEACHING_CONTENT","scoreValue":4.2},
                                    {"dimensionCode":"INTERACTION_FEEDBACK","scoreValue":4.4},
                                    {"dimensionCode":"TEACHING_EFFECTIVENESS","scoreValue":4.3}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.scores.length()").value(5));

        Integer recordCount = jdbcTemplate.queryForObject("select count(*) from observation_record", Integer.class);
        Integer scoreCount = jdbcTemplate.queryForObject("select count(*) from observation_score", Integer.class);
        String taskStatus = jdbcTemplate.queryForObject(
                "select status from observation_task where id = 1",
                String.class);

        assertThat(recordCount).isEqualTo(1);
        assertThat(scoreCount).isEqualTo(5);
        assertThat(taskStatus).isEqualTo("COMPLETED");
    }

    @Test
    void shouldReturnExistingRecordByTaskId() throws Exception {
        mockMvc.perform(post("/api/records/save-draft")
                        .header("X-Auth-Token", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId":1,
                                  "teacherName":"Teacher Zhao",
                                  "strengths":"Classroom rhythm is good",
                                  "weaknesses":"Board writing can be clearer",
                                  "suggestions":"Add more in-class follow-up questions",
                                  "scores":[
                                    {"dimensionCode":"TEACHING_DESIGN","scoreValue":4.5}
                                  ]
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/records/task/1")
                        .header("X-Auth-Token", memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.taskId").value(1))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.strengths").value("Classroom rhythm is good"));
    }
}
