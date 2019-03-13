package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.Goods;
import com.pinyougou.pojo.GoodsDesc;
import com.pinyougou.pojo.Item;
import com.pinyougou.pojo.ItemCat;
import com.pinyougou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.*;

@Service(interfaceName = "com.pinyougou.service.GoodsService")
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsDescMapper goodsDescMapper;

    @Autowired
    private ItemCatMapper itemCatMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SellerMapper sellerMapper;

    @Autowired
    private ItemMapper itemMapper;

    /**
     * 添加方法
     *
     * @param goods
     */
    @Override
    public void save(Goods goods) {
        try {
            // 为商品设置未审核状态
            goods.setAuditStatus("0");
            // 添加SPU商品表
            goodsMapper.insertSelective(goods);
            // 添加商品描述表,为商品描述设置主键id
            goods.getGoodsDesc().setGoodsId(goods.getId());
            goodsDescMapper.insertSelective(goods.getGoodsDesc());

            // 迭代所有商品SKU具体商品集合,往SKU表注入数据
            if ("1".equals(goods.getIsEnableSpec())) {
                for (Item item : goods.getItems()) {
                    // 定义SKU商品的标题
                    StringBuilder title = new StringBuilder();
                    title.append(goods.getGoodsName());
                    // 把规格选项JSON字符串转化成Map集合
                    Map<String, Object> spec = JSON.parseObject(item.getSpec());
                    for (Object value : spec.values()) {
                        title.append(" " + value);
                    }
                    // 设置SKU商品的标题
                    item.setTitle(title.toString());
                    /** 设置SKU商品其它属性 */
                    setItemInfo(item, goods);

                    itemMapper.insertSelective(item);
                }
            } else {
                /** 创建SKU具体商品对象 */
                Item item = new Item();
                /** 设置SKU商品的标题 */
                item.setTitle(goods.getGoodsName());
                /** 设置SKU商品的价格 */
                item.setPrice(goods.getPrice());
                /** 设置SKU商品库存数据 */
                item.setNum(9999);
                /** 设置SKU商品启用状态 */
                item.setStatus("1");
                /** 设置是否默认*/
                item.setIsDefault("1");
                /** 设置规格选项 */
                item.setSpec("{}");
                /** 设置SKU商品其它属性 */
                setItemInfo(item, goods);
                itemMapper.insertSelective(item);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setItemInfo(Item item, Goods goods) {
        // 是指SKU商品图片地址
        List<Map> imageList = JSON.parseArray(
                goods.getGoodsDesc().getItemImages(), Map.class);
        for (Map map : imageList) {
            item.setImage(map.get("url").toString());
        }
        // 设置SKU商品分类id
        item.setCategoryid(goods.getCategory3Id());
        // 设置SKU商品创建时间
        item.setCreateTime(new Date());
        // 设置SKU商品修改时间
        item.setUpdateTime(item.getCreateTime());
        // 设置SKU商品SPU编号
        item.setGoodsId(goods.getId());
        // 设置商家编号
        item.setSellerId(goods.getSellerId());
        // 设置商品分类名称
        item.setCategory(itemCatMapper
                .selectByPrimaryKey(item.getCategoryid()).getName());
        // 设置品牌名称
        item.setBrand(brandMapper
                .selectByPrimaryKey(goods.getBrandId()).getName());
        // 设置商家店铺名称
        item.setSeller(sellerMapper
                .selectByPrimaryKey(goods.getSellerId()).getNickName());

    }

    /**
     * 修改方法
     *
     * @param goods
     */
    @Override
    public void update(Goods goods) {

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
        goodsMapper.deleteAll(ids, "1");
    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public Goods findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<Goods> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *  @param goods
     * @param page
     * @param rows
     */
    @Override
    public PageResult findByPage(Goods goods, int page, int rows) {
        try {
            PageInfo<Map<String, Object>> pageInfo = PageHelper.startPage(page, rows)
                    .doSelectPageInfo(new ISelect() {
                @Override
                public void doSelect() {
                    goodsMapper.findAll(goods);
                }
            });

            for (Map<String, Object> map : pageInfo.getList()) {
                ItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(map.get("category1Id"));
                map.put("category1", itemCat1 != null ? itemCat1.getName() : "");

                ItemCat itemCat2 = itemCatMapper.selectByPrimaryKey(map.get("category2Id"));
                map.put("category2", itemCat2 != null ? itemCat2.getName() : "");

                ItemCat itemCat3 = itemCatMapper.selectByPrimaryKey(map.get("category3Id"));
                map.put("category3", itemCat3 != null ? itemCat3.getName() : "");
            }
            return new PageResult(pageInfo.getTotal(), pageInfo.getList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据auditStatus更新id数组对应id的SPU商品的状态码
     *
     * @param ids
     * @param auditStatus
     */
    @Override
    public void updateStatus(Long[] ids, String auditStatus) {
        /*try {
            for (Long id : ids) {
                Goods goods = goodsMapper.selectByPrimaryKey(id);
                goods.setAuditStatus(auditStatus);
                goodsMapper.updateByPrimaryKeySelective(goods);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }*/
        try {
            goodsMapper.updateStatus(ids, auditStatus);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据isMarketable更新ids数组对应的id的SPU商品的是否上架属性
     *
     * @param ids
     * @param isMarketable
     */
    @Override
    public void updateMarketable(Long[] ids, String isMarketable) {
        try {
            goodsMapper.updateMarketable(ids,isMarketable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> getGoods(Long goodsId) {
        try {
            // 定义数据模型
            Map<String, Object> dataModel = new HashMap<>();
            /** 加载商品SPU数据 */
            Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods", goods);
            /** 加载商品描述数据 */
            GoodsDesc goodsDesc =goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc", goodsDesc);
            /** 商品分类 */
            if (goods != null && goods.getCategory3Id() != null) {
                String itemCat1 = itemCatMapper
                        .selectByPrimaryKey(goods.getCategory1Id()).getName();
                String itemCat2 = itemCatMapper
                        .selectByPrimaryKey(goods.getCategory2Id()).getName();
                String itemCat3 = itemCatMapper
                        .selectByPrimaryKey(goods.getCategory3Id()).getName();
                dataModel.put("itemCat1", itemCat1);
                dataModel.put("itemCat2", itemCat2);
                dataModel.put("itemCat3", itemCat3);
            }
            // 查询SKU商品
            Example example = new Example(Item.class);
            Example.Criteria criteria = example.createCriteria();
            // 设置条件为查询状态码为1
            criteria.andEqualTo("status", "1");
            // 设置goodsId
            criteria.andEqualTo("goodsId", goodsId);
            // 按是否默认倒序排序
            example.orderBy("isDefault").desc();
            // 执行查询
            List<Item> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", JSON.toJSONString(itemList));
            //dataModel.put("itemList", itemList);

            return dataModel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 查询上架的 SKU 商品数据
    @Override
    public List<Item> findItemByGoodsId(Long[] ids) {
        try {
            // 创建示范对象
            Example example = new Example(Item.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andIn("goodsId",Arrays.asList(ids));
            List<Item> itemList = itemMapper.selectByExample(example);
            return itemList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
