package com.sweet.dessertsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.dessertsystem.dto.DessertView;
import com.sweet.dessertsystem.entity.Dessert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DessertMapper extends BaseMapper<Dessert> {

    @Select("SELECT * FROM dessert WHERE id = #{id} FOR UPDATE")
    Dessert findByIdForUpdate(Long id);

    @Select("""
            <script>
            SELECT d.id, d.name, d.category_id, c.name AS category_name,
                   d.price, d.stock, d.image, d.description, d.status,
                   d.create_time, d.update_time
            FROM dessert d
            JOIN category c ON c.id = d.category_id
            WHERE 1 = 1
            <if test='name != null and name != ""'>
              AND d.name LIKE CONCAT('%', #{name}, '%')
            </if>
            <if test='categoryId != null'>
              AND d.category_id = #{categoryId}
            </if>
            ORDER BY d.update_time DESC, d.id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<DessertView> findPage(@Param("name") String name,
                               @Param("categoryId") Long categoryId,
                               @Param("offset") long offset,
                               @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM dessert d
            WHERE 1 = 1
            <if test='name != null and name != ""'>
              AND d.name LIKE CONCAT('%', #{name}, '%')
            </if>
            <if test='categoryId != null'>
              AND d.category_id = #{categoryId}
            </if>
            </script>
            """)
    long countPage(@Param("name") String name, @Param("categoryId") Long categoryId);

    @Select("""
            SELECT d.id, d.name, d.category_id, c.name AS category_name,
                   d.price, d.stock, d.image, d.description, d.status,
                   d.create_time, d.update_time
            FROM dessert d JOIN category c ON c.id = d.category_id
            WHERE d.id = #{id}
            """)
    DessertView findViewById(Long id);
}
