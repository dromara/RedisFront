package cn.devcms.redisfront.common.util;

import cn.devcms.redisfront.model.ConnectInfo;

import java.util.LinkedHashMap;

/**
 * ContextUtil
 *
 * @author Jin
 */
public class ContextUtil {

    private final static LinkedHashMap<Object, ConnectInfo> connected = new LinkedHashMap<>();
    private static Integer openedServerTableId = 0;
    private static String selectedNode = "";


    public static void putConnectInfo(Object key, ConnectInfo connectInfo) {
        connected.put(key, connectInfo);
    }

    public static void removeConnectInfo(Object key) {
        connected.remove(key);
    }

    public static ConnectInfo getConnectInfo(Object openedServer) {
        return connected.get(openedServer);
    }

    public static Integer getOpenedServerTableId() {
        return openedServerTableId;
    }

    public static void setOpenedServerTableId(Integer openedServerTableId) {
        ContextUtil.openedServerTableId = openedServerTableId;
    }

    public static void setSelectedNode(String selectedNode) {
        ContextUtil.selectedNode = selectedNode;
    }

    public static String getSelectedNode() {
        return selectedNode;
    }


}
