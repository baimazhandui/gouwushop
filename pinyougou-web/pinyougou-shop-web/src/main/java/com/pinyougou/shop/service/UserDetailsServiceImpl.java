package com.pinyougou.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Seller;
import com.pinyougou.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println(sellerService);
        // 创建List集合封装角色
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 添加角色
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        // 根据username查询商家
        Seller seller = sellerService.findOne(username);
        // 判断商家是否为空且是否已审核
        if (seller != null && "1".equals(seller.getStatus())) {
            return new User(username, seller.getPassword(), authorities);
        }
        return null;
    }
}
