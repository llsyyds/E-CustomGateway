package com.lls.user.controller;


import com.lls.gateway.client.core.ApiInvoker;
import com.lls.gateway.client.core.ApiProtocol;
import com.lls.gateway.client.core.ApiService;
import com.lls.user.dto.UserInfo;
import com.lls.user.model.User;
import com.lls.user.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@ApiService(serviceId = "backend-user-server", protocol = ApiProtocol.HTTP, patternPath = "/user/**")
public class UserController {
    private static final String SECRETKEY = "faewifheafewhefsfjkds";//一般不会直接写代码里，可以用一些安全机制来保护
    private static final String COOKIE_NAME = "user-jwt";
    private final UserService userService;

    @ApiInvoker(path = "/login")
    @PostMapping("/login")
    public UserInfo login(@RequestBody Map<String, String> requestBody,
                          HttpServletResponse response) {
        String nickname = requestBody.get("nickname");
        String phoneNumber = requestBody.get("phoneNumber");
        User user = userService.login(nickname,phoneNumber);
        var jwt = Jwts.builder()
            .setSubject(String.valueOf(user.getId()))
            .setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, SECRETKEY).compact();
        response.addCookie(new Cookie(COOKIE_NAME, jwt));
        return UserInfo.builder()
            .id(user.getId())
            .nickname(user.getNickname())
            .phoneNumber(user.getPhoneNumber()).build();
    }

    @GetMapping("/private/user-info")
    public UserInfo getUserInfo(@RequestHeader("userId") String userId) {
        log.info("userId :{}", userId);
        var user = userService.getUser(Long.parseLong(userId));
        return UserInfo.builder()
            .id(user.getId())
            .nickname(user.getNickname())
            .phoneNumber(user.getPhoneNumber()).build();
    }
}
