package com.redisfront.model;

import cn.hutool.json.JSONUtil;
import io.lettuce.core.StreamMessage;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Redis Connection TableModel
 */
public class StreamTableModel extends DefaultTableModel {

    private final Class<?>[] columnTypes = new Class<?>[]{
            Integer.class, String.class, String.class
    };
    private final boolean[] columnEditable = new boolean[]{
            false, false, false
    };

    public StreamTableModel(List<StreamMessage<String, String>> dataList) {
        var dataVector = new Object[dataList.size()][4];
        for (var i = 0; i < dataList.size(); i++) {
            dataVector[i][0] = i + 1;
            dataVector[i][1] = dataList.get(i).getId();
            dataVector[i][2] = JSONUtil.toJsonStr(dataList.get(i).getBody()) ;
        }
        this.setDataVector(dataVector, new String[]{"#", "ID","Body"});
    }


    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnEditable[columnIndex];
    }

}
