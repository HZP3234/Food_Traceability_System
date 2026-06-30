package com.foodtraceability.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.foodtraceability.enterprise.entity.ProdMaterialInput;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProdMaterialInputMapper extends BaseMapper<ProdMaterialInput> {

    ProdMaterialInput selectByRawBatchNo(@Param("batchNo") String batchNo);

    List<ProdMaterialInput> selectByMaterialName(@Param("materialName") String materialName);

    List<ProdMaterialInput> selectByProdBatchNo(@Param("prodBatchNo") String prodBatchNo);

}