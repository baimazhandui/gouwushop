package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.service.ItemSearchService;
import com.pinyougou.solr.SolrItem;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service(interfaceName = "com.pinyougou.service.ItemSearchService")
@Transactional
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    // 搜索方法
    @Override
    public Map<String, Object> search(Map<String, Object> params) {
        // 创建Map 集合封装返回数据
        Map<String, Object> data = new HashMap<>();
        // 获取检索关键字
        String keywords = (String) params.get("keywords");

        // 获取当前页码
        Integer page = (Integer) params.get("page");
        if (page == null) {
            // 默认第一页
            page = 1;
        }
        // 获取每页显示的记录数
        Integer rows = (Integer) params.get("rows");
        if (rows == null) {
            // 默认20条记录
            rows = 20;
        }

        // 获取排序数据
        String sortField = (String) params.get("sortField");
        String sortValue = (String) params.get("sort");

        // 判断检索关键字是否为空
        if (!StringUtils.isEmpty(keywords)) { // 高亮查询

            // 创建高亮查询对象
            HighlightQuery highlightQuery = new SimpleHighlightQuery();
            // 创建高亮选项对象
            HighlightOptions highlightOptions = new HighlightOptions();
            // 设置高亮域
            highlightOptions.addField("title");
            // 设置高亮前缀
            highlightOptions.setSimplePrefix("<font color='red'>");
            // 设置高亮后缀
            highlightOptions.setSimplePostfix("</font>");
            // 设置高亮选项
            highlightQuery.setHighlightOptions(highlightOptions);
            // 创建查询条件
            Criteria criteria = new Criteria("keywords").is(keywords);
            // 添加查询条件
            highlightQuery.addCriteria(criteria);

            // 执行过滤
            List<Criteria> highlightQueryCriteriaList = dofilt(params);

            for (Criteria cri : highlightQueryCriteriaList) {
                highlightQuery.addCriteria(cri);
            }

            // 添加排序

            if (!StringUtils.isEmpty(sortField)
                    && !StringUtils.isEmpty(sortValue)) {
                Sort sort = new Sort("ASC".equalsIgnoreCase(sortValue) ?
                        Sort.Direction.ASC : Sort.Direction.DESC, sortField);
                highlightQuery.addSort(sort);
            }

            // 设置起始记录查询数
            highlightQuery.setOffset((page - 1) * rows);
            // 设置每页记录数
            highlightQuery.setRows(rows);

            // 分页查询, 得到高亮分页查询对象
            HighlightPage<SolrItem> highlightPage = solrTemplate
                    .queryForHighlightPage(highlightQuery, SolrItem.class);
            // 循环高亮项集合
            for (HighlightEntry<SolrItem> he :
                    highlightPage.getHighlighted()) {
                // 获取检索到的原实体
                SolrItem solrItem = he.getEntity();
                // 判断高亮集合及集合中第一个Field 的高亮内容
                if (he.getHighlights().size() > 0
                        && he.getHighlights().get(0)
                        .getSnipplets().size() > 0) {
                    // 设置高亮的结果
                    solrItem.setTitle(he.getHighlights()
                            .get(0).getSnipplets().get(0));
                }
            }
            data.put("rows", highlightPage.getContent());

            // 设置总页数
            data.put("totalPages", highlightPage.getTotalPages());
            // 设置总记录数
            data.put("total", highlightPage.getTotalElements());
        } else { // 简单查询

            SimpleQuery simpleQuery = new SimpleQuery("*:*");

            // 执行过滤
            List<Criteria> simpleQueryCriteriaList = dofilt(params);

            // 遍历查询条件,并添加到查询中
            for (Criteria cri : simpleQueryCriteriaList) {
                simpleQuery.addCriteria(cri);
            }

            // 添加排序
            if (!StringUtils.isEmpty(sortField)
                    && !StringUtils.isEmpty(sortValue)) {
                Sort sort = new Sort("ASC".equalsIgnoreCase(sortValue) ?
                        Sort.Direction.ASC : Sort.Direction.DESC, sortField);
                simpleQuery.addSort(sort);
            }

            // 设置起始记录查询数
            simpleQuery.setOffset((page - 1) * rows);
            // 设置每页显示记录数
            simpleQuery.setRows(rows);

            ScoredPage scoredPage = solrTemplate
                    .queryForPage(simpleQuery, SolrItem.class);
            data.put("rows", scoredPage.getContent());

            // 设置总页数
            data.put("totalPages", scoredPage.getTotalPages());
            // 设置总记录数
            data.put("total", scoredPage.getTotalElements());
        }
        return data;
    }

    /** 查询上架的 SKU 商品数据 */
    @Override
    public void saveOrUpdate(List<SolrItem> solrItems) {
        UpdateResponse updateResponse =
                solrTemplate.saveBeans(solrItems);
        if (updateResponse.getStatus() == 0) {
            solrTemplate.commit();
        } else {
            solrTemplate.rollback();
        }
    }

    // 删除商品索引
    @Override
    public void delete(List<Long> goodsId) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("goodsId").in(goodsId);
        query.addCriteria(criteria);

        UpdateResponse updateResponse = solrTemplate.delete(query);
        if (updateResponse.getStatus() == 0) {
            solrTemplate.commit();
        } else {
            solrTemplate.rollback();
        }
    }

    private List<Criteria> dofilt(Map<String, Object> params) {
        // 创建集合存放查询条件
        List<Criteria> criteriaList = new ArrayList<>();
        // 商品分类过滤
        if (!"".equals(params.get("category"))) {
            Criteria criteria1 = new Criteria("category")
                    .is(params.get("category"));
            criteriaList.add(criteria1);
        }
        // 品牌过滤
        if (!"".equals(params.get("brand"))) {
            Criteria criteria2 = new Criteria("brand")
                    .is(params.get("brand"));
            criteriaList.add(criteria2);
        }
        // 规格过滤
        Map<String, String> spec =
                (Map<String, String>) params.get("spec");
        if (spec.values().size() > 0) {
            Set<String> strings = spec.keySet();
            for (String attr : strings) {
                String fieldName = "spec_" + attr;
                Criteria criteria3 = new Criteria(fieldName)
                        .is(spec.get(attr));
                criteriaList.add(criteria3);
                ;
            }
        }
        // 价格过滤
        if (!"".equals(params.get("price"))) {
            String priceStr = (String) params.get("price");
            String[] priceArr = priceStr.split("-");
            if (!"*".equals(priceArr[1])) {
                Criteria criteria4 = new Criteria("price")
                        .between(priceArr[0], priceArr[1],
                                true, true);
                criteriaList.add(criteria4);
            } else {
                Criteria criteria4 = new Criteria("price")
                        .greaterThanEqual(priceArr[0]);
                criteriaList.add(criteria4);
            }
        }
        return criteriaList;
    }
}
