package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.Content;
import com.pinyougou.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.ContentService")
@Transactional
public class ContentServiceImpl implements ContentService{

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加方法
     *
     * @param content
     */
    @Override
    public void save(Content content) {
        // 保存广告后清除缓存
        try {
            contentMapper.insertSelective(content);
            // 清除redis缓存
            redisTemplate.delete("content");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改方法
     *
     * @param content
     */
    @Override
    public void update(Content content) {
        // 修改广告后清除缓存
        try {
            contentMapper.updateByPrimaryKeySelective(content);
            // 清除缓存
            redisTemplate.delete("content");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        Example example = new Example(Content.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));

        contentMapper.deleteByExample(example);
    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public Content findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<Content> findAll() {
        return null;
    }

    /**
     * 多条件分页查询
     *
     * @param content
     * @param page
     * @param rows
     */
    @Override
    public PageResult findByPage(Content content, int page, int rows) {
        PageInfo<Content> pageInfo = PageHelper.startPage(page, rows).doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                contentMapper.selectAll();
            }
        });
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 根据分类id查询广告列表
     *
     * @param categoryId
     */
    @Override
    public List<Content> findContentByCategoryId(Long categoryId) {
        /** 定义广告数据 */
        List<Content> contentList = null;
        try {
            /** 从Redis中获取广告 */
            contentList = (List<Content>) redisTemplate
                    .boundValueOps("content").get();
            if (contentList != null && contentList.size() > 0) {
                return contentList;
            }
        }catch (Exception ex){ }
        try {
            Example example = new Example(Content.class);
            Example.Criteria criteria = example.createCriteria();
            // 添加categoryId = 条件
            criteria.andEqualTo("categoryId", categoryId);
            // 添加status = 1 条件
            criteria.andEqualTo("status", "1");
            // 根据sortOrder排序
            example.orderBy("sortOrder").asc();
            // 查询广告数据
            contentList = contentMapper.selectByExample(example);
            try {
                // 存入redis缓存
                redisTemplate.boundValueOps("content").set(contentList);
            } catch (Exception e) { }
            return contentList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
