package com.wfj.search.online.index.coordinating;

import com.wfj.search.online.index.operation.IOperation;
import com.wfj.search.util.record.pojo.Operation;

import java.text.SimpleDateFormat;

/**
 * <p>create at 16-1-5</p>
 *
 * @author liufl
 * @since 1.0.0
 */
public class ActivitiesUpdateTaskDescription extends AbstractCoordinatingTaskDescription {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHH");

    public ActivitiesUpdateTaskDescription(Operation param,
            IOperation<Void> activeUpdateOperation) {
        super("activeUpdate", param, activeUpdateOperation);
    }

    @Override
    public String getParamPath() {
        return DATE_FORMAT.format(this.getParam().getStartTime()) + "";
    }
}
