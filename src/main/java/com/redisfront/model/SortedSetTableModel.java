package com.redisfront.model;

import cn.hutool.core.io.unit.DataSizeUtil;
import io.lettuce.core.ScoredValue;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Redis Connection TableModel
 */
public class SortedSetTableModel extends DefaultTableModel {

    private final Class<?>[] columnTypes = new Class<?>[]{
            Integer.class, Double.class, String.class, Integer.class, String.class
    };
    private final boolean[] columnEditable = new boolean[]{
            false, false, false, false
    };

    public SortedSetTableModel(List<ScoredValue<String>> dataList, String... columNames) {
        var dataVector = new Object[dataList.size()][5];
        for (var i = 0; i < dataList.size(); i++) {
            dataVector[i][0] = i;
            dataVector[i][1] = dataList.get(i).getScore();
            dataVector[i][2] = dataList.get(i).getValue();
            dataVector[i][3] = dataList.get(i).getValue().length();
            dataVector[i][4] = DataSizeUtil.format(dataList.get(i).getValue().getBytes().length);
        }
        this.setDataVector(dataVector, columNames);
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
