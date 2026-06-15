package com.siasun.iscs.ep.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siasun.iscs.ep.entity.EmergencyPlanTaskRel;

import java.util.List;

/**
 * @description: 应急预案任务关联服务接口
 * @author liao
 * @date 2026/6/3
 * @version 1.0
 */

public interface EmergencyPlanTaskRelService extends IService<EmergencyPlanTaskRel> {

    /**
     * 批量保存关联关系（先删后加）
     */
    void saveRelations(String id, String station, List<EmergencyPlanTaskRel> relations);

}
