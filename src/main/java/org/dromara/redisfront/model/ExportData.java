package org.dromara.redisfront.model;

import lombok.Data;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;
import org.dromara.redisfront.model.entity.ConnectGroupEntity;

import java.util.List;

@Data
public class ExportData {
    private List<ConnectGroupEntity> groups;
    private List<ConnectDetailEntity> details;
}
