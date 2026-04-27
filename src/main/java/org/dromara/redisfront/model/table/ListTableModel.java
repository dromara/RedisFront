package org.dromara.redisfront.model.table;

import cn.hutool.core.io.unit.DataSizeUtil;
import org.dromara.redisfront.model.value.RedisValueItem;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Redis Connection TableModel
 */
public class ListTableModel extends DefaultTableModel {

    private final Class<?>[] columnTypes = new Class<?>[]{
            Integer.class, String.class, Integer.class, String.class
    };
    private final boolean[] columnEditable = new boolean[]{
            false, false, false, false
    };

    public ListTableModel(List<byte[]> dataList) {
        var dataVector = new Object[dataList.size()][4];
        for (var i = 0; i < dataList.size(); i++) {
            RedisValueItem item = new RedisValueItem(dataList.get(i));
            dataVector[i][0] = i + 1;
            dataVector[i][1] = item;
            dataVector[i][2] = item.toString().length();
            dataVector[i][3] = DataSizeUtil.format(item.byteLength());
        }
        this.setDataVector(dataVector, new String[]{"#", "Value", "Length", "Size"});
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
