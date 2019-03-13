package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.pojo.PageResult;
import com.pinyougou.mapper.ContentCategoryMapper;
import com.pinyougou.pojo.ContentCategory;
import com.pinyougou.service.ContentCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service(interfaceName = "com.pinyougou.service.ContentCategoryService")
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private ContentCategoryMapper contentCategoryMapper;

    /**
     * 添加方法
     *
     * @param contentCategory
     */
    @Override
    public void save(ContentCategory contentCategory) {
        contentCategoryMapper.insertSelective(contentCategory);
    }

    /**
     * 修改方法
     *
     * @param contentCategory
     */
    @Override
    public void update(ContentCategory contentCategory) {
        contentCategoryMapper.updateByPrimaryKeySelective(contentCategory);
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
        Example example = new Example(ContentCategory.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));

        contentCategoryMapper.deleteByExample(example);
    }

    /**
     * 根据主键id查询
     *
     * @param id
     */
    @Override
    public ContentCategory findOne(Serializable id) {
        return null;
    }

    /**
     * 查询全部
     */
    @Override
    public List<ContentCategory> findAll() {
        return contentCategoryMapper.selectAll();
    }

    /**
     * 多条件分页查询
     *
     * @param contentCategory
     * @param page
     * @param rows
     */
    @Override
    public PageResult findByPage(ContentCategory contentCategory, int page, int rows) {
        PageInfo<ContentCategory> pageInfo = PageHelper.startPage(page, rows)
                .doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                contentCategoryMapper.selectAll();
            }
        });
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }
}
