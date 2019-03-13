package com.pinyougou.cart.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    // 显示用户名
    @GetMapping("/user/showName")
    public Map<String, String> showName(HttpServletRequest httpServletRequest) {
        String name = httpServletRequest.getRemoteUser();
        Map<String, String> map = new HashMap<>();
        map.put("loginName", name);

        return map;
    }
}
