package cn.devcms.redisfront.model;

import cn.devcms.redisfront.common.enums.NodeTypeEnum;
import lombok.Data;

/**
 * NodeInfo
 *
 * @author Jin
 */
@Data
public class NodeInfo {
    private String title;
    private String icon;
    private NodeTypeEnum nodeTypeEnum;
    private String key;
}
