package com.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emergency.entity.EmergencyPlan;
import org.apache.ibatis.annotations.Mapper;

// 预案Mapper
@Mapper
public interface EmergencyPlanMapper extends BaseMapper<EmergencyPlan> {
}