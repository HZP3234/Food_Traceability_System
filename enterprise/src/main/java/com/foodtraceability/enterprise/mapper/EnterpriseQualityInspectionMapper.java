package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.QualityInspection;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnterpriseQualityInspectionMapper extends BaseMapper<QualityInspection> {

    QualityInspection selectByInspectionNo(@Param("inspectionNo") String inspectionNo);

    List<QualityInspection> selectByBizBatch(@Param("bizType") Integer bizType, @Param("bizBatchNo") String bizBatchNo);

    List<QualityInspection> selectByBizType(@Param("bizType") Integer bizType);

    List<QualityInspection> selectByInspectionResult(@Param("result") Integer result);

}