package com.edu.tobserver.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edu.tobserver.auth.service.TokenSessionService;
import com.edu.tobserver.common.context.LoginUser;
import com.edu.tobserver.common.enums.RoleCode;
import java.math.BigDecimal;
import java.sql.Timestamp;
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
class ReviewAndAnalyticsControllerTest {

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

        jdbcTemplate.update("delete from observation_score");
        jdbcTemplate.update("delete from observation_record");
        jdbcTemplate.update("delete from observation_task");
        jdbcTemplate.update("delete from audit_log");

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
                Timestamp.valueOf("2026-04-20 09:00:00"),
                Timestamp.valueOf("2026-04-22 18:00:00"),
                "COMPLETED",
                "Seeded for review tests");

        jdbcTemplate.update("""
                insert into observation_record
                    (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                1L,
                1L,
                2L,
                "Teacher Zhao",
                "Good pacing",
                "Board writing can be clearer",
                "Add more questioning",
                "SUBMITTED",
                null,
                Timestamp.valueOf("2026-04-20 10:00:00"),
                null);

        insertScore(1L, "TEACHING_DESIGN", "Teaching Design", new BigDecimal("4.5"));
        insertScore(1L, "CLASSROOM_ORGANIZATION", "Classroom Organization", new BigDecimal("4.0"));
        insertScore(1L, "TEACHING_CONTENT", "Teaching Content", new BigDecimal("4.2"));
        insertScore(1L, "INTERACTION_FEEDBACK", "Interaction Feedback", new BigDecimal("4.4"));
        insertScore(1L, "TEACHING_EFFECTIVENESS", "Teaching Effectiveness", new BigDecimal("4.3"));
    }

    @Test
    void shouldFetchReviewRecordDetail() throws Exception {
        mockMvc.perform(get("/api/reviews/1")
                        .header("X-Auth-Token", leaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.observerName").value("赵老师"))
                .andExpect(jsonPath("$.data.teacherName").value("Teacher Zhao"))
                .andExpect(jsonPath("$.data.strengths").value("Good pacing"))
                .andExpect(jsonPath("$.data.scores[0].dimensionCode").value("TEACHING_DESIGN"));
    }

    @Test
    void shouldListReviewRecordsForLeader() throws Exception {
        insertReviewRecord(2L, "APPROVED", null, Timestamp.valueOf("2026-04-19 10:00:00"));
        insertReviewRecord(3L, "RETURNED", "Need more detail", Timestamp.valueOf("2026-04-21 10:00:00"));

        mockMvc.perform(get("/api/reviews")
                        .header("X-Auth-Token", leaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].recordId").value(3))
                .andExpect(jsonPath("$.data[0].taskTitle").value("Grade 1 Math Observation"))
                .andExpect(jsonPath("$.data[0].observerName").value("赵老师"))
                .andExpect(jsonPath("$.data[0].teacherName").value("Teacher Zhao"))
                .andExpect(jsonPath("$.data[0].courseName").value("Function Concept"))
                .andExpect(jsonPath("$.data[0].recordStatus").value("RETURNED"))
                .andExpect(jsonPath("$.data[1].recordStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.data[2].recordStatus").value("APPROVED"));
    }

    @Test
    void shouldRequireRejectReason() throws Exception {
        mockMvc.perform(post("/api/reviews/1/reject")
                        .header("X-Auth-Token", leaderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("退回时必须填写原因"));
    }

    @Test
    void shouldShowPendingReviewAgainAfterReturnedRecordIsResubmitted() throws Exception {
        mockMvc.perform(post("/api/reviews/1/reject")
                        .header("X-Auth-Token", leaderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Need more detail"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RETURNED"));

        mockMvc.perform(post("/api/records/submit")
                        .header("X-Auth-Token", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "taskId":1,
                                  "teacherName":"Teacher Zhao",
                                  "strengths":"Revised strengths",
                                  "weaknesses":"Revised weaknesses",
                                  "suggestions":"Revised suggestions",
                                  "scores":[
                                    {"dimensionCode":"TEACHING_DESIGN","dimensionName":"Teaching Design","scoreValue":4.5},
                                    {"dimensionCode":"CLASSROOM_ORGANIZATION","dimensionName":"Classroom Organization","scoreValue":4.0},
                                    {"dimensionCode":"TEACHING_CONTENT","dimensionName":"Teaching Content","scoreValue":4.2},
                                    {"dimensionCode":"INTERACTION_FEEDBACK","dimensionName":"Interaction Feedback","scoreValue":4.4},
                                    {"dimensionCode":"TEACHING_EFFECTIVENESS","dimensionName":"Teaching Effectiveness","scoreValue":4.3}
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.rejectReason").doesNotExist());

        mockMvc.perform(get("/api/reviews")
                        .header("X-Auth-Token", leaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].recordStatus").value("SUBMITTED"));
    }

    @Test
    void shouldApproveSubmittedRecord() throws Exception {
        mockMvc.perform(post("/api/reviews/1/approve")
                        .header("X-Auth-Token", leaderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        String recordStatus = jdbcTemplate.queryForObject(
                "select status from observation_record where id = 1",
                String.class);
        Integer auditCount = jdbcTemplate.queryForObject(
                "select count(*) from audit_log where biz_type = 'RECORD' and operation_type = 'APPROVE_RECORD'",
                Integer.class);

        assertThat(recordStatus).isEqualTo("APPROVED");
        assertThat(auditCount).isEqualTo(1);
    }

    @Test
    void shouldReturnNoDataConclusionWhenApprovedRecordsAreEmpty() throws Exception {
        mockMvc.perform(post("/api/analytics/generate")
                        .header("X-Auth-Token", leaderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"teacherName":"Teacher Zhao","startTime":"2026-04-01T00:00:00","endTime":"2026-04-30T23:59:59"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sampleCount").value(0))
                .andExpect(jsonPath("$.data.radarChart").doesNotExist())
                .andExpect(jsonPath("$.data.conclusion").value("所选时间范围内暂无已通过记录，无法生成分析"));
    }

    @Test
    void shouldGenerateRadarChartWhenApprovedRecordsReachOne() throws Exception {
        insertApprovedRecord(2L, "Teacher Zhao", Timestamp.valueOf("2026-04-10 10:00:00"));

        mockMvc.perform(post("/api/analytics/generate")
                        .header("X-Auth-Token", leaderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"teacherName":"Teacher Zhao","startTime":"2026-04-10T00:00:00","endTime":"2026-04-30T23:59:59"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sampleCount").value(1))
                .andExpect(jsonPath("$.data.radarChart.values[0]").value(4.5))
                .andExpect(jsonPath("$.data.conclusion").value("已根据 1 条已通过记录生成分析"));
    }

    @Test
    void shouldGenerateAnalyticsForApprovedRecordsWithinCustomTimeRange() throws Exception {
        insertApprovedRecord(2L, "Teacher Zhao", Timestamp.valueOf("2026-04-10 10:00:00"));
        insertApprovedRecord(3L, "Teacher Zhao", Timestamp.valueOf("2026-04-15 10:00:00"));
        insertApprovedRecord(4L, "Teacher Zhao", Timestamp.valueOf("2026-04-20 10:00:00"));
        insertApprovedRecord(5L, "Teacher Zhao", Timestamp.valueOf("2026-05-01 10:00:00"));

        mockMvc.perform(post("/api/analytics/generate")
                        .header("X-Auth-Token", leaderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"teacherName":"Teacher Zhao","startTime":"2026-04-10T00:00:00","endTime":"2026-04-30T23:59:59"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.periodType").value("RANGE"))
                .andExpect(jsonPath("$.data.periodValue").value("2026-04-10T00:00 ~ 2026-04-30T23:59"))
                .andExpect(jsonPath("$.data.sampleCount").value(3))
                .andExpect(jsonPath("$.data.radarChart.values[0]").value(4.5))
                .andExpect(jsonPath("$.data.conclusion").value("已根据 3 条已通过记录生成分析"));
    }

    private void insertApprovedRecord(Long recordId, String teacherName, Timestamp approvedAt) {
        jdbcTemplate.update("""
                insert into observation_record
                    (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                recordId,
                1L,
                2L,
                teacherName,
                "Strong design " + recordId,
                "Needs tighter feedback " + recordId,
                "Add structured reflection " + recordId,
                "APPROVED",
                null,
                Timestamp.valueOf("2026-04-10 09:00:00"),
                approvedAt);

        insertScore(recordId, "TEACHING_DESIGN", "Teaching Design", new BigDecimal("4.5"));
        insertScore(recordId, "CLASSROOM_ORGANIZATION", "Classroom Organization", new BigDecimal("4.0"));
        insertScore(recordId, "TEACHING_CONTENT", "Teaching Content", new BigDecimal("4.2"));
        insertScore(recordId, "INTERACTION_FEEDBACK", "Interaction Feedback", new BigDecimal("4.4"));
        insertScore(recordId, "TEACHING_EFFECTIVENESS", "Teaching Effectiveness", new BigDecimal("4.3"));
    }

    private void insertReviewRecord(Long recordId, String status, String rejectReason, Timestamp submittedAt) {
        jdbcTemplate.update("""
                insert into observation_record
                    (id, task_id, observer_id, teacher_name, strengths, weaknesses, suggestions, status, reject_reason, submitted_at, approved_at)
                values
                    (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                recordId,
                1L,
                2L,
                "Teacher Zhao",
                "Review strengths " + recordId,
                "Review weaknesses " + recordId,
                "Review suggestions " + recordId,
                status,
                rejectReason,
                submittedAt,
                "APPROVED".equals(status) ? Timestamp.valueOf("2026-04-21 11:00:00") : null);
    }

    private void insertScore(Long recordId, String dimensionCode, String dimensionName, BigDecimal scoreValue) {
        jdbcTemplate.update("""
                insert into observation_score
                    (record_id, dimension_code, dimension_name, score_value)
                values
                    (?, ?, ?, ?)
                """,
                recordId,
                dimensionCode,
                dimensionName,
                scoreValue);
    }
}
