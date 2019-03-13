package com.pinyougou.cart.service.impl;
import java.math.BigDecimal;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.Cart;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.OrderItem;
import com.pinyougou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.CartService")
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加 SKU 商品到购物车
     *
     * @param carts  购物车(一个 cart 对应一个商家)
     * @param itemId SKU 商品id
     * @param num    购买数据
     * @return List<Cart> 修改后的购物车
     */
    @Override
    public List<Cart> addItemToCart(List<Cart> carts,
                                    Long itemId, Integer num) {
        try {
            // 根据 SKU 商品 ID 查询 SKU 商品对象
            Item item = itemMapper.selectByPrimaryKey(itemId);
            // 获取商家id
            String sellerId = item.getSellerId();
            // 根据商家id判断购物车集合中是否存在该商家的购物车
            Cart cart = searchCartBySellerId(carts, sellerId);
            if (cart == null) { // 购物车集合中不存在该商家购物车
                // 创建新的购物车对象
                cart = new Cart();
                cart.setSellerId(item.getSellerId());
                cart.setSellerName(item.getSeller());
                // 创建订单明细(购物车中一个商品)
                OrderItem orderItem = createOrderItem(item, num);
                List<OrderItem> orderItems = new ArrayList<>(0);
                orderItems.add(orderItem);
                // 为购物车设置订单明细集合
                cart.setOrderItems(orderItems);
                // 将新的购物车对象添加到购物车集合
                carts.add(cart);
            } else { // 购物车集合中存在该商家购物车
                // 判断购物车订单明细集合中是否存在该商品
                OrderItem orderItem = searchOrderItemByItemId(cart.getOrderItems(), itemId);
                if (orderItem == null) {
                    // 如果没有,新增购物车订单明细
                    orderItem = createOrderItem(item, num);
                    cart.getOrderItems().add(orderItem);
                } else {
                    // 如果有,在原购物车订单明细上添加数量,更改金额
                    orderItem.setNum(orderItem.getNum() + num);
                    orderItem.setTotalFee(new BigDecimal(orderItem.getPrice()
                            .doubleValue() * orderItem.getNum()));
                    // 如果订单明细的购买数小于等于0,则删除
                    if (orderItem.getNum() <= 0) {
                        cart.getOrderItems().remove(orderItem);
                    }
                    // 如果cart的orderItems订单明细为0, 则删除cart
                    if (cart.getOrderItems().size() == 0) {
                        carts.remove(cart);
                    }
                }
            }
            return carts;
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }
    }

    /**
     * 用户已登录从Redis中获取购物车
     *
     * @param username
     * @return List<Cart>
     */
    @Override
    public List<Cart> findCartRedis(String username) {
        System.out.println("获取Redis中购物车: " + username);
        List<Cart> carts = (List<Cart>) redisTemplate
                .boundValueOps("cart_" + username).get();
        if (carts == null) {
            return new ArrayList<>(0);
        }
        return carts;
    }

    /**
     * 用户已登录,往Redis中存储购物车
     *
     * @param username
     * @param carts
     */
    @Override
    public void saveCartRedis(String username, List<Cart> carts) {
        System.out.println("往Redis中存储购物车: " + username);
        redisTemplate.boundValueOps("cart_" + username).set(carts);
    }

    /**
     * 合并购物车
     *  @param carts
     * @param cookieCarts
     */
    @Override
    public List<Cart> mergeCart(List<Cart> carts, List<Cart> cookieCarts) {
        for (Cart cart : cookieCarts) {
            for (OrderItem orderItem : cart.getOrderItems()) {
                carts = addItemToCart(carts, orderItem.getItemId(),
                        orderItem.getNum());
            }
        }
        return carts;
    }

    /**
     * 提交订单后更新数据
     *
     * @param carts
     * @param selectedIds
     * @return
     */
    @Override
    public List<Cart> updateCart(List<Cart> carts, Long[] selectedIds) {
        for (Long itemId : selectedIds) {
            // 根据 SKU 商品 ID 查询 SKU 商品对象
            Item item = itemMapper.selectByPrimaryKey(itemId);
            // 获取商家id
            String sellerId = item.getSellerId();
            // 根据商家id在购物车中查找商家购物车
            Cart cart = searchCartBySellerId(carts, sellerId);
            // 根据商品id获取商品订单
            OrderItem orderItem = searchOrderItemByItemId(cart.getOrderItems(), itemId);
            // 删除商品订单
            cart.getOrderItems().remove(orderItem);
            // 如果cart的orderItems订单明细为0, 则删除cart
            if (cart.getOrderItems().size() == 0) {
                carts.remove(cart);
            }
        }
        return carts;
    }

    // 从订单明细集合中获取指定订单明细
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItems, Long itemId) {
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getItemId().equals(itemId)) {
                return orderItem;
            }
        }
        return null;
    }

    // 创建订单明细
    private OrderItem createOrderItem(Item item, Integer num) {
        // 创建订单明细
        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setSellerId(item.getSellerId());
        // 小计
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));

        return orderItem;
    }

    // 从购物车集合中获取该商家的购物车
    private Cart searchCartBySellerId(
            List<Cart> carts, String sellerId) {
        for (Cart cart : carts) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

}
