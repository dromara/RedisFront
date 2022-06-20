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

    public static DerbyUtil newInstance() {
        return new DerbyUtil();
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.derby.iapi.jdbc.InternalDriver");
        return DriverManager.getConnection("jdbc:derby:" + System.getProperty("user.home") + File.separator + "redis-front" + File.separator + "derby" + File.separator + "data;create=true");
    }

    public List<Map<String, Object>> querySql(String sql) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (var conn = getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
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
        }
        return resultList;
    }

    public boolean exec(String sql) {
        try (var conn = getConnection();
             var stmt = conn.createStatement()) {
            return stmt.execute(sql);
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage());
        }
        return false;
    }

}
