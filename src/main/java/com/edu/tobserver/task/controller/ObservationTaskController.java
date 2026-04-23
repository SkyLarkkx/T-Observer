package com.edu.tobserver.task.controller;

import com.edu.tobserver.common.api.ApiResponse;
import com.edu.tobserver.common.api.PageResult;
import com.edu.tobserver.task.dto.TaskCreateRequest;
import com.edu.tobserver.task.dto.TaskQueryRequest;
import com.edu.tobserver.task.entity.ObservationTask;
import com.edu.tobserver.task.service.ObservationTaskService;
import com.edu.tobserver.task.vo.TaskListItemVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class ObservationTaskController {

    private final ObservationTaskService observationTaskService;

    public ObservationTaskController(ObservationTaskService observationTaskService) {
        this.observationTaskService = observationTaskService;
    }

    @PostMapping
    public ApiResponse<ObservationTask> create(@Valid @RequestBody TaskCreateRequest request) {
        return ApiResponse.success(observationTaskService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<TaskListItemVo>> list(@ModelAttribute TaskQueryRequest request) {
        return ApiResponse.success(observationTaskService.list(request));
    }
}
