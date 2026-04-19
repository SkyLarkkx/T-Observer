package com.edu.tobserver.record.controller;

import com.edu.tobserver.common.api.ApiResponse;
import com.edu.tobserver.record.dto.RecordDraftRequest;
import com.edu.tobserver.record.dto.RecordSubmitRequest;
import com.edu.tobserver.record.service.ObservationRecordService;
import com.edu.tobserver.record.vo.ObservationRecordVo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/records")
public class ObservationRecordController {

    private final ObservationRecordService observationRecordService;

    public ObservationRecordController(ObservationRecordService observationRecordService) {
        this.observationRecordService = observationRecordService;
    }

    @PostMapping("/save-draft")
    public ApiResponse<ObservationRecordVo> saveDraft(@Valid @RequestBody RecordDraftRequest request) {
        return ApiResponse.success(observationRecordService.saveDraft(request));
    }

    @PostMapping("/submit")
    public ApiResponse<ObservationRecordVo> submit(@Valid @RequestBody RecordSubmitRequest request) {
        return ApiResponse.success(observationRecordService.submit(request));
    }
}
