package com.pinyougou.search.listener;
import java.util.*;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.Item;
import com.pinyougou.service.GoodsService;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

// 商品消息监听器
public class ItemMessageListener implements
        SessionAwareMessageListener<ObjectMessage> {

    @Reference(timeout = 30000)
    private GoodsService goodsService;

    @Reference(timeout = 30000)
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(ObjectMessage objectMessage,
                          Session session) throws JMSException {
        System.out.println("===ItemMessageListener===");
        // 获取消息内容
        Long[] ids = (Long[]) objectMessage.getObject();
        System.out.println("ids:" + Arrays.toString(ids));
        // 查询上架的 SKU 商品数据
        List<Item> itemList = goodsService.findItemByGoodsId(ids);
        // 判断集合
        if (itemList.size() > 0) {
            // 把List<Item>转化成List<SolrItem>
            List<SolrItem> solrItems = new ArrayList<>();
            for (Item item : itemList) {
                SolrItem solrItem = new SolrItem();
                solrItem.setSpecMap(JSON.
                        parseObject(item.getSpec(), Map.class));
                solrItem.setId(item.getId());
                solrItem.setTitle(item.getTitle());
                solrItem.setPrice(item.getPrice());
                solrItem.setImage(item.getImage());
                solrItem.setGoodsId(item.getGoodsId());
                solrItem.setCategory(item.getCategory());
                solrItem.setBrand(item.getBrand());
                solrItem.setSeller(item.getSeller());
                solrItems.add(solrItem);
            }
            // 把 SKU 商品同步到索引库
            itemSearchService.saveOrUpdate(solrItems);
        }
    }
}
