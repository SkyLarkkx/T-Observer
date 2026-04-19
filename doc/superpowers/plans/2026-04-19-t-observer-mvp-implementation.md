# T-Observer MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first working MVP of the teacher observation system with local login, task assignment, record submission, leader review, and teacher analytics with a radar chart.

**Architecture:** The backend stays in the root Spring Boot project under `src/main/java`, using MyBatis, H2-backed integration tests, and a lightweight token interceptor instead of full Spring Security. The frontend stays in `t-observer-web`, using Vue 3, Pinia, Vue Router, Axios, and ECharts, with one layout shell and role-based pages for member and leader workflows.

**Tech Stack:** Java 17, Spring Boot, MyBatis, H2, MySQL, Vue 3, TypeScript, Vite, Pinia, Vue Router, Axios, ECharts, Vitest

---

## File Structure Map

### Backend

- `src/main/java/com/edu/tobserver/common/...`
  Shared response wrapper, exceptions, enums, and request context.
- `src/main/java/com/edu/tobserver/auth/...`
  Login request handling, local token sessions, and current-user lookup.
- `src/main/java/com/edu/tobserver/task/...`
  Task entities, mappers, services, controllers, and task status updates.
- `src/main/java/com/edu/tobserver/record/...`
  Observation record draft/submit flows and score validation.
- `src/main/java/com/edu/tobserver/review/...`
  Approve/reject endpoints and audit integration.
- `src/main/java/com/edu/tobserver/analytics/...`
  Approved-record aggregation, text summaries, and radar payload generation.
- `src/main/java/com/edu/tobserver/audit/...`
  Audit log persistence and write helpers.
- `src/main/resources/schema.sql`
  MySQL-friendly schema used by local development and test bootstrapping.
- `src/main/resources/data.sql`
  Demo accounts, fixed dimensions, and sample seed data.
- `src/test/java/com/edu/tobserver/...`
  MockMvc integration tests by module.
- `src/test/resources/application-test.properties`
  H2 datasource and SQL init settings for repeatable tests.

### Frontend

- `t-observer-web/src/api/...`
  Axios client and module-specific request functions.
- `t-observer-web/src/stores/auth.ts`
  Login state, user profile, token persistence, and logout behavior.
- `t-observer-web/src/router/index.ts`
  Route table and role-aware navigation guard.
- `t-observer-web/src/layouts/MainLayout.vue`
  Shared shell, top bar, role badge, and menu.
- `t-observer-web/src/views/login/LoginView.vue`
  Local login page.
- `t-observer-web/src/views/member/...`
  Member task list and observation record form pages.
- `t-observer-web/src/views/leader/...`
  Leader task management, review queue, and analytics pages.
- `t-observer-web/src/components/...`
  Reusable status tag, score panel, and radar chart components.
- `t-observer-web/src/types/...`
  Shared DTO and VO type definitions matching backend contracts.
- `t-observer-web/src/**/*.spec.ts`
  Vitest coverage for auth store and key page rendering.

### Cross-Cutting Contracts

- Role codes: `LEADER`, `MEMBER`, `ADMIN`
- Task statuses: `PENDING`, `IN_PROGRESS`, `COMPLETED`
- Record statuses: `DRAFT`, `SUBMITTED`, `RETURNED`, `APPROVED`
- Fixed dimensions:
  - `TEACHING_DESIGN`
  - `CLASSROOM_ORGANIZATION`
  - `TEACHING_CONTENT`
  - `INTERACTION_FEEDBACK`
  - `TEACHING_EFFECTIVENESS`

## Task 1: Bootstrap Schema, Shared Types, and Test Harness

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.properties`
- Create: `src/main/resources/schema.sql`
- Create: `src/main/resources/data.sql`
- Create: `src/test/resources/application-test.properties`
- Create: `src/main/java/com/edu/tobserver/common/api/ApiResponse.java`
- Create: `src/main/java/com/edu/tobserver/common/enums/RoleCode.java`
- Create: `src/main/java/com/edu/tobserver/common/enums/TaskStatus.java`
- Create: `src/main/java/com/edu/tobserver/common/enums/RecordStatus.java`
- Create: `src/main/java/com/edu/tobserver/common/enums/DimensionCode.java`
- Create: `src/test/java/com/edu/tobserver/bootstrap/BootstrapDataTest.java`

- [ ] **Step 1: Write the failing bootstrap test**

```java
package com.edu.tobserver.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

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
        Integer dimensionCount = jdbcTemplate.queryForObject("select count(*) from evaluation_dimension", Integer.class);

        assertThat(userCount).isEqualTo(4);
        assertThat(dimensionCount).isEqualTo(5);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=BootstrapDataTest test`
Expected: FAIL with a table-not-found or datasource initialization error because `sys_user` and `evaluation_dimension` do not exist yet.

- [ ] **Step 3: Add schema, seed data, shared enums, and test config**

```sql
create table sys_user (
    id bigint primary key auto_increment,
    username varchar(64) not null unique,
    password varchar(128) not null,
    real_name varchar(64) not null,
    role_code varchar(32) not null,
    status varchar(32) not null
);

