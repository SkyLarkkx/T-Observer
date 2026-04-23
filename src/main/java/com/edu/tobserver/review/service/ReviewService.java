package com.edu.tobserver.review.service;

import com.edu.tobserver.audit.service.AuditLogService;
import com.edu.tobserver.auth.mapper.UserMapper;
import com.edu.tobserver.common.context.LoginUser;
import com.edu.tobserver.common.context.LoginUserContext;
import com.edu.tobserver.common.enums.RecordStatus;
import com.edu.tobserver.common.enums.RoleCode;
import com.edu.tobserver.common.enums.TaskStatus;
import com.edu.tobserver.common.exception.BusinessException;
import com.edu.tobserver.record.entity.ObservationRecord;
import com.edu.tobserver.record.entity.ObservationScore;
import com.edu.tobserver.record.mapper.ObservationRecordMapper;
import com.edu.tobserver.record.mapper.ObservationScoreMapper;
import com.edu.tobserver.record.vo.ObservationRecordVo;
import com.edu.tobserver.review.vo.ReviewListItemVo;
import com.edu.tobserver.task.mapper.ObservationTaskMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ObservationRecordMapper observationRecordMapper;
    private final ObservationScoreMapper observationScoreMapper;
    private final ObservationTaskMapper observationTaskMapper;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;

    public ReviewService(ObservationRecordMapper observationRecordMapper,
                         ObservationScoreMapper observationScoreMapper,
                         ObservationTaskMapper observationTaskMapper,
                         AuditLogService auditLogService,
                         UserMapper userMapper) {
        this.observationRecordMapper = observationRecordMapper;
        this.observationScoreMapper = observationScoreMapper;
        this.observationTaskMapper = observationTaskMapper;
        this.auditLogService = auditLogService;
        this.userMapper = userMapper;
    }

    public List<ReviewListItemVo> findList() {
        LoginUser loginUser = requireLeader();
        return observationRecordMapper.findReviewItemsByLeaderId(loginUser.getUserId());
    }

    public ObservationRecordVo findDetail(Long recordId) {
        requireLeader();
        ObservationRecord observationRecord = observationRecordMapper.findById(recordId);
        if (observationRecord == null) {
            throw new BusinessException(404, "听课记录不存在");
        }
        return toVo(observationRecord);
    }

    @Transactional
    public ObservationRecordVo approve(Long recordId) {
        requireLeader();
        ObservationRecord observationRecord = requireSubmittedRecord(recordId);
        observationRecord.setStatus(RecordStatus.APPROVED.name());
        observationRecord.setRejectReason(null);
        observationRecord.setApprovedAt(LocalDateTime.now());
        observationRecordMapper.update(observationRecord);
        auditLogService.write("RECORD", observationRecord.getId(), "APPROVE_RECORD", "通过听课记录");
        return toVo(observationRecord);
    }

    @Transactional
    public ObservationRecordVo reject(Long recordId, String reason) {
        requireLeader();
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(400, "退回时必须填写原因");
        }
        ObservationRecord observationRecord = requireSubmittedRecord(recordId);
        observationRecord.setStatus(RecordStatus.RETURNED.name());
        observationRecord.setRejectReason(reason.trim());
        observationRecord.setApprovedAt(null);
        observationRecordMapper.update(observationRecord);
        observationTaskMapper.updateStatus(observationRecord.getTaskId(), TaskStatus.IN_PROGRESS.name());
        auditLogService.write("RECORD", observationRecord.getId(), "REJECT_RECORD", "退回听课记录");
        return toVo(observationRecord);
    }

    private LoginUser requireLeader() {
        LoginUser loginUser = LoginUserContext.getRequired();
        if (loginUser.getRoleCode() != RoleCode.LEADER) {
            throw new BusinessException(403, "只有组长可以审核听课记录");
        }
        return loginUser;
    }

    private ObservationRecord requireSubmittedRecord(Long recordId) {
        ObservationRecord observationRecord = observationRecordMapper.findById(recordId);
        if (observationRecord == null) {
            throw new BusinessException(404, "听课记录不存在");
        }
        if (!RecordStatus.SUBMITTED.name().equals(observationRecord.getStatus())) {
            throw new BusinessException(400, "只有已提交的听课记录可以审核");
        }
        return observationRecord;
    }

    private ObservationRecordVo toVo(ObservationRecord observationRecord) {
        List<ObservationScore> scores = observationScoreMapper.findByRecordId(observationRecord.getId());
        return ObservationRecordVo.builder()
                .id(observationRecord.getId())
                .taskId(observationRecord.getTaskId())
                .observerId(observationRecord.getObserverId())
                .observerName(userMapper.findRealNameById(observationRecord.getObserverId()))
                .teacherName(observationRecord.getTeacherName())
                .strengths(observationRecord.getStrengths())
                .weaknesses(observationRecord.getWeaknesses())
                .suggestions(observationRecord.getSuggestions())
                .status(observationRecord.getStatus())
                .rejectReason(observationRecord.getRejectReason())
                .submittedAt(observationRecord.getSubmittedAt())
                .approvedAt(observationRecord.getApprovedAt())
                .scores(scores)
                .build();
    }
}
