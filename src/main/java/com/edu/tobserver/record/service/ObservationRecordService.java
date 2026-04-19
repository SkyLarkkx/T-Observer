package com.edu.tobserver.record.service;

import com.edu.tobserver.audit.service.AuditLogService;
import com.edu.tobserver.common.context.LoginUser;
import com.edu.tobserver.common.context.LoginUserContext;
import com.edu.tobserver.common.enums.DimensionCode;
import com.edu.tobserver.common.enums.RecordStatus;
import com.edu.tobserver.common.enums.RoleCode;
import com.edu.tobserver.common.enums.TaskStatus;
import com.edu.tobserver.common.exception.BusinessException;
import com.edu.tobserver.record.dto.RecordDraftRequest;
import com.edu.tobserver.record.dto.RecordSubmitRequest;
import com.edu.tobserver.record.dto.ScoreInput;
import com.edu.tobserver.record.entity.ObservationRecord;
import com.edu.tobserver.record.entity.ObservationScore;
import com.edu.tobserver.record.mapper.ObservationRecordMapper;
import com.edu.tobserver.record.mapper.ObservationScoreMapper;
import com.edu.tobserver.record.vo.ObservationRecordVo;
import com.edu.tobserver.task.entity.ObservationTask;
import com.edu.tobserver.task.mapper.ObservationTaskMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObservationRecordService {

    private static final String SUBMIT_SCORE_COUNT_MESSAGE = "\u63d0\u4ea4\u65f6\u5fc5\u987b\u586b\u5199 5 \u4e2a\u7ef4\u5ea6\u8bc4\u5206";
    private static final String SCORE_RANGE_MESSAGE = "\u8bc4\u5206\u5fc5\u987b\u5728 1.0 \u5230 5.0 \u4e4b\u95f4";

    private final ObservationRecordMapper observationRecordMapper;
    private final ObservationScoreMapper observationScoreMapper;
    private final ObservationTaskMapper observationTaskMapper;
    private final AuditLogService auditLogService;

    public ObservationRecordService(ObservationRecordMapper observationRecordMapper,
                                    ObservationScoreMapper observationScoreMapper,
                                    ObservationTaskMapper observationTaskMapper,
                                    AuditLogService auditLogService) {
        this.observationRecordMapper = observationRecordMapper;
        this.observationScoreMapper = observationScoreMapper;
        this.observationTaskMapper = observationTaskMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ObservationRecordVo saveDraft(RecordDraftRequest request) {
        LoginUser loginUser = requireMember();
        ObservationTask observationTask = validateTaskAccess(request.getTaskId(), request.getTeacherName(), loginUser);

        ObservationRecord observationRecord = buildRecord(
                request.getTaskId(),
                loginUser.getUserId(),
                request.getTeacherName(),
                emptyIfNull(request.getStrengths()),
                emptyIfNull(request.getWeaknesses()),
                emptyIfNull(request.getSuggestions()),
                RecordStatus.DRAFT.name(),
                null,
                null,
                null);
        ObservationRecord savedRecord = saveOrUpdateRecord(observationRecord);
        replaceScores(savedRecord.getId(), request.getScores());
        observationTaskMapper.updateStatus(observationTask.getId(), TaskStatus.IN_PROGRESS.name());
        auditLogService.write("RECORD", savedRecord.getId(), "SAVE_DRAFT", "\u4fdd\u5b58\u542c\u8bfe\u8bb0\u5f55\u8349\u7a3f");
        return toVo(savedRecord);
    }

    @Transactional
    public ObservationRecordVo submit(RecordSubmitRequest request) {
        validateForSubmit(request);
        LoginUser loginUser = requireMember();
        ObservationTask observationTask = validateTaskAccess(request.getTaskId(), request.getTeacherName(), loginUser);

        ObservationRecord observationRecord = buildRecord(
                request.getTaskId(),
                loginUser.getUserId(),
                request.getTeacherName(),
                request.getStrengths().trim(),
                request.getWeaknesses().trim(),
                request.getSuggestions().trim(),
                RecordStatus.SUBMITTED.name(),
                null,
                LocalDateTime.now(),
                null);
        ObservationRecord savedRecord = saveOrUpdateRecord(observationRecord);
        replaceScores(savedRecord.getId(), request.getScores());
        observationTaskMapper.updateStatus(observationTask.getId(), TaskStatus.COMPLETED.name());
        auditLogService.write("RECORD", savedRecord.getId(), "SUBMIT_RECORD", "\u63d0\u4ea4\u542c\u8bfe\u8bb0\u5f55");
        return toVo(savedRecord);
    }

    private LoginUser requireMember() {
        LoginUser loginUser = LoginUserContext.getRequired();
        if (loginUser.getRoleCode() != RoleCode.MEMBER) {
            throw new BusinessException(403, "\u53ea\u6709\u6210\u5458\u53ef\u4ee5\u586b\u5199\u542c\u8bfe\u8bb0\u5f55");
        }
        return loginUser;
    }

    private ObservationTask validateTaskAccess(Long taskId, String teacherName, LoginUser loginUser) {
        ObservationTask observationTask = observationTaskMapper.findById(taskId);
        if (observationTask == null) {
            throw new BusinessException(404, "\u542c\u8bfe\u4efb\u52a1\u4e0d\u5b58\u5728");
        }
        if (!loginUser.getUserId().equals(observationTask.getObserverId())) {
            throw new BusinessException(403, "\u4f60\u65e0\u6743\u586b\u5199\u8be5\u4efb\u52a1\u7684\u542c\u8bfe\u8bb0\u5f55");
        }
        if (!observationTask.getTeacherName().equals(teacherName)) {
            throw new BusinessException(400, "\u6388\u8bfe\u6559\u5e08\u4e0e\u4efb\u52a1\u4e0d\u5339\u914d");
        }
        return observationTask;
    }

    private ObservationRecord buildRecord(Long taskId,
                                          Long observerId,
                                          String teacherName,
                                          String strengths,
                                          String weaknesses,
                                          String suggestions,
                                          String status,
                                          String rejectReason,
                                          LocalDateTime submittedAt,
                                          LocalDateTime approvedAt) {
        ObservationRecord observationRecord = new ObservationRecord();
        observationRecord.setTaskId(taskId);
        observationRecord.setObserverId(observerId);
        observationRecord.setTeacherName(teacherName);
        observationRecord.setStrengths(strengths);
        observationRecord.setWeaknesses(weaknesses);
        observationRecord.setSuggestions(suggestions);
        observationRecord.setStatus(status);
        observationRecord.setRejectReason(rejectReason);
        observationRecord.setSubmittedAt(submittedAt);
        observationRecord.setApprovedAt(approvedAt);
        return observationRecord;
    }

    private ObservationRecord saveOrUpdateRecord(ObservationRecord candidate) {
        ObservationRecord existingRecord = observationRecordMapper.findByTaskIdAndObserverId(
                candidate.getTaskId(),
                candidate.getObserverId());
        if (existingRecord == null) {
            observationRecordMapper.insert(candidate);
            return candidate;
        }

        existingRecord.setTeacherName(candidate.getTeacherName());
        existingRecord.setStrengths(candidate.getStrengths());
        existingRecord.setWeaknesses(candidate.getWeaknesses());
        existingRecord.setSuggestions(candidate.getSuggestions());
        existingRecord.setStatus(candidate.getStatus());
        existingRecord.setRejectReason(candidate.getRejectReason());
        existingRecord.setSubmittedAt(candidate.getSubmittedAt());
        existingRecord.setApprovedAt(candidate.getApprovedAt());
        observationRecordMapper.update(existingRecord);
        return existingRecord;
    }

    private void replaceScores(Long recordId, List<ScoreInput> scores) {
        observationScoreMapper.deleteByRecordId(recordId);
        if (scores == null || scores.isEmpty()) {
            return;
        }
        for (ScoreInput score : scores) {
            ObservationScore observationScore = new ObservationScore();
            observationScore.setRecordId(recordId);
            observationScore.setDimensionCode(score.getDimensionCode());
            observationScore.setDimensionName(resolveDimensionName(score));
            observationScore.setScoreValue(score.getScoreValue());
            observationScoreMapper.insert(observationScore);
        }
    }

    private void validateForSubmit(RecordSubmitRequest request) {
        if (request.getScores() == null || request.getScores().size() != 5) {
            throw new BusinessException(400, SUBMIT_SCORE_COUNT_MESSAGE);
        }

        Set<String> uniqueCodes = new HashSet<>();
        for (ScoreInput score : request.getScores()) {
            if (score.getDimensionCode() == null || score.getScoreValue() == null) {
                throw new BusinessException(400, SUBMIT_SCORE_COUNT_MESSAGE);
            }
            DimensionCode dimensionCode;
            try {
                dimensionCode = DimensionCode.valueOf(score.getDimensionCode());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(400, "\u8bc4\u5206\u7ef4\u5ea6\u4e0d\u5408\u6cd5");
            }
            uniqueCodes.add(dimensionCode.name());
            BigDecimal scoreValue = score.getScoreValue();
            if (scoreValue.compareTo(new BigDecimal("1.0")) < 0
                    || scoreValue.compareTo(new BigDecimal("5.0")) > 0) {
                throw new BusinessException(400, SCORE_RANGE_MESSAGE);
            }
        }

        if (uniqueCodes.size() != DimensionCode.values().length) {
            throw new BusinessException(400, SUBMIT_SCORE_COUNT_MESSAGE);
        }
    }

    private String resolveDimensionName(ScoreInput score) {
        if (score.getDimensionName() != null && !score.getDimensionName().isBlank()) {
            return score.getDimensionName().trim();
        }
        return score.getDimensionCode();
    }

    private ObservationRecordVo toVo(ObservationRecord observationRecord) {
        List<ObservationScore> scores = observationScoreMapper.findByRecordId(observationRecord.getId());
        return ObservationRecordVo.builder()
                .id(observationRecord.getId())
                .taskId(observationRecord.getTaskId())
                .observerId(observationRecord.getObserverId())
                .teacherName(observationRecord.getTeacherName())
                .strengths(observationRecord.getStrengths())
                .weaknesses(observationRecord.getWeaknesses())
                .suggestions(observationRecord.getSuggestions())
                .status(observationRecord.getStatus())
                .rejectReason(observationRecord.getRejectReason())
                .submittedAt(observationRecord.getSubmittedAt())
                .approvedAt(observationRecord.getApprovedAt())
                .scores(new ArrayList<>(scores))
                .build();
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value.trim();
    }
}