create table evaluation_dimension (
    id bigint primary key auto_increment,
    dimension_code varchar(64) not null unique,
    dimension_name varchar(64) not null
);

insert into sys_user (username, password, real_name, role_code, status) values
('leader01', '123456', '教研组长张老师', 'LEADER', 'ACTIVE'),
('member01', '123456', '听课成员李老师', 'MEMBER', 'ACTIVE'),
('member02', '123456', '听课成员王老师', 'MEMBER', 'ACTIVE'),
('admin01', '123456', '系统管理员', 'ADMIN', 'ACTIVE');

insert into evaluation_dimension (dimension_code, dimension_name) values
('TEACHING_DESIGN', '教学设计'),
('CLASSROOM_ORGANIZATION', '课堂组织'),
('TEACHING_CONTENT', '教学内容'),
('INTERACTION_FEEDBACK', '互动反馈'),
('TEACHING_EFFECTIVENESS', '教学效果');
```

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/t_observer?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.sql.init.mode=always
spring.sql.init.encoding=UTF-8
mybatis.configuration.map-underscore-to-camel-case=true
```

```properties
spring.datasource.url=jdbc:h2:mem:tobserver;MODE=MySQL;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.sql.init.mode=always
mybatis.configuration.map-underscore-to-camel-case=true
```

```java
package com.edu.tobserver.common.enums;

public enum RoleCode {
    LEADER,
    MEMBER,
    ADMIN
}
```

```java
package com.edu.tobserver.common.api;

public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static ApiResponse<Void> successMessage(String message) {
        return new ApiResponse<>(200, message, null);
    }

    public static ApiResponse<Void> failure(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

- [ ] **Step 4: Run the bootstrap test to verify it passes**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=BootstrapDataTest test`
Expected: PASS with `Tests run: 1, Failures: 0, Errors: 0`.

- [ ] **Step 5: Commit the foundation**

```bash
git add pom.xml src/main/resources/application.properties src/main/resources/schema.sql src/main/resources/data.sql src/test/resources/application-test.properties src/main/java/com/edu/tobserver/common src/test/java/com/edu/tobserver/bootstrap/BootstrapDataTest.java
git commit -m "feat: bootstrap schema and shared enums"
```

## Task 2: Implement Local Login Backend and Request Interception

**Files:**
- Create: `src/main/java/com/edu/tobserver/common/exception/BusinessException.java`
- Create: `src/main/java/com/edu/tobserver/common/exception/GlobalExceptionHandler.java`
- Create: `src/main/java/com/edu/tobserver/common/context/LoginUser.java`
- Create: `src/main/java/com/edu/tobserver/common/context/LoginUserContext.java`
- Create: `src/main/java/com/edu/tobserver/auth/entity/SysUser.java`
- Create: `src/main/java/com/edu/tobserver/auth/dto/LoginRequest.java`
- Create: `src/main/java/com/edu/tobserver/auth/vo/LoginResponse.java`
- Create: `src/main/java/com/edu/tobserver/auth/vo/CurrentUserVo.java`
- Create: `src/main/java/com/edu/tobserver/auth/mapper/UserMapper.java`
- Create: `src/main/java/com/edu/tobserver/auth/service/TokenSessionService.java`
- Create: `src/main/java/com/edu/tobserver/auth/service/AuthService.java`
- Create: `src/main/java/com/edu/tobserver/auth/controller/AuthController.java`
- Create: `src/main/java/com/edu/tobserver/auth/config/AuthInterceptor.java`
- Create: `src/main/java/com/edu/tobserver/auth/config/WebMvcConfig.java`
- Test: `src/test/java/com/edu/tobserver/auth/AuthControllerTest.java`

- [ ] **Step 1: Write failing auth endpoint tests**

```java
package com.edu.tobserver.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"leader01","password":"123456"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andExpect(jsonPath("$.data.roleCode").value("LEADER"));
    }

    @Test
    void shouldRejectCurrentUserWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("未登录或登录已过期"));
    }
}
```

