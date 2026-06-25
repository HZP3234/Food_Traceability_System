package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.TechTemplate;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface TechTemplateMapper extends BaseMapper<TechTemplate> {

    TechTemplate selectByTemplateName(@Param("templateName") String templateName);

    List<TechTemplate> selectByApplicableProduct(@Param("product") String product);

    List<TechTemplate> selectByTemplateStatus(@Param("status") Integer status);

}