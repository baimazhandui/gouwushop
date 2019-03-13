package com.pinyougou.service;

import com.pinyougou.cart.Cart;

import java.util.List;

// 购物车服务接口
public interface CartService {

    /**
     * 添加 SKU 商品到购物车
     * @param carts 购物车(一个 cart 对应一个商家)
     * @param itemId SKU 商品id
     * @param num 购买数据
     * @return List<Cart> 修改后的购物车
     */
    List<Cart> addItemToCart(List<Cart> carts, Long itemId, Integer num);

    /**
     * 用户已登录从Redis中获取购物车
     * @param username
     * @return List<Cart>
     */
    List<Cart> findCartRedis(String username);

    /**
     * 用户已登录,往Redis中存储购物车
     * @param username
     * @param carts
     */
    void saveCartRedis(String username, List<Cart> carts);

    /**
     * 合并购物车
     * @param carts
     * @param cookieCarts
     */
    List<Cart> mergeCart(List<Cart> carts, List<Cart> cookieCarts);

    /**
     * 提交订单后更新数据
     * @param carts
     * @param selectedIds
     * @return
     */
    List<Cart> updateCart(List<Cart> carts, Long[] selectedIds);
}