- [ ] **Step 2: Run the auth tests to verify they fail**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=AuthControllerTest test`
Expected: FAIL with `404` or bean-missing errors because the auth controller and interceptor are not implemented yet.

- [ ] **Step 3: Implement mapper, token session service, controller, and global error handling**

```java
package com.edu.tobserver.auth.service;

import com.edu.tobserver.auth.entity.SysUser;
import com.edu.tobserver.auth.mapper.UserMapper;
import com.edu.tobserver.auth.vo.CurrentUserVo;
import com.edu.tobserver.auth.vo.LoginResponse;
import com.edu.tobserver.common.context.LoginUser;
import com.edu.tobserver.common.exception.BusinessException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class TokenSessionService {
    private final Map<String, LoginUser> sessions = new ConcurrentHashMap<>();

    public String create(LoginUser loginUser) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, loginUser);
        return token;
    }

    public LoginUser get(String token) {
        return sessions.get(token);
    }
}
```

```java
package com.edu.tobserver.auth.controller;

import com.edu.tobserver.auth.dto.LoginRequest;
import com.edu.tobserver.auth.service.AuthService;
import com.edu.tobserver.auth.vo.CurrentUserVo;
import com.edu.tobserver.auth.vo.LoginResponse;
import com.edu.tobserver.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVo> me() {
        return ApiResponse.success(authService.currentUser());
    }
}
```

```java
package com.edu.tobserver.common.exception;

import com.edu.tobserver.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleBusiness(BusinessException exception) {
        return ApiResponse.failure(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldError().getDefaultMessage();
        return ApiResponse.failure(400, message);
    }
}
```

- [ ] **Step 4: Run the auth tests to verify they pass**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=AuthControllerTest test`
Expected: PASS with the login test returning a token and `/api/auth/me` returning `401` when the header is missing.

- [ ] **Step 5: Commit the auth backend**

```bash
git add src/main/java/com/edu/tobserver/common/exception src/main/java/com/edu/tobserver/common/context src/main/java/com/edu/tobserver/auth src/test/java/com/edu/tobserver/auth/AuthControllerTest.java
git commit -m "feat: add local login endpoints"
```

## Task 3: Implement Task Management Backend and Audit Logging

**Files:**
- Create: `src/main/java/com/edu/tobserver/task/entity/ObservationTask.java`
- Create: `src/main/java/com/edu/tobserver/task/dto/TaskCreateRequest.java`
- Create: `src/main/java/com/edu/tobserver/task/dto/TaskQueryRequest.java`
- Create: `src/main/java/com/edu/tobserver/task/vo/TaskListItemVo.java`
- Create: `src/main/java/com/edu/tobserver/task/mapper/ObservationTaskMapper.java`
- Create: `src/main/java/com/edu/tobserver/task/service/ObservationTaskService.java`
- Create: `src/main/java/com/edu/tobserver/task/controller/ObservationTaskController.java`
- Create: `src/main/java/com/edu/tobserver/audit/entity/AuditLog.java`
- Create: `src/main/java/com/edu/tobserver/audit/mapper/AuditLogMapper.java`
- Create: `src/main/java/com/edu/tobserver/audit/service/AuditLogService.java`
- Modify: `src/main/resources/schema.sql`
- Test: `src/test/java/com/edu/tobserver/task/ObservationTaskControllerTest.java`

- [ ] **Step 1: Write failing task management tests**

```java
package com.edu.tobserver.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ObservationTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void leaderShouldCreateTask() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .header("X-Auth-Token", "leader-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "title":"高一数学听课",
                      "observerId":2,
                      "teacherName":"赵老师",
                      "courseName":"函数概念",
                      "lessonTime":"2026-04-20T09:00:00",
                      "deadline":"2026-04-22T18:00:00",
                      "remark":"重点观察课堂互动"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
```

- [ ] **Step 2: Run the task tests to verify they fail**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=ObservationTaskControllerTest test`
Expected: FAIL because the task table, endpoint, and leader-token helper do not exist yet.

- [ ] **Step 3: Create task table, mapper, service, controller, and audit helper**

```sql
create table observation_task (
    id bigint primary key auto_increment,
    title varchar(128) not null,
    leader_id bigint not null,
    observer_id bigint not null,
    teacher_name varchar(64) not null,
    course_name varchar(128) not null,
    lesson_time timestamp not null,
    deadline timestamp not null,
    status varchar(32) not null,
    remark varchar(255),
    created_at timestamp not null default current_timestamp
);

create table audit_log (
    id bigint primary key auto_increment,
    biz_type varchar(32) not null,
    biz_id bigint not null,
    operation_type varchar(32) not null,
    operator_id bigint not null,
    operator_name varchar(64) not null,
    content varchar(255) not null,
    created_at timestamp not null default current_timestamp
);
```

```java
package com.edu.tobserver.task.service;

