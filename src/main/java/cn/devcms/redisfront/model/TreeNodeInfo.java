package cn.devcms.redisfront.model;

import cn.devcms.redisfront.common.enums.NodeTypeEnum;

/**
 * NodeInfo
 *
 * @author Jin
 */

public record TreeNodeInfo(String title, String icon, NodeTypeEnum nodeTypeEnum, String key) {
}
