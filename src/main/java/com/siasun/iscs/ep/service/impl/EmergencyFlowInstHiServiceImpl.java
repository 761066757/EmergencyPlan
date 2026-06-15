package com.siasun.iscs.ep.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siasun.iscs.ep.dto.EmergencyFlowInstHiDTO;
import com.siasun.iscs.ep.entity.EmergencyFlowInstHi;
import com.siasun.iscs.ep.mapper.EmergencyFlowInstHiMapper;
import com.siasun.iscs.ep.service.EmergencyFlowInstHiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author liao
 * @version 1.0
 * @description: 流程实例历史服务实现类
 * @date 2026/6/10
 */
@Slf4j
@Service
public class EmergencyFlowInstHiServiceImpl extends ServiceImpl<EmergencyFlowInstHiMapper, EmergencyFlowInstHi> implements EmergencyFlowInstHiService {

    /**
     * 查询流程实例历史列表
     */
    @Override
    public List<EmergencyFlowInstHi> queryList(EmergencyFlowInstHiDTO dto) {

        return this.list(new QueryWrapper<EmergencyFlowInstHi>()
                .eq("is_deleted", 0)
                .eq("is_history", 1)
                .like(StringUtils.isNotBlank(dto.getPlanName()), "plan_name", dto.getPlanName())
                .ge(dto.getStartTime() != null, "start_time", dto.getStartTime())
                .le(dto.getEndTime() != null, "end_time", dto.getEndTime())
                .orderByDesc("start_time"));
    }

    /**
     * 异步保存录像回放
     */
    @Async
    @Override
    public void asyncSaveVideoPlayback(String instHiId) {
        try {
            log.info("开始异步保存录像回放, instHiId={}", instHiId);
            EmergencyFlowInstHi instHi = this.getById(instHiId);
            if (instHi == null || StringUtils.isBlank(instHi.getCameraIds())) {
                log.warn("流程实例历史记录不存在或无摄像头, instHiId={}", instHiId);
                return;
            }
            String[] cameraIds = instHi.getCameraIds().split(";");
            LocalDateTime startTime = instHi.getStartTime();
            LocalDateTime endTime = instHi.getEndTime();
            for (String cameraId : cameraIds) {
                // TODO: 调用第三方服务获取录像回放url并更新 video_urls 字段-待完善
                log.info("异步保存录像回放完成, cameraId={}", cameraId);
            }
        } catch (Exception e) {
            log.error("异步保存录像回放失败, instHiId={}", instHiId, e);
        }
    }
}
