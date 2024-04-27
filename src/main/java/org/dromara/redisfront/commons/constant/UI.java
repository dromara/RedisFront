package org.dromara.redisfront.commons.constant;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UI {
    public static final List<Image> MAIN_FRAME_ICON_IMAGES = FlatSVGUtils.createWindowIconImages("/svg/redisfront.svg");
    public static final Icon REDIS_ICON = new FlatSVGIcon("svg/redisfront.svg");
    public static final Icon SUBSCRIBE_ICON = new FlatSVGIcon("svg/subscribe.svg");
    public static final Icon UNSUBSCRIBE_ICON = new FlatSVGIcon("svg/unSubscribe.svg");
    public static final Icon PUBLISH_ICON = new FlatSVGIcon("svg/publish.svg");
    public static final Icon TREE_KEY_ICON = new FlatSVGIcon("icons/tree_key.svg");
    public static final Icon MQ_ICON = new FlatSVGIcon("icons/mq.svg");
    public static final Icon CLOSE_ICON = new FlatSVGIcon("icons/close.svg");
    public static final Icon LOGS_ICON = new FlatSVGIcon("icons/RecentlyUsed.svg");
    public static final Icon PLUS_ICON = new FlatSVGIcon("icons/add.svg");
    public static final Icon LOAD_MORE_ICON = new FlatSVGIcon("svg/testBtn.svg");

    public static final Icon GITHUB_ICON = new FlatSVGIcon("icons/github.svg");
    public static final Icon MAIN_TAB_DATABASE_ICON = new FlatSVGIcon("icons/icon_db5.svg");
    public static final Icon INFO_ICON = new FlatSVGIcon("icons/info.svg");
    public static final Icon CONNECTION_ICON = new FlatSVGIcon("icons/connection.svg");
    public static final Icon TEST_CONNECTION_ICON = new FlatSVGIcon("icons/lan-connect.svg");
    public static final Icon REFRESH_ICON = new FlatSVGIcon("icons/refresh.svg");
    public static final Icon DELETE_ICON = new FlatSVGIcon("icons/delete.svg");
    public static final Icon SAVE_ICON = new FlatSVGIcon("icons/save.svg");
    public static final Icon CONTENT_TAB_DATA_ICON = new FlatSVGIcon("icons/db_key2.svg");
    public static final Icon CONTENT_TAB_COMMAND_ICON = new FlatSVGIcon("icons/db_cli2.svg");
    public static final Icon CONTENT_TAB_INFO_ICON = new FlatSVGIcon("icons/db_report2.svg");
    public static final Icon CONTENT_TAB_MEMORY_ICON = new FlatSVGIcon("icons/memory.svg");
    public static final Icon CONTENT_TAB_CPU_ICON = new FlatSVGIcon("icons/process.svg");
    public static final Icon CONTENT_TAB_KEYS_ICON = new FlatSVGIcon("icons/key.svg");
    public static final Icon CONTENT_TAB_HOST_ICON = new FlatSVGIcon("icons/host.svg");

}
