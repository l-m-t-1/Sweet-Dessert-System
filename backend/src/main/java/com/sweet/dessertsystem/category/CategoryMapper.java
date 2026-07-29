package com.sweet.dessertsystem.category;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT COUNT(*) FROM dessert WHERE category_id = #{categoryId}")
    long countDesserts(Long categoryId);
}
