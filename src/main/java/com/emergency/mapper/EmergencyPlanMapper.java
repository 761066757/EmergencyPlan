package com.emergency.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.emergency.entity.EmergencyPlan;
import com.emergency.vo.PlanQueryVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// 预案Mapper
@Mapper
public interface EmergencyPlanMapper extends BaseMapper<EmergencyPlan> {
    List<PlanQueryVO> selectPlanQueryVOList();
}