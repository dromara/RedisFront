package com.redisfront.commons.util;


import cn.hutool.core.io.FileUtil;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.exception.RedisFrontException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DerbyUtils {
    private static final Logger log = LoggerFactory.getLogger(DerbyUtils.class);
    private static Connection conn;

    private DerbyUtils() {

    }

    public static DerbyUtils getInstance() {
        return new DerbyUtils();
    }

    public static void init() {
        try {

            var createdFile = new File(Const.DERBY_LOG_FILE_PATH);
            if (FileUtil.isEmpty(createdFile)) {
                var dirCreated = createdFile.mkdir();
                log.info("create Derby Log dir created: {}", dirCreated);
                var fileCreated = new File(Const.DERBY_LOG_FILE).createNewFile();
                log.info("create Derby Log File created: {}", fileCreated);
            }
            System.setProperty("derby.stream.error.file", Const.DERBY_LOG_FILE);
            Class.forName("org.apache.derby.iapi.jdbc.InternalDriver");
            conn = DriverManager.getConnection("jdbc:derby:" + Const.DERBY_DATA_PATH + "create=true");
        } catch (Exception e) {
            throw new RedisFrontException(e, true);
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
