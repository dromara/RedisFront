package org.dromara.redisfront.model.table;

import cn.hutool.core.io.unit.DataSizeUtil;
import org.dromara.redisfront.model.value.RedisValueItem;

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

    public HashTableModel(List<Map.Entry<String, byte[]>> dataList) {
        var dataVector = new Object[dataList.size()][6];
        for (var i = 0; i < dataList.size(); i++) {
            Map.Entry<String, byte[]> entry = dataList.get(i);
            RedisValueItem valueItem = new RedisValueItem(entry.getValue());
            dataVector[i][0] = entry.getKey();
            dataVector[i][1] = valueItem;
            dataVector[i][2] = entry.getKey().length();
            dataVector[i][3] = DataSizeUtil.format(entry.getKey().getBytes().length);
            dataVector[i][4] = valueItem.toString().length();
            dataVector[i][5] = DataSizeUtil.format(valueItem.byteLength());
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
