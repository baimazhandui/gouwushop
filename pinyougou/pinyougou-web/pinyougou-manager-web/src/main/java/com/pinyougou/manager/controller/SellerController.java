package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/seller")
public class SellerController {
    // 注入商家服务接口代理对象
    @Reference(timeout = 10000)
    private SellerService sellerService;

    @GetMapping("/findByPage")
    public PageResult findByPage(Seller seller, Integer page, Integer rows) {
        try {
            if (seller != null && !StringUtils.isEmpty(seller.getName())) {
                seller.setName(new String(seller.getName().getBytes("ISO8859-1"), "utf-8"));
            }
            if (seller != null && !StringUtils.isEmpty(seller.getNickName())) {
                seller.setNickName(new String(seller.getNickName().getBytes("ISO8859-1"), "utf-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sellerService.findByPage(seller, page, rows);
    }

    @GetMapping("/updateStatus")
    public boolean updateStatus(String sellerId, String status) {
        try {
            sellerService.updateStatus(sellerId, status);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
