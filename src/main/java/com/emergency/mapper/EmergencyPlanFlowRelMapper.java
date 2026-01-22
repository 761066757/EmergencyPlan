package com.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emergency.entity.EmergencyPlanFlowRel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
// 流程关联Mapper
public interface EmergencyPlanFlowRelMapper extends BaseMapper<EmergencyPlanFlowRel> {}
