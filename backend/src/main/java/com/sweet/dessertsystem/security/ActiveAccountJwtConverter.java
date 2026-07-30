package com.sweet.dessertsystem.security;

import com.sweet.dessertsystem.entity.User;
import com.sweet.dessertsystem.mapper.UserMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActiveAccountJwtConverter
        implements Converter<Jwt, AbstractOAuth2TokenAuthenticationToken<Jwt>> {
    private final UserMapper userMapper;

    public ActiveAccountJwtConverter(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public AbstractOAuth2TokenAuthenticationToken<Jwt> convert(Jwt jwt) {
        Long userId;
        try {
            userId = Long.valueOf(jwt.getSubject());
        } catch (RuntimeException exception) {
            throw new BadCredentialsException("登录凭证无效");
        }
        User user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BadCredentialsException("账号已停用");
        }
        return new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                user.getUsername());
    }
}