import com.edu.tobserver.audit.service.AuditLogService;
import com.edu.tobserver.common.context.LoginUserContext;
import com.edu.tobserver.common.enums.TaskStatus;
import com.edu.tobserver.task.dto.TaskCreateRequest;
import com.edu.tobserver.task.entity.ObservationTask;
import com.edu.tobserver.task.mapper.ObservationTaskMapper;
import org.springframework.stereotype.Service;

@Service
public class ObservationTaskService {

    private final ObservationTaskMapper taskMapper;
    private final AuditLogService auditLogService;

    public ObservationTaskService(ObservationTaskMapper taskMapper, AuditLogService auditLogService) {
        this.taskMapper = taskMapper;
        this.auditLogService = auditLogService;
    }

    public ObservationTask create(TaskCreateRequest request) {
        ObservationTask task = new ObservationTask();
        task.setTitle(request.title());
        task.setLeaderId(LoginUserContext.required().userId());
        task.setObserverId(request.observerId());
        task.setTeacherName(request.teacherName());
        task.setCourseName(request.courseName());
        task.setLessonTime(request.lessonTime());
        task.setDeadline(request.deadline());
        task.setRemark(request.remark());
        task.setStatus(TaskStatus.PENDING.name());
        taskMapper.insert(task);
        auditLogService.write("TASK", task.getId(), "CREATE_TASK", "创建听课任务");
        return task;
    }
}
```

- [ ] **Step 4: Run the task tests to verify they pass**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=ObservationTaskControllerTest test`
Expected: PASS with task creation returning `PENDING` and one task row inserted.

- [ ] **Step 5: Commit the task backend**

```bash
git add src/main/java/com/edu/tobserver/task src/main/java/com/edu/tobserver/audit src/main/resources/schema.sql src/test/java/com/edu/tobserver/task/ObservationTaskControllerTest.java
git commit -m "feat: add observation task management"
```

## Task 4: Implement Observation Record Draft and Submit Backend

**Files:**
- Create: `src/main/java/com/edu/tobserver/record/entity/ObservationRecord.java`
- Create: `src/main/java/com/edu/tobserver/record/entity/ObservationScore.java`
- Create: `src/main/java/com/edu/tobserver/record/dto/ScoreInput.java`
- Create: `src/main/java/com/edu/tobserver/record/dto/RecordDraftRequest.java`
- Create: `src/main/java/com/edu/tobserver/record/dto/RecordSubmitRequest.java`
- Create: `src/main/java/com/edu/tobserver/record/vo/ObservationRecordVo.java`
- Create: `src/main/java/com/edu/tobserver/record/mapper/ObservationRecordMapper.java`
- Create: `src/main/java/com/edu/tobserver/record/mapper/ObservationScoreMapper.java`
- Create: `src/main/java/com/edu/tobserver/record/service/ObservationRecordService.java`
- Create: `src/main/java/com/edu/tobserver/record/controller/ObservationRecordController.java`
- Modify: `src/main/resources/schema.sql`
- Test: `src/test/java/com/edu/tobserver/record/ObservationRecordControllerTest.java`

- [ ] **Step 1: Write failing draft and submit tests**

```java
package com.edu.tobserver.record;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ObservationRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectSubmitWhenScoresAreIncomplete() throws Exception {
        mockMvc.perform(post("/api/records/submit")
                .header("X-Auth-Token", "member-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "taskId":1,
                      "teacherName":"赵老师",
                      "strengths":"课堂节奏较好",
                      "weaknesses":"板书层次可以更清晰",
                      "suggestions":"增加随堂追问",
                      "scores":[
                        {"dimensionCode":"TEACHING_DESIGN","scoreValue":4.5}
                      ]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("提交时必须填写 5 个维度评分"));
    }
}
```

- [ ] **Step 2: Run the record tests to verify they fail**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=ObservationRecordControllerTest test`
Expected: FAIL because record endpoints and validation rules are not implemented yet.

- [ ] **Step 3: Create record tables, service validation, and draft/submit endpoints**

```sql
create table observation_record (
    id bigint primary key auto_increment,
    task_id bigint not null,
    observer_id bigint not null,
    teacher_name varchar(64) not null,
    strengths clob not null,
    weaknesses clob not null,
    suggestions clob not null,
    status varchar(32) not null,
    reject_reason varchar(255),
    submitted_at timestamp,
    approved_at timestamp
);

