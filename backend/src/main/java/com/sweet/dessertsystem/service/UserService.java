package com.sweet.dessertsystem.service;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sweet.dessertsystem.entity.User;
import com.sweet.dessertsystem.mapper.UserMapper;
import org.springframework.stereotype.Service;


@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}
