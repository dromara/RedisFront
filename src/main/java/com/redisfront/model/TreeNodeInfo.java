package com.redisfront.model;

import com.redisfront.common.enums.NodeTypeEnum;

/**
 * NodeInfo
 *
 * @author Jin
 */

public class TreeNodeInfo {

    private String title;
    private String icon;
    private NodeTypeEnum nodeTypeEnum;
    private String key;

    public TreeNodeInfo(String title, String icon, NodeTypeEnum nodeTypeEnum, String key) {
        this.title = title;
        this.icon = icon;
        this.nodeTypeEnum = nodeTypeEnum;
        this.key = key;
    }

    public String title() {
        return title;
    }

    public TreeNodeInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public String icon() {
        return icon;
    }

    public TreeNodeInfo setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public NodeTypeEnum nodeTypeEnum() {
        return nodeTypeEnum;
    }

    public TreeNodeInfo setNodeTypeEnum(NodeTypeEnum nodeTypeEnum) {
        this.nodeTypeEnum = nodeTypeEnum;
        return this;
    }

    public String key() {
        return key;
    }

    public TreeNodeInfo setKey(String key) {
        this.key = key;
        return this;
    }
}
