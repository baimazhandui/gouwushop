package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.*;
import com.pinyougou.service.AddressService;
import com.pinyougou.service.ProvincesService;
import com.pinyougou.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference(timeout = 10000)
    private UserService userService;

    @Reference(timeout = 10000)
    private ProvincesService provincesService;


    @PostMapping("/save")
    public boolean save(@RequestBody User user, String smsCode) {
        try {
            boolean ok = userService
                    .checkSmsCode(user.getPhone(), smsCode);
            if (ok) {
                userService.save(user);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/sendCode")
    public boolean sendCode(String phone) {
        try {
            if (!StringUtils.isEmpty(phone)) {
                return userService.sendCode(phone);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/findUser")
    public Map<String, Object> findUser(HttpServletRequest request) {
        String username = request.getRemoteUser();
        Map<String, Object> map = new HashMap<>();
        User user = userService.findUser(username);
        map.put("user", user);
        return map;
    }

    @GetMapping("/findProvinces")
    public List<Provinces> findProvinces() {
        return provincesService.findProvinces();
    }

    @GetMapping("/findCities")
    public List<Cities> findCities(String provinceId) {
        return provincesService.findCities(provinceId);
    }

    @GetMapping("/findCAreas")
    public List<Areas> findCAreas(String cityId) {
        return provincesService.findCAreas(cityId);
    }

    @PostMapping("/saveMsg")
    public boolean saveMsg(@RequestBody User user) {
        try {
            userService.update(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/findAddressByUser")
    public List<Address> findAddressByUser(HttpServletRequest request) {
        String userId = request.getRemoteUser();
        return userService.findAddressByUser(userId);
    }

    @GetMapping("/updateDefault")
    public boolean updateDefault(HttpServletRequest request, Long id) {
        try {
            String username = request.getRemoteUser();
            userService.updateDefault(username, id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @PostMapping("/saveAddress")
    public boolean saveAddress(@RequestBody Address address, HttpServletRequest request) {
        try {
            address.setUserId(request.getRemoteUser());
            userService.saveAddress(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @PostMapping("/updateAddress")
    public boolean updateAddress(@RequestBody Address address) {
        try {
            userService.updateAddress(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/deleteAddress")
    public boolean deleteAddress(Long id) {
        try {
            userService.deleteAddress(id);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @PostMapping("/savePassword")
    public boolean savePassword(@RequestBody User user) {
        try {
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            userService.update(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/checkCodeAndSmsCode")
    public String checkCodeAndSmsCode(HttpServletRequest request, String phone, String code, String smsCode) {
        try {
            String checkCode = (String) request.getSession().getAttribute("CHECKCODE_SERVER");
            String username = request.getRemoteUser();
            if (!checkCode.equalsIgnoreCase(code)) {
                return "验证码错误!";
            }
            boolean b = userService.checkSmsCode(phone, smsCode);
            userService.saveMsg(username, phone);
            if (b) {
                return "验证成功!";
            } else {
                return "短信验证码错误!";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "操作失败";
    }
}
