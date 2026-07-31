package com.sweet.dessertsystem.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sweet.dessertsystem.dto.UserView;
import com.sweet.dessertsystem.entity.User;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.mapper.UserMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public UserView register(RegisterRequest request) {
        String username = normalizeUsername(request == null ? null : request.username());
        String password = validatePassword(request == null ? null : request.password());
        if (findByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setStatus(1);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("用户名已存在");
        }
        return UserView.from(user);
    }

    public AuthResponse login(LoginRequest request) {
        String username = normalizeUsername(request == null ? null : request.username());
        String password = request == null ? null : request.password();
        User user = findByUsername(username);
        if (user == null || password == null
                || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已停用");
        }
        return new AuthResponse(tokenService.issue(user), user.getId(),
                user.getUsername(), user.getRole());
    }

    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    private String normalizeUsername(String value) {
        String username = value == null ? "" : value.trim();
        if (username.length() < 3 || username.length() > 30) {
            throw new BusinessException("用户名长度必须为3到30个字符");
        }
        return username;
    }

    private String validatePassword(String value) {
        if (value == null || value.length() < 6 || value.length() > 72) {
            throw new BusinessException("密码长度必须为6到72个字符");
        }
        return value;
    }
}