create table observation_score (
    id bigint primary key auto_increment,
    record_id bigint not null,
    dimension_code varchar(64) not null,
    dimension_name varchar(64) not null,
    score_value decimal(2,1) not null
);
```

```java
private void validateForSubmit(RecordSubmitRequest request) {
    if (request.scores() == null || request.scores().size() != 5) {
        throw new BusinessException(400, "提交时必须填写 5 个维度评分");
    }
    for (ScoreInput score : request.scores()) {
        if (score.scoreValue().compareTo(new BigDecimal("1.0")) < 0
                || score.scoreValue().compareTo(new BigDecimal("5.0")) > 0) {
            throw new BusinessException(400, "评分必须在 1.0 到 5.0 之间");
        }
    }
}
```

```java
@PostMapping("/save-draft")
public ApiResponse<ObservationRecordVo> saveDraft(@RequestBody RecordDraftRequest request) {
    return ApiResponse.success(recordService.saveDraft(request));
}

@PostMapping("/submit")
public ApiResponse<ObservationRecordVo> submit(@Valid @RequestBody RecordSubmitRequest request) {
    return ApiResponse.success(recordService.submit(request));
}
```

- [ ] **Step 4: Run the record tests to verify they pass**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=ObservationRecordControllerTest test`
Expected: PASS with incomplete-score submit returning `400` and valid draft/submit flows ready for the next tasks.

- [ ] **Step 5: Commit the record backend**

```bash
git add src/main/java/com/edu/tobserver/record src/main/resources/schema.sql src/test/java/com/edu/tobserver/record/ObservationRecordControllerTest.java
git commit -m "feat: add observation record draft and submit flow"
```

## Task 5: Implement Review and Analytics Backend

**Files:**
- Create: `src/main/java/com/edu/tobserver/review/dto/RejectRequest.java`
- Create: `src/main/java/com/edu/tobserver/review/controller/ReviewController.java`
- Create: `src/main/java/com/edu/tobserver/review/service/ReviewService.java`
- Create: `src/main/java/com/edu/tobserver/analytics/entity/RadarReport.java`
- Create: `src/main/java/com/edu/tobserver/analytics/dto/AnalyticsGenerateRequest.java`
- Create: `src/main/java/com/edu/tobserver/analytics/vo/AnalyticsReportVo.java`
- Create: `src/main/java/com/edu/tobserver/analytics/mapper/AnalyticsMapper.java`
- Create: `src/main/java/com/edu/tobserver/analytics/service/AnalyticsService.java`
- Create: `src/main/java/com/edu/tobserver/analytics/controller/AnalyticsController.java`
- Modify: `src/main/resources/schema.sql`
- Test: `src/test/java/com/edu/tobserver/review/ReviewAndAnalyticsControllerTest.java`

- [ ] **Step 1: Write failing review and analytics tests**

```java
package com.edu.tobserver.review;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewAndAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRequireRejectReason() throws Exception {
        mockMvc.perform(post("/api/reviews/1/reject")
                .header("X-Auth-Token", "leader-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"reason":""}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("退回时必须填写原因"));
    }

    @Test
    void shouldReturnSampleInsufficientWhenApprovedRecordsLessThanThree() throws Exception {
        mockMvc.perform(post("/api/analytics/generate")
                .header("X-Auth-Token", "leader-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"teacherName":"赵老师","periodType":"MONTH","periodValue":"2026-04"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.sampleCount").value(0))
            .andExpect(jsonPath("$.data.radarChart").doesNotExist())
            .andExpect(jsonPath("$.data.conclusion").value("样本不足，暂不生成雷达图"));
    }
}
```

