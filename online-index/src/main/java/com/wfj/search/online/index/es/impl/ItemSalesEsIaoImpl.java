package com.wfj.search.online.index.es.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfj.search.online.index.es.ItemSalesEsIao;
import com.wfj.search.online.index.iao.IndexException;
import com.wfj.search.online.index.pojo.ItemSalesPojo;
import org.elasticsearch.action.ActionWriteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <p>create at 16-3-28</p>
 *
 * @author liufl
 * @since 1.0.19
 */
@Component("itemSalesEsIao")
public class ItemSalesEsIaoImpl implements ItemSalesEsIao {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String TYPE = "item-sales";
    @Autowired
    private Client esClient;
    @Value("${es.index}")
    private String index;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void upsert(ItemSalesPojo itemSalesPojo) throws JsonProcessingException, IndexException {
        String source = this.objectMapper.writeValueAsString(itemSalesPojo);
        UpdateResponse resp = this.esClient.prepareUpdate(this.index, TYPE, itemSalesPojo.getItemId()).setDoc(source)
                .setUpsert(source).get();
        ActionWriteResponse.ShardInfo shardInfo = resp.getShardInfo();
        if (shardInfo.getFailed() == shardInfo.getTotal() && shardInfo.getTotal() > 0) {
            logger.error("写入商品[{}]失败，所有节点都写入失败！", itemSalesPojo.getItemId());
            throw new IndexException("所有节点都写入失败！");
        }
    }

    @Override
    public ItemSalesPojo get(String itemId) {
        try {
            GetResponse resp = this.esClient.prepareGet(this.index, TYPE, itemId).get();
            String source = resp.getSourceAsString();
            if (source != null) {
                return this.objectMapper.readValue(source, ItemSalesPojo.class);
            } else {
                logger.warn("查找商品[{}]销量失败", itemId);
            }
        } catch (Exception e) {
            logger.warn("查找商品[{}]销量失败", itemId, e);
        }
        return null;
    }
}