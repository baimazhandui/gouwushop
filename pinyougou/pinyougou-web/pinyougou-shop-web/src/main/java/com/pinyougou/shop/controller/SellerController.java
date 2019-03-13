package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/seller")
public class SellerController {
    // 注入商家服务接口代理对象
    @Reference(timeout = 10000)
    private SellerService sellerService;

    @PostMapping("/save")
    public boolean save(@RequestBody Seller seller) {
        try {
            // 密码加密
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String password = passwordEncoder.encode(seller.getPassword());
            seller.setPassword(password);
            sellerService.save(seller);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/findSeller")
    public Seller findSeller(HttpServletRequest request) {
        String sellerId = request.getRemoteUser();
        return sellerService.findSeller(sellerId);
    }

    @PostMapping("/saveSellerMsg")
    public boolean saveSellerMsg(@RequestBody Seller seller) {
        try {
            sellerService.saveSellerMsg(seller);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @PostMapping("/changePassword")
    public String changePassword(HttpServletRequest request, String oldPassword, String newPassword) {
        String sellerId = request.getRemoteUser();
        return sellerService.changePassword(sellerId, oldPassword, newPassword);
    }
}
