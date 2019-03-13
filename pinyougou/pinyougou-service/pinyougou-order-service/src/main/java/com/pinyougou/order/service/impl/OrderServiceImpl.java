package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.cart.Cart;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.*;
import com.pinyougou.orders.Orders;
import com.pinyougou.pojo.*;
import com.pinyougou.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.OrderService")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private PayLogMapper payLogMapper;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private SellerMapper sellerMapper;

    /**
     * 添加方法
     *
     * @param order
     * @param itemIds
     */
    @Override
    public void save(Order order, Long[] itemIds) {
        try {
            // 根据登录用户名获取 redis 中购物车数据
            List<Cart> carts = (List<Cart>) redisTemplate
                    .boundValueOps("cart_" + order.getUserId()).get();
            // 定义订单 ID 集合(一次支付对应多个订单)
            List<String> orderIdList = new ArrayList<>();
            // 定义多个订单支付的总金额(元)
            double totalMoney = 0;

            // 迭代购物车
            for (Cart cart : carts) {
                // 查询选中商品中是否存在该商家商品
                boolean flag = isSelectedCart(cart, itemIds);
                if (flag) {
                    /** ####### 往订单表插入数据 ######### */
                    // 生成新的订单
                    Order order1 = new Order();
                    // 生成订单主键id
                    long orderId = idWorker.nextId();
                    // 设置订单id
                    order1.setOrderId(orderId);
                    // 设置支付类型
                    order1.setPaymentType(order.getPaymentType());
                    // 设置支付状态码为“未支付”
                    order1.setStatus("1");
                    // 设置订单创建时间
                    order1.setCreateTime(new Date());
                    // 设置订单修改时间
                    order1.setUpdateTime(order1.getCreateTime());
                    // 设置用户名
                    order1.setUserId(order.getUserId());
                    // 设置收件人地址
                    order1.setReceiverAreaName(order.getReceiverAreaName());
                    // 设置收件人手机号码
                    order1.setReceiverMobile(order.getReceiverMobile());
                    // 设置收件人
                    order1.setReceiver(order.getReceiver());
                    // 设置订单来源
                    order1.setSourceType(order.getSourceType());
                    // 设置商家id
                    order1.setSellerId(cart.getSellerId());
                    // 定义该订单总金额
                    double money = 0;
                    /** ####### 往订单明细表插入数据 ######### */
                    for (OrderItem orderItem : cart.getOrderItems()) {
                        if (isSelectedOrderItem(orderItem, itemIds)) {
                            // 设置主键id
                            orderItem.setId(idWorker.nextId());
                            // 设置关联的订单id
                            orderItem.setOrderId(orderId);
                            // 累计总金额
                            money += orderItem.getTotalFee().doubleValue();
                            // 保存数据到订单明细表
                            orderItemMapper.insertSelective(orderItem);
                        }
                    }
                    // 设置支付总金额
                    order1.setPayment(new BigDecimal(money));
                    // 保存数据到订单表
                    orderMapper.insertSelective(order1);
                    // 记录订单 id
                    orderIdList.add(String.valueOf(orderId));
                    // 记录总金额
                    totalMoney += money;
                }
            }
            // 判断是否为微信支付
            if ("1".equals(order.getPaymentType())) {
                // 创建支付日志对象
                PayLog payLog = new PayLog();
                // 生成订单交易号
                String outTradeNo = String.valueOf(idWorker.nextId());
                // 设置订单交易号
                payLog.setOutTradeNo(outTradeNo);
                // 创建时间
                payLog.setCreateTime(new Date());
                // 支付总金额(分)
                payLog.setTotalFee((long) (totalMoney * 100));
                // 用户ID
                payLog.setUserId(order.getUserId());
                // 支付状态
                payLog.setTradeState("0");
                // 订单号集合，逗号分隔
                String ids = orderIdList.toString().replace("[", "")
                        .replace("]", "").replace(" ", "");
                // 设置订单号
                payLog.setOrderList(ids);
                // 支付类型
                payLog.setPayType("1");
                // 往支付日志表插入数据
                payLogMapper.insertSelective(payLog);
                // 存入缓存
                redisTemplate.boundValueOps("payLog_" + order.getUserId()).set(payLog);
            }

            for (Long itemId : itemIds) {
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
            saveCartRedis(order.getUserId(), carts);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveCartRedis(String username, List<Cart> carts) {
        redisTemplate.boundValueOps("cart_" + username).set(carts);
    }

    private boolean isSelectedOrderItem(OrderItem orderItem, Long[] itemIds) {
        for (Long itemId : itemIds) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSelectedCart(Cart cart, Long[] itemIds) {
        for (OrderItem orderItem : cart.getOrderItems()) {
            for (Long itemId : itemIds) {
                if (orderItem.getItemId().longValue() == itemId.longValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 修改方法
     *
     * @param order
     */
    @Override
    public void update(Order order) {

    }

    /**
     * 根据主键id删除
     *
     * @param id
     */
    @Override
    public void delete(Serializable id) {

    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void deleteAll(Serializable[] ids) {

    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public Order findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<Order> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *  @param userId
     * @param page
     * @param rows
     */
    @Override
    public PageResult findByPage(String userId, int page, int rows) {
        try {
            PageHelper.startPage(page, rows);
            List<Orders> ordersList = new ArrayList<>();
            Order or = new Order();
            or.setUserId(userId);
            List<Order> orderList = orderMapper.select(or);
            for (Order order1 : orderList) {
                Orders orders = new Orders();
                orders.setOrderId(String.valueOf(order1.getOrderId()));
                orders.setUpdateTime(order1.getUpdateTime());
                Seller seller = sellerMapper.selectByPrimaryKey(order1.getSellerId());
                orders.setNickName(seller.getNickName());
                orders.setStatus(order1.getStatus());
                orders.setPayment(order1.getPayment());
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order1.getOrderId());
                List<OrderItem> orderItemList = orderItemMapper.select(orderItem);
                orders.setOrderItems(orderItemList);
                ordersList.add(orders);
            }
            PageInfo<Orders> pageInfo = new PageInfo<>(ordersList);

            return new PageResult(ordersList.size(), pageInfo.getList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据用户名从 Redis 查询支付日志
     *
     * @param userId
     */
    @Override
    public PayLog findPayLogFromRedis(String userId) {
        try {
            return (PayLog) redisTemplate.boundValueOps(
                    "payLog_" + userId).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改订单状态
     *
     * @param outTradeNo    订单交易号
     * @param transactionId 微信交易流水号
     */
    @Override
    public void updateOrderStatus(String outTradeNo, String transactionId) {
        // 修改支付日志状态
        PayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1"); // 已支付
        payLog.setTransactionId(transactionId);// 交易流水号
        payLogMapper.updateByPrimaryKeySelective(payLog);

        // 修改订单状态
        String[] orderIds = payLog.getOrderList().split(","); // 订单号列表
        // 循环订单号数组
        for (String orderId : orderIds) {
            Order order = new Order();
            order.setOrderId(Long.valueOf(orderId));
            order.setPaymentTime(new Date()); // 支付时间
            order.setStatus("2"); // 已支付
            orderMapper.updateByPrimaryKeySelective(order);
        }
        // 清除 redis 缓存数据
        redisTemplate.delete("payLog_" + payLog.getUserId());
    }

    /**
     * 修改订单状态
     *
     * @param orderId
     */
    @Override
    public void changeOrderStatus(Long orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus("2");
        orderMapper.updateByPrimaryKeySelective(order);
    }

    @Override
    public Order findOrder(Long orderId) {
        Order order = new Order();
        order.setOrderId(orderId);
        return orderMapper.selectByPrimaryKey(orderId);
    }

    /**
     * 查询所有未支付订单
     */
    /*@Override
    public List<Cart> findOrders() {
        Order order = new Order();
        order.setStatus("1");
        List<Cart> cartList = new ArrayList<>();
        List<Order> orderList = orderMapper.select(order);
        for (Order order1 : orderList) {
            Cart cart = new Cart();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order1.getOrderId());
            List<OrderItem> orderItemList = orderItemMapper.select(orderItem);
            cart.setOrderItems(orderItemList);

            cartList.add(cart);
        }
    }*/

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

    // 从订单明细集合中获取指定订单明细
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItems, Long itemId) {
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getItemId().equals(itemId)) {
                return orderItem;
            }
        }
        return null;
    }
}
