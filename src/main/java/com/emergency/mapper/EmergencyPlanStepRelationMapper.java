package com.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emergency.entity.EmergencyPlanStepRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

// 预案-步骤关联Mapper
@Mapper
public interface EmergencyPlanStepRelationMapper extends BaseMapper<EmergencyPlanStepRelation> {
    // 根据预案ID逻辑删除关联
    @Update("UPDATE public.emergency_plan_step_relation SET is_deleted = 1 WHERE plan_id = #{planId}")
    void deleteByPlanId(@Param("planId") String planId);

    // 根据预案ID查询有效关联
    @Select("SELECT * FROM public.emergency_plan_step_relation WHERE plan_id = #{planId} AND is_deleted = 0")
    List<EmergencyPlanStepRelation> selectByPlanId(@Param("planId") String planId);
}