- [ ] **Step 2: Run the review and analytics tests to verify they fail**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=ReviewAndAnalyticsControllerTest test`
Expected: FAIL because the review endpoints, aggregation query, and report table are missing.

- [ ] **Step 3: Implement approve/reject flow, report table, and analytics aggregation**

```sql
create table radar_report (
    id bigint primary key auto_increment,
    teacher_name varchar(64) not null,
    period_type varchar(32) not null,
    period_value varchar(32) not null,
    sample_count int not null,
    radar_json clob,
    strength_summary clob not null,
    weakness_summary clob not null,
    conclusion varchar(255) not null,
    generated_at timestamp not null default current_timestamp
);
```

```java
if (request.reason() == null || request.reason().isBlank()) {
    throw new BusinessException(400, "退回时必须填写原因");
}
```

```java
if (approvedRecords.size() < 3) {
    return new AnalyticsReportVo(
        request.teacherName(),
        request.periodValue(),
        approvedRecords.size(),
        null,
        joinTexts(approvedRecords, ObservationRecord::getStrengths),
        joinTexts(approvedRecords, ObservationRecord::getWeaknesses),
        "样本不足，暂不生成雷达图"
    );
}
```

- [ ] **Step 4: Run the review and analytics tests to verify they pass**

Run: `.\mvnw.cmd -Dspring.profiles.active=test -Dtest=ReviewAndAnalyticsControllerTest test`
Expected: PASS with reject-without-reason returning `400` and low-sample analytics returning the text-only result.

- [ ] **Step 5: Commit the review and analytics backend**

```bash
git add src/main/java/com/edu/tobserver/review src/main/java/com/edu/tobserver/analytics src/main/resources/schema.sql src/test/java/com/edu/tobserver/review/ReviewAndAnalyticsControllerTest.java
git commit -m "feat: add review and analytics backend"
```

## Task 6: Build Frontend Foundation, HTTP Client, and Login Flow

**Files:**
- Modify: `t-observer-web/package.json`
- Modify: `t-observer-web/package-lock.json`
- Create: `t-observer-web/src/api/http.ts`
- Create: `t-observer-web/src/api/auth.ts`
- Create: `t-observer-web/src/types/auth.ts`
- Create: `t-observer-web/src/stores/auth.ts`
- Modify: `t-observer-web/src/router/index.ts`
- Modify: `t-observer-web/src/main.ts`
- Modify: `t-observer-web/src/App.vue`
- Create: `t-observer-web/src/layouts/MainLayout.vue`
- Create: `t-observer-web/src/views/login/LoginView.vue`
- Delete: `t-observer-web/src/stores/counter.ts`
- Create: `t-observer-web/src/stores/auth.spec.ts`

- [ ] **Step 1: Add frontend dependencies and write a failing auth store test**

```ts
import { describe, expect, it } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth'

describe('auth store', () => {
  it('stores token and role after login success', () => {
    setActivePinia(createPinia())
    const store = useAuthStore()

    store.acceptLogin({
      token: 'test-token',
      userId: 1,
      realName: '教研组长张老师',
      roleCode: 'LEADER',
    })

    expect(store.token).toBe('test-token')
    expect(store.roleCode).toBe('LEADER')
  })
})
```

Run:

```bash
cd t-observer-web
npm install axios echarts
npm install -D vitest jsdom @vue/test-utils
```

Expected after installation: `package.json` and `package-lock.json` change, but `npm run test -- auth.spec.ts` still fails because `useAuthStore` does not exist yet.

- [ ] **Step 2: Run the failing frontend auth test**

Run:

```bash
cd t-observer-web
npx vitest run src/stores/auth.spec.ts
```

Expected: FAIL with module-not-found or missing method errors for `useAuthStore` or `acceptLogin`.

- [ ] **Step 3: Implement Axios client, auth store, login page, and route guard**

```ts
// t-observer-web/src/api/http.ts
import axios from 'axios'

export const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('t-observer-token')
  if (token) {
    config.headers['X-Auth-Token'] = token
  }
  return config
})
```

```ts
// t-observer-web/src/stores/auth.ts
import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('t-observer-token') ?? '',
    roleCode: localStorage.getItem('t-observer-role') ?? '',
    realName: localStorage.getItem('t-observer-name') ?? '',
    userId: Number(localStorage.getItem('t-observer-user-id') ?? 0),
  }),
  actions: {
    acceptLogin(payload: { token: string; userId: number; realName: string; roleCode: string }) {
      this.token = payload.token
      this.userId = payload.userId
      this.realName = payload.realName
      this.roleCode = payload.roleCode
      localStorage.setItem('t-observer-token', payload.token)
      localStorage.setItem('t-observer-role', payload.roleCode)
      localStorage.setItem('t-observer-name', payload.realName)
      localStorage.setItem('t-observer-user-id', String(payload.userId))
    },
  },
})
```

```ts
// t-observer-web/src/router/index.ts
routes: [
  { path: '/login', name: 'login', component: () => import('../views/login/LoginView.vue') },
  { path: '/', component: () => import('../layouts/MainLayout.vue'), children: [] },
]
```

- [ ] **Step 4: Run the frontend auth test and build checks**

Run:

```bash
cd t-observer-web
npx vitest run src/stores/auth.spec.ts
npm run type-check
npm run build
```

Expected: PASS in Vitest, `vue-tsc` succeeds, and Vite build completes without route or store errors.

- [ ] **Step 5: Commit the frontend foundation**

```bash
git add t-observer-web/package.json t-observer-web/package-lock.json t-observer-web/src/api t-observer-web/src/types t-observer-web/src/stores/auth.ts t-observer-web/src/stores/auth.spec.ts t-observer-web/src/router/index.ts t-observer-web/src/main.ts t-observer-web/src/App.vue t-observer-web/src/layouts/MainLayout.vue t-observer-web/src/views/login/LoginView.vue
git rm t-observer-web/src/stores/counter.ts
git commit -m "feat: add frontend auth foundation"
```

## Task 7: Build Task List and Record Form Frontend

**Files:**
- Create: `t-observer-web/src/types/task.ts`
- Create: `t-observer-web/src/types/record.ts`
- Create: `t-observer-web/src/api/tasks.ts`
- Create: `t-observer-web/src/api/records.ts`
- Create: `t-observer-web/src/components/common/StatusTag.vue`
- Create: `t-observer-web/src/components/record/DimensionScorePanel.vue`
- Create: `t-observer-web/src/views/member/MemberTaskListView.vue`
- Create: `t-observer-web/src/views/member/RecordFormView.vue`
- Create: `t-observer-web/src/views/leader/LeaderTaskManageView.vue`
- Modify: `t-observer-web/src/router/index.ts`
- Create: `t-observer-web/src/views/member/MemberTaskListView.spec.ts`

- [ ] **Step 1: Write a failing task list render test**

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import MemberTaskListView from './MemberTaskListView.vue'

describe('MemberTaskListView', () => {
  it('shows status filter tabs', () => {
    const wrapper = mount(MemberTaskListView, {
      global: {
        stubs: ['RouterLink'],
      },
    })

    expect(wrapper.text()).toContain('待填写')
    expect(wrapper.text()).toContain('已退回')
  })
})
```

