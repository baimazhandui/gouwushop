package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.service.CartService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout = 30000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    // 添加 SKU 商品到购物车
    @GetMapping("/addCart")
    public boolean addCart(Long itemId, Integer num) {
        try {
            // 设置允许访问的域名
            response.setHeader("Access-Control-Allow-Origin",
                    "http://item.pinyougou.com");
            // 设置允许操作Cookie
            response.setHeader("Access-Control-Allow-Credentials",
                    "true");
            // 获取登录用户名
            String username = request.getRemoteUser();
            // 获取购物车集合
            List<Cart> carts = findCart();
            // 调用服务层添加 SKU 商品到购物车
            carts = cartService.addItemToCart(carts, itemId, num);
            if (StringUtils.isNoneBlank(username)) { // 已登录
                // 往Redis中存储购物车
                cartService.saveCartRedis(username, carts);
            } else { // 未登录
                // 将购物车重新存入 Cookie 中
                CookieUtils.setCookie(request, response,
                        CookieUtils.CookieName.PINYOUGOU_CART,
                        JSON.toJSONString(carts), 3600 * 24, true);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取购物车集合
    @GetMapping("/findCart")
    public List<Cart> findCart() {
        // 获取登录用户名
        String username = request.getRemoteUser();
        // 定义购物车集合
        List<Cart> carts = null;
        // 判断用户是否登录
        if (StringUtils.isNoneBlank(username)) { // 已登录
            // 从Redis获取购物车
            carts = cartService.findCartRedis(username);
            // 从Cookie中获取购物车集合json字符串
            String cartStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            // 判断是否为空
            if (StringUtils.isNoneBlank(cartStr)) { // 不为空
                // json字符串转化成List集合
                List<Cart> cookieCarts = JSON.parseArray(cartStr, Cart.class);
                // 合并购物车
                carts = cartService.mergeCart(carts, cookieCarts);
                // 将合并后的购物车存入Redis
                cartService.saveCartRedis(username, carts);
                // 删除Cookie中存储的购物车
                CookieUtils.deleteCookie(request, response,
                        CookieUtils.CookieName.PINYOUGOU_CART);
            }
        } else { // 未登录
            // 从 Cookie 中获取购物车集合json字符串
            String cartStr = CookieUtils.getCookieValue(request,
                    CookieUtils.CookieName.PINYOUGOU_CART, true);
            // 判断是否为空
            if (StringUtils.isBlank(cartStr)) {
                cartStr = "[]";
            }
            carts = JSON.parseArray(cartStr, Cart.class);
        }
        return carts;
    }
}
