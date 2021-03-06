package com.wfj.search.online.web.query;

import com.wfj.search.online.web.common.pojo.SearchParams;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.GroupParams;
import org.springframework.stereotype.Component;

/**
 * <p>create at 15-11-5</p>
 *
 * @author liufl
 * @since 1.0.0
 */
@Component("justRootCategoryFilterQueryDecorator")
public class JustRootCategoryFilterQueryDecorator implements QueryDecorator {

    @Override
    public void decorator(SolrQuery query, SearchParams searchParams) {
        String channel = searchParams.getChannel();
        query.setQuery("*:*").addFilterQuery(
                "allLevelCategoryIds_" + channel + ":" + searchParams.getSelectedCategories().get(0).getId());
        // group参数
        query
                .set(GroupParams.GROUP, true)
                .set(GroupParams.GROUP_FIELD, "spuId")
                .set(GroupParams.GROUP_TOTAL_COUNT, true)
                .set(GroupParams.GROUP_FACET, true)
                .set(GroupParams.GROUP_TRUNCATE, true);
        query.addFilterQuery("allLevelCategoryIds_" + channel + ":*");
    }
}
