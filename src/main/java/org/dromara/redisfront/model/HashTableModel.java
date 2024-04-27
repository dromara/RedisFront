package org.dromara.redisfront.model;

import cn.hutool.core.io.unit.DataSizeUtil;

import javax.swing.table.DefaultTableModel;
import java.util.*;

/**
 * Redis Connection TableModel
 */
public class HashTableModel extends DefaultTableModel {

    private final Class<?>[] columnTypes = new Class<?>[]{
            String.class, String.class, Integer.class, String.class, Integer.class, String.class
    };
    private final boolean[] columnEditable = new boolean[]{
            false, false, false, false, false, false
    };

    public HashTableModel(List<Map.Entry<String, String>> dataList) {
        var dataVector = new Object[dataList.size()][6];
        for (var i = 0; i < dataList.size(); i++) {
            Map.Entry<String, String> Entry = dataList.get(i);
            dataVector[i][0] = Entry.getKey();
            dataVector[i][1] = Entry.getValue();
            dataVector[i][2] = Entry.getKey().length();
            dataVector[i][3] = DataSizeUtil.format(Entry.getKey().getBytes().length);
            dataVector[i][4] = Entry.getValue().length();
            dataVector[i][5] = DataSizeUtil.format(Entry.getValue().getBytes().length);
;
        }
        this.setDataVector(dataVector, new String[]{"key", "Value", "KeyLength", "KeySize", "ValueLength", "ValueSize"});
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
