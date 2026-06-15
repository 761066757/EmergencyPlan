package com.siasun.iscs.ep.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siasun.iscs.ep.entity.EmergencyPlanTaskRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author liao
 * @version 1.0
 * @description: 预案与任务关联Mapper
 * @date 2026/6/3
 */
@Mapper
public interface EmergencyPlanTaskRelMapper extends BaseMapper<EmergencyPlanTaskRel> {

    /**
     * 根据预案ID逻辑删除原关联任务
     */
    @Update("UPDATE public.emergency_plan_task_rel SET is_deleted = 1 WHERE plan_id = #{planId}")
    void deleteByPlanId(@Param("planId") String planId);

}
