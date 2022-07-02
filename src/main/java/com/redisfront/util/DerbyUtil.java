package com.redisfront.util;


import com.redisfront.constant.Const;
import com.redisfront.exception.RedisFrontException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DerbyUtil {
    private static final Logger log = LoggerFactory.getLogger(DerbyUtil.class);
    private static Connection conn;

    public static DerbyUtil getInstance() {
        return new DerbyUtil();
    }

    public static void init() {
        try {
            System.setProperty("derby.stream.error.file", Const.DERBY_LOG_FILE_PATH);
            Class.forName("org.apache.derby.iapi.jdbc.InternalDriver");
            conn = DriverManager.getConnection("jdbc:derby:" + Const.DERBY_DATA_PATH + "create=true");
        } catch (ClassNotFoundException | SQLException e) {
            log.error(e.getMessage());
            throw new RedisFrontException(e,true);
        }
    }

    public List<Map<String, Object>> querySql(String sql) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (var ps = conn.prepareStatement(sql);
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
        try (var stmt = conn.createStatement()) {
            return stmt.execute(sql);
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

}