- [ ] **Step 2: Run the task list test to verify it fails**

Run:

```bash
cd t-observer-web
npx vitest run src/views/member/MemberTaskListView.spec.ts
```

Expected: FAIL because `MemberTaskListView.vue` does not exist yet.

- [ ] **Step 3: Implement task APIs, status tags, member list, leader task page, and record form**

```ts
// t-observer-web/src/api/tasks.ts
import { http } from './http'

export function fetchTasks(params?: Record<string, string>) {
  return http.get('/api/tasks', { params })
}

export function createTask(payload: Record<string, unknown>) {
  return http.post('/api/tasks', payload)
}
```

```vue
<!-- t-observer-web/src/components/record/DimensionScorePanel.vue -->
<script setup lang="ts">
defineProps<{
  modelValue: { dimensionCode: string; dimensionName: string; scoreValue: number }[]
}>()
const emits = defineEmits<{
  'update:modelValue': [{ dimensionCode: string; dimensionName: string; scoreValue: number }[]]
}>()
</script>
```

```vue
<!-- t-observer-web/src/views/member/RecordFormView.vue -->
<template>
  <section class="record-form-page">
    <h2>听课记录填写</h2>
    <DimensionScorePanel v-model="scores" />
    <textarea v-model="strengths" placeholder="填写优点项" />
    <textarea v-model="weaknesses" placeholder="填写待改进项" />
    <textarea v-model="suggestions" placeholder="填写改进建议" />
    <button @click="saveDraft">保存草稿</button>
    <button @click="submitRecord">提交</button>
  </section>
</template>
```

- [ ] **Step 4: Run the frontend tests and build checks**

Run:

```bash
cd t-observer-web
npx vitest run src/views/member/MemberTaskListView.spec.ts
npm run type-check
npm run build
```

Expected: PASS with the status filter labels rendered and the app compiling with member and leader task pages wired into the router.

- [ ] **Step 5: Commit the task and record pages**

```bash
git add t-observer-web/src/types/task.ts t-observer-web/src/types/record.ts t-observer-web/src/api/tasks.ts t-observer-web/src/api/records.ts t-observer-web/src/components/common/StatusTag.vue t-observer-web/src/components/record/DimensionScorePanel.vue t-observer-web/src/views/member t-observer-web/src/views/leader/LeaderTaskManageView.vue t-observer-web/src/router/index.ts
git commit -m "feat: add task list and record form pages"
```

## Task 8: Build Review and Analytics Frontend and Final Verification

**Files:**
- Create: `t-observer-web/src/types/analytics.ts`
- Create: `t-observer-web/src/api/reviews.ts`
- Create: `t-observer-web/src/api/analytics.ts`
- Create: `t-observer-web/src/components/analytics/RadarChart.vue`
- Create: `t-observer-web/src/views/leader/ReviewView.vue`
- Create: `t-observer-web/src/views/leader/AnalyticsView.vue`
- Create: `t-observer-web/src/views/leader/AnalyticsView.spec.ts`
- Modify: `t-observer-web/src/router/index.ts`
- Modify: `src/main/resources/data.sql`

