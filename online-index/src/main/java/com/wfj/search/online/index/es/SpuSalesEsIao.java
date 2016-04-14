package com.wfj.search.online.index.es;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wfj.search.online.index.iao.IndexException;
import com.wfj.search.online.index.pojo.SpuSalesPojo;

/**
 * <p>create at 16-3-28</p>
 *
 * @author liufl
 * @since 1.0.19
 */
public interface SpuSalesEsIao {
    void upsert(SpuSalesPojo spuSalesPojo) throws JsonProcessingException, IndexException;

    SpuSalesPojo get(String spuId);
}
