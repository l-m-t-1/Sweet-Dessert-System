package com.sweet.dessertsystem.controller;


import com.sweet.dessertsystem.common.ApiResponse;
import com.sweet.dessertsystem.dto.UserView;
import com.sweet.dessertsystem.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/user")
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

}
