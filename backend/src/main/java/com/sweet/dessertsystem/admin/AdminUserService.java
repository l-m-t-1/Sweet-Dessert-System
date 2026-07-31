package com.sweet.dessertsystem.admin;

import com.sweet.dessertsystem.dto.UserView;
import com.sweet.dessertsystem.entity.User;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {
    private final UserMapper userMapper;

    public AdminUserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public AdminUserPageResult page(long page, long size, String keyword) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);
        String safeKeyword = blankToNull(keyword);
        return new AdminUserPageResult(
                userMapper.findUsers(safeKeyword, (safePage - 1) * safeSize, safeSize)
                        .stream()
                        .map(UserView::from)
                        .toList(),
                userMapper.countUsers(safeKeyword),
                safePage,
                safeSize);
    }

    @Transactional
    public void changeStatus(Long id, Integer status, String currentUsername) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("账号状态只能是启用或停用");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException("不能停用当前登录账号");
        }
        if (!"USER".equals(user.getRole())) {
            throw new BusinessException("管理员账号不可修改");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
