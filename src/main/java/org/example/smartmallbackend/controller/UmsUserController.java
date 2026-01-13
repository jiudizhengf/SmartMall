package org.example.smartmallbackend.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.smartmallbackend.common.Result;
import org.example.smartmallbackend.entity.UmsUser;
import org.example.smartmallbackend.service.UmsUserService;
import org.example.smartmallbackend.util.JwtUtil;

import org.example.smartmallbackend.vo.LoginResultVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户的增删改查接口")
@RestController
@RequestMapping("/api/ums/user")
@RequiredArgsConstructor
public class UmsUserController {
    private final UmsUserService umsUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@RequestParam String username, @RequestParam String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return Result.error("用户名或密码不能为空");
        }
        //幂等性检查
        long count = umsUserService.count(new LambdaQueryWrapper<UmsUser>().eq(UmsUser::getUsername, username));
        if (count > 0) {
            return Result.error("用户名已存在");
        }
        UmsUser user = new UmsUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickName(username);
        umsUserService.save(user);
        return Result.success("注册成功");
    }

    @Operation(summary ="用户登录")
    @PostMapping("/login")
    public Result<?> login(@RequestParam String username, @RequestParam String password) {
        UmsUser user = umsUserService.getOne(new LambdaQueryWrapper<UmsUser>().eq(UmsUser::getUsername, username));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        //生成token
        String token = jwtUtil.createToken(user.getId(), user.getUsername());

        LoginResultVO loginResultVO = new LoginResultVO();
        loginResultVO.setToken(token);
        loginResultVO.setId(user.getId());
        loginResultVO.setUsername(user.getUsername());
        loginResultVO.setNickName(user.getNickName());
        return Result.success(loginResultVO);
    }
}
