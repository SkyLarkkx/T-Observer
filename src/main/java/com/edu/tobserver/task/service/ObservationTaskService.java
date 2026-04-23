package com.edu.tobserver.task.service;

import com.edu.tobserver.audit.service.AuditLogService;
import com.edu.tobserver.common.api.PageResult;
import com.edu.tobserver.common.context.LoginUser;
import com.edu.tobserver.common.context.LoginUserContext;
import com.edu.tobserver.common.enums.RoleCode;
import com.edu.tobserver.common.enums.TaskStatus;
import com.edu.tobserver.common.exception.BusinessException;
import com.edu.tobserver.task.dto.TaskCreateRequest;
import com.edu.tobserver.task.dto.TaskQueryRequest;
import com.edu.tobserver.task.entity.ObservationTask;
import com.edu.tobserver.task.mapper.ObservationTaskMapper;
import com.edu.tobserver.task.vo.TaskListItemVo;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ObservationTaskService {

    private final ObservationTaskMapper observationTaskMapper;
    private final AuditLogService auditLogService;

    public ObservationTaskService(ObservationTaskMapper observationTaskMapper, AuditLogService auditLogService) {
        this.observationTaskMapper = observationTaskMapper;
        this.auditLogService = auditLogService;
    }

    public ObservationTask create(TaskCreateRequest request) {
        LoginUser loginUser = LoginUserContext.getRequired();
        if (loginUser.getRoleCode() != RoleCode.LEADER) {
            throw new BusinessException(403, "只有组长可以创建听课任务");
        }

        ObservationTask observationTask = new ObservationTask();
        observationTask.setTitle(request.getTitle());
        observationTask.setLeaderId(loginUser.getUserId());
        observationTask.setObserverId(request.getObserverId());
        observationTask.setTeacherName(request.getTeacherName());
        observationTask.setCourseName(request.getCourseName());
        observationTask.setLessonTime(request.getLessonTime());
        observationTask.setDeadline(request.getDeadline());
        observationTask.setStatus(TaskStatus.PENDING.name());
        observationTask.setRemark(request.getRemark());
        observationTaskMapper.insert(observationTask);

        auditLogService.write("TASK", observationTask.getId(), "CREATE_TASK", "创建听课任务");
        return observationTask;
    }

    public PageResult<TaskListItemVo> list(TaskQueryRequest request) {
        LoginUser loginUser = LoginUserContext.getRequired();
        Long leaderId = null;
        Long observerId = request.getObserverId();
        if (loginUser.getRoleCode() == RoleCode.LEADER) {
            leaderId = loginUser.getUserId();
        } else if (loginUser.getRoleCode() == RoleCode.MEMBER) {
            observerId = loginUser.getUserId();
        }
        int pageNum = normalizePageNum(request.getPageNum());
        int pageSize = normalizePageSize(request.getPageSize());
        int offset = (pageNum - 1) * pageSize;
        long total = observationTaskMapper.countList(leaderId, observerId, request.getStatus());
        List<TaskListItemVo> list = observationTaskMapper.findPage(
                leaderId,
                observerId,
                request.getStatus(),
                pageSize,
                offset);
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    private int normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            return 1;
        }
        return pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
