package com.sweet.dessertsystem.controller;


import com.sweet.dessertsystem.entity.User;
import com.sweet.dessertsystem.common.ApiResponse;
import com.sweet.dessertsystem.dto.UserView;
import com.sweet.dessertsystem.exception.BusinessException;
import com.sweet.dessertsystem.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {


    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }



    @GetMapping("/list")
    public ApiResponse<List<UserView>> list(){

        return ApiResponse.ok(userService.list().stream()
                .map(UserView::from)
                .toList());
    }



    // 登录接口
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody User user){
        User authenticated = userService.login(
                user.getUsername(),
                user.getPassword()
        );
        if (authenticated == null) {
            throw new BusinessException("用户名或密码错误");
        }
        return ApiResponse.ok(Map.of(
                "id", authenticated.getId(),
                "username", authenticated.getUsername(),
                "role", authenticated.getRole()
        ));
    }


}