- [ ] **Step 1: Write a failing analytics page test**

```ts
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import AnalyticsView from './AnalyticsView.vue'

describe('AnalyticsView', () => {
  it('renders sample-insufficient message when no radar data exists', () => {
    const wrapper = mount(AnalyticsView)
    expect(wrapper.text()).toContain('样本不足')
  })
})
```

- [ ] **Step 2: Run the analytics page test to verify it fails**

Run:

```bash
cd t-observer-web
npx vitest run src/views/leader/AnalyticsView.spec.ts
```

Expected: FAIL because `AnalyticsView.vue` does not exist yet.

- [ ] **Step 3: Implement review page, analytics page, radar chart component, and richer demo data**

```vue
<!-- t-observer-web/src/components/analytics/RadarChart.vue -->
<script setup lang="ts">
import * as echarts from 'echarts'
import { onMounted, ref, watch } from 'vue'

const props = defineProps<{
  indicators: { name: string; max: number }[]
  values: number[]
}>()

const chartRef = ref<HTMLDivElement | null>(null)

function render() {
  if (!chartRef.value) return
  const chart = echarts.init(chartRef.value)
  chart.setOption({
    radar: { indicator: props.indicators },
    series: [{ type: 'radar', data: [{ value: props.values }] }],
  })
}

onMounted(render)
watch(() => props.values, render, { deep: true })
</script>
```

```vue
<!-- t-observer-web/src/views/leader/AnalyticsView.vue -->
<template>
  <section class="analytics-page">
    <h2>教师分析结果</h2>
    <p v-if="!report?.radarChart">样本不足，暂不生成雷达图</p>
    <RadarChart
      v-else
      :indicators="report.radarChart.indicators"
      :values="report.radarChart.values"
    />
  </section>
</template>
```

```sql
insert into observation_task (id, title, leader_id, observer_id, teacher_name, course_name, lesson_time, deadline, status, remark)
values
(1, '高一数学听课 1', 1, 2, '赵老师', '函数概念', '2026-04-20 09:00:00', '2026-04-22 18:00:00', 'COMPLETED', '观察互动'),
(2, '高一数学听课 2', 1, 3, '赵老师', '函数概念', '2026-04-21 09:00:00', '2026-04-23 18:00:00', 'COMPLETED', '观察板书'),
(3, '高一数学听课 3', 1, 2, '赵老师', '函数概念', '2026-04-22 09:00:00', '2026-04-24 18:00:00', 'COMPLETED', '观察提问');
```

- [ ] **Step 4: Run full verification**

Run:

```bash
.\mvnw.cmd test
cd t-observer-web
npx vitest run
npm run type-check
npm run build
```

Expected:

- Maven reports `BUILD SUCCESS`
- Vitest reports all store/page specs passing
- `vue-tsc` completes without type errors
- Vite emits a production bundle successfully

- [ ] **Step 5: Commit the review and analytics UI**

```bash
git add t-observer-web/src/types/analytics.ts t-observer-web/src/api/reviews.ts t-observer-web/src/api/analytics.ts t-observer-web/src/components/analytics/RadarChart.vue t-observer-web/src/views/leader/ReviewView.vue t-observer-web/src/views/leader/AnalyticsView.vue t-observer-web/src/views/leader/AnalyticsView.spec.ts t-observer-web/src/router/index.ts src/main/resources/data.sql
git commit -m "feat: add review and analytics pages"
```

## Plan Self-Review

### Spec Coverage

- Local login and fixed roles are covered by Task 2 and Task 6.
- Task creation, assignment, and list display are covered by Task 3 and Task 7.
- Draft/save/submit observation records are covered by Task 4 and Task 7.
- Leader approve/reject review flow is covered by Task 5 and Task 8.
- Strength/weakness summaries and radar generation are covered by Task 5 and Task 8.
- Demo data and end-to-end verification are covered by Task 1 and Task 8.

### Placeholder Scan

- Checked the plan against common placeholder patterns and vague action language.
- Replaced vague steps with concrete endpoints, files, commands, and code snippets.

### Type Consistency

- Backend and frontend both use the same role codes, task statuses, record statuses, and fixed dimension codes.
- Backend endpoints in the plan match the spec:
  - `/api/auth/login`
  - `/api/auth/me`
  - `/api/tasks`
  - `/api/records/save-draft`
  - `/api/records/submit`
  - `/api/reviews/{recordId}/approve`
  - `/api/reviews/{recordId}/reject`
  - `/api/analytics/generate`
