package com.redisfront.common.util;


import cn.hutool.core.io.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DerbyUtil {
    private static final Logger log = LoggerFactory.getLogger(DerbyUtil.class.getName());

    private static final String userDir = System.getProperty("user.home");

    static {
        System.setProperty("derby.stream.error.file", userDir + File.separator + "redis-front" + File.separator + "/derby.log");
        System.out.println("Derby 日志配置成功 - " + userDir);
    }

    public static DerbyUtil newInstance() {
        return new DerbyUtil();
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.derby.iapi.jdbc.InternalDriver");
        return DriverManager.getConnection("jdbc:derby:" + userDir + File.separator + "redis-front" + File.separator + "data;create=true");
    }

    public List<Map<String, Object>> querySql(String sql) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> resultList = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs != null) {
                var md = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> rowData = new HashMap<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        rowData.put(md.getColumnName(i).toLowerCase(), rs.getObject(i));
                    }
                    resultList.add(rowData);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            IoUtil.close(rs);
            IoUtil.close(ps);
            IoUtil.close(conn);
        }
        return resultList;
    }

    public boolean exec(String sql) {
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            return stmt.execute(sql);
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage());
        } finally {
            IoUtil.close(stmt);
            IoUtil.close(conn);
        }
        return false;
    }

}
