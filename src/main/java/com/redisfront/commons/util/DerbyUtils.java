package com.redisfront.commons.util;


import cn.hutool.core.io.FileUtil;
import com.redisfront.commons.constant.Const;
import com.redisfront.commons.exception.RedisFrontException;
import com.redisfront.commons.func.Fn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.*;

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
            var isCurrentDir = FileUtil.exist(Const.CURRENT_DIR_DERBY_LOG_FILE_PATH);
            var derbyFolder = new File(isCurrentDir ? Const.CURRENT_DIR_DERBY_LOG_FILE_PATH : Const.DERBY_LOG_FILE_PATH);

            if (FileUtil.isEmpty(derbyFolder) || !FileUtil.exist(derbyFolder)) {
                var dirCreated = FileUtil.mkdir(derbyFolder);
                log.info("create Derby Log dir created: {}", dirCreated);
                var fileCreated = FileUtil.newFile(isCurrentDir ? Const.CURRENT_DIR_DERBY_LOG_FILE : Const.DERBY_LOG_FILE);
                log.info("create Derby Log File created: {}", fileCreated);
            }

            System.setProperty("derby.stream.error.file", (isCurrentDir ? Const.CURRENT_DIR_DERBY_LOG_FILE : Const.DERBY_LOG_FILE));

            if (Arrays.stream(Objects.requireNonNull(derbyFolder.listFiles())).noneMatch(file -> Fn.equal(file.getName().toLowerCase(), "data"))) {
                PrefUtils.getState().put(Const.KEY_APP_DATABASE_INIT, Boolean.TRUE.toString());
            }

            Class.forName("org.apache.derby.iapi.jdbc.InternalDriver");
            conn = DriverManager.getConnection("jdbc:derby:" + (isCurrentDir ? Const.CURRENT_DIR_DERBY_DATA_PATH : Const.DERBY_DATA_PATH) + "create=true");
        } catch (Exception e) {
            log.error("Derby init failed - {}", e.getMessage());
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
