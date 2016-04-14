package com.wfj.search.online.index.es.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wfj.search.online.index.es.PropertyValueEsIao;
import com.wfj.search.online.index.iao.IndexException;
import com.wfj.search.online.index.pojo.PropertyValueIndexPojo;
import org.elasticsearch.action.ActionWriteResponse;
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
@Component("propertyValueEsIao")
public class PropertyValueEsIaoImpl implements PropertyValueEsIao {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String TYPE = "property-value";
    @Autowired
    private Client esClient;
    @Value("${es.index}")
    private String index;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void upsert(PropertyValueIndexPojo propertyValueIndexPojo) throws JsonProcessingException, IndexException {
        String source = this.objectMapper.writeValueAsString(propertyValueIndexPojo);
        UpdateResponse resp = this.esClient.prepareUpdate(this.index, TYPE, propertyValueIndexPojo.getPropertyValueId())
                .setDoc(source).setUpsert(source).get();
        ActionWriteResponse.ShardInfo shardInfo = resp.getShardInfo();
        if (shardInfo.getFailed() == shardInfo.getTotal() && shardInfo.getTotal() > 0) {
            logger.error("写入属性值[{}]失败，所有节点都写入失败！", propertyValueIndexPojo.getPropertyValueId());
            throw new IndexException("所有节点都写入失败！");
        }
    }
}
