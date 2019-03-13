package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Goods;
import com.pinyougou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;


@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 10000)
    private GoodsService goodsService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Destination solrQueue;

      @Autowired
    private Destination pageTopic;

    @Autowired
    private Destination pageDeleteTopic;

    @PostMapping("/save")
    public boolean save(@RequestBody Goods goods) {
        try {
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.setSellerId(sellerId);
            goodsService.save(goods);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/findByPage")
    public PageResult findByPage(Goods goods, Integer page, Integer rows) {
        String sellId = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(sellId);
        try {
            if (sellId != null && !StringUtils.isEmpty(goods.getGoodsName())) {
                goods.setGoodsName(new String(goods.getGoodsName()
                        .getBytes("ISO8859-1"), "UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return goodsService.findByPage(goods, page, rows);
    }

    @PostMapping("/updateMarketable")
    public boolean updateMarketable(@RequestBody Long[] ids, String isMarketable) {
        try {
            goodsService.updateMarketable(ids,isMarketable);
            // 判断商品上下架状态
            if ("1".equals(isMarketable)) { // 表示商品上架
                // 发送消息, 生成商品索引
                jmsTemplate.send(solrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session)
                            throws JMSException {
                        return session.createObjectMessage(ids);
                    }
                });
                for (Long goodsId : ids) {
                    jmsTemplate.send(pageTopic, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session)
                                throws JMSException {
                            return session.createTextMessage(goodsId.toString());
                        }
                    });
                }
            } else { // 表示商品下架
                jmsTemplate.send(solrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session)
                            throws JMSException {
                        return session.createObjectMessage(ids);
                    }
                });

                // 发送消息,删除静态网页
                jmsTemplate.send(pageDeleteTopic, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session)
                            throws JMSException {
                        return session.createObjectMessage(ids);
                    }
                });
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
