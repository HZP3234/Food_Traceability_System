package com.foodtraceability.regulation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.regulation.entity.QualificationFile;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface QualificationFileMapper extends BaseMapper<QualificationFile> {

    List<QualificationFile> selectByEnterpriseUuid(@Param("uuid") String uuid);

    List<QualificationFile> selectByQualificationType(@Param("type") Integer type);

    List<QualificationFile> selectByAuditState(@Param("state") Integer state);

    List<QualificationFile> selectByQualificationState(@Param("state") Integer state);

}