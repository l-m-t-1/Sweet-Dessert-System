package com.sweet.dessertsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sweet.dessertsystem.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    @Select("""
            <script>
            SELECT id, username, role, status, create_time, update_time
            FROM user
            WHERE role = 'USER'
            <if test="keyword != null">
              AND username LIKE CONCAT('%', #{keyword}, '%')
            </if>
            ORDER BY create_time DESC, id DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<User> findUsers(@Param("keyword") String keyword,
                         @Param("offset") long offset,
                         @Param("size") long size);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM user
            WHERE role = 'USER'
            <if test="keyword != null">
              AND username LIKE CONCAT('%', #{keyword}, '%')
            </if>
            </script>
            """)
    long countUsers(@Param("keyword") String keyword);
}
