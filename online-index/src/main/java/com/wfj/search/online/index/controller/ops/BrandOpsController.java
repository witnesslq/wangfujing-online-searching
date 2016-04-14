package com.wfj.search.online.index.controller.ops;

import com.alibaba.fastjson.JSONObject;
import com.wfj.platform.util.zookeeper.discovery.ServiceRegister;
import com.wfj.search.online.index.iao.IndexException;
import com.wfj.search.online.index.pojo.failure.Failure;
import com.wfj.search.online.index.service.IEsService;
import com.wfj.search.online.index.service.IIndexService;
import com.wfj.search.online.index.util.MessageBodyChooser;
import com.wfj.search.util.record.pojo.Operation;
import com.wfj.search.util.record.util.OperationHolderKt;
import com.wfj.search.util.web.record.WebOperation;
import com.wfj.search.utils.web.signature.verify.JsonSignVerify;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * <p>create at 15-11-3</p>
 *
 * @author liufl
 * @author liuxh
 * @since 1.0.0
 */
@Controller
@RequestMapping("/ops/brand")
public class BrandOpsController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private IEsService esService;
    @Autowired
    private IIndexService indexService;

    @RequestMapping("/indexItems")
    @ServiceRegister(value = "online-ops-indexBrand")
    @WebOperation
    @JsonSignVerify
    public JSONObject indexItems(@RequestBody(required = false) String message,
            @RequestParam(value = "message", required = false) String messageGet) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = ((ServletRequestAttributes) requestAttributes)
                .getResponse();
        logger.debug("post message body:" + message);
        logger.debug("param message=" + messageGet);
        JSONObject result = new JSONObject();
        Operation operation = OperationHolderKt.getOperation();
        result.put("asynchronous", false);
        String brandId;
        try {
            JSONObject messageBody = MessageBodyChooser.getJsonObject(message, messageGet);
            brandId = Validate.notBlank(messageBody.getString("brandId"), "品牌编码为空").trim();
        } catch (Exception e) {
            logger.error("请求消息格式非法", e);
            result.put("success", false);
            result.put("message", "请求消息格式非法,Exception:" + e.toString());
            response.setStatus(500);
            return result;
        }
        try {
            result.put("success", true);
            Optional<Failure> failureOptional = this.esService.rebuildBrandAndItems(brandId, Long.parseLong(operation.getSid()));
            failureOptional.ifPresent(failure -> {
                result.put("success", false);
                result.put("message", failure.getMessage());
                response.setStatus(500);
            });
            if (!failureOptional.isPresent()) {
                Optional<Failure> indexFailure = this.indexService
                        .indexItemsOfBrandFromEs(brandId, Long.parseLong(operation.getSid()));
                indexFailure.ifPresent(failure -> {
                    result.put("success", false);
                    result.put("message", failure.getMessage());
                    response.setStatus(500);
                });
                if (!indexFailure.isPresent()) {
                    try {
                        this.indexService.commit();
                    } catch (IndexException e) {
                        logger.warn("commit solr索引失败", e);
                    }
                }
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.toString());
            response.setStatus(500);
        }
        return result;
    }

    /**
     * 只在加品牌黑名单时调用
     */
    @RequestMapping("/removeItems")
    @ServiceRegister(value = "online-ops-removeBrand")
    @WebOperation
    @JsonSignVerify
    public JSONObject removeItems(@RequestBody(required = false) String message,
            @RequestParam(value = "message", required = false) String messageGet) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = ((ServletRequestAttributes) requestAttributes)
                .getResponse();
        logger.debug("post message body:" + message);
        logger.debug("param message=" + messageGet);
        JSONObject result = new JSONObject();
        result.put("asynchronous", false);
        String brandId;
        try {
            JSONObject messageBody = MessageBodyChooser.getJsonObject(message, messageGet);
            brandId = Validate.notBlank(messageBody.getString("brandId"), "品牌编码为空").trim();
        } catch (Exception e) {
            logger.error("请求消息格式非法", e);
            result.put("success", false);
            result.put("message", "请求消息格式非法,Exception:" + e.toString());
            response.setStatus(500);
            return result;
        }
        Exception exception = null;
        String msg = "";
        try {
            this.indexService.removeItemsOfBrand(brandId);
            try {
                this.indexService.commit();
            } catch (IndexException e) {
                logger.warn("commit solr索引失败", e);
            }
        } catch (Exception e) {
            exception = e;
            msg = "删除索引失败";
        }
        if (exception != null) {
            logger.error(msg, exception);
            result.put("success", false);
            result.put("message", msg + ",Exception:" + exception.toString());
            response.setStatus(500);
        } else {
            result.put("success", true);
        }
        return result;
    }
}
