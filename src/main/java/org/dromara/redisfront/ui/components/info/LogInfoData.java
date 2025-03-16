package org.dromara.redisfront.ui.components.info;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class LogInfoData {
    String key;
    Map<String, Object> infoData;
}