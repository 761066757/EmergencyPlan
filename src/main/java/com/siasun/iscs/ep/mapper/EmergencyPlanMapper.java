package com.siasun.iscs.ep.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siasun.iscs.ep.dto.EmergencyPlanDTO;
import com.siasun.iscs.ep.entity.EmergencyPlan;
import com.siasun.iscs.ep.vo.EmergencyPlanQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liao
 * @version 1.0
 * @description: 预案Mapper
 * @date 2026/6/4
 */
@Mapper
public interface EmergencyPlanMapper extends BaseMapper<EmergencyPlan> {
    List<EmergencyPlanQueryVO> selectPlanQueryVOList(@Param("planDTO") EmergencyPlanDTO planDTO);

}