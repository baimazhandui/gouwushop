package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.pojo.Goods;
import com.pinyougou.service.GoodsService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference(timeout = 10000)
    private GoodsService goodsService;

    @GetMapping("/findByPage")
    private PageResult findByPage(Goods goods, Integer page, Integer rows) {
        try {
            if (goods != null && !StringUtils.isEmpty(goods.getGoodsName())) {
                goods.setGoodsName(new String(goods.getGoodsName()
                        .getBytes("ISO8859-1"), "UTF-8"));
            }
            goods.setAuditStatus("0");
            return goodsService.findByPage(goods, page, rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/updateStatus")
    public boolean updateStatus(@RequestBody Long[] ids, String auditStatus) {
        try {
            goodsService.updateStatus(ids, auditStatus);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @GetMapping("/delete")
    public boolean delete(Long[] ids) {
        try {
            goodsService.deleteAll(ids);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
