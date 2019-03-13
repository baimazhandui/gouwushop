package com.pinyougou.service;

import java.util.Map;

// 微信支付服务接口
public interface WeixinPayService {

    /**
     * 生成微信支付二维码
     * @param outTradeNo 订单交易号
     * @param totalFee 金额(分)
     * @return Map<String,String>
     */
    Map<String,String> genPayCode(String outTradeNo, String totalFee);

    /**
     * 查询支付状态
     * @param outTradeNo
     * @return Map<String,String>
     */
    Map<String,String> queryPayStatus(String outTradeNo);

    /**
     * 关闭微信未支付订单
     * @param id
     * @return Map<String,String>
     */
    Map<String,String> closePayTimeout(String id);
}
