package org.dromara.redisfront.model;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Redis Connection TableModel
 */
public class PubSubTableModel extends DefaultTableModel {

    private final Class<?>[] columnTypes = new Class<?>[]{
            String.class, String.class, String.class
    };
    private final boolean[] columnEditable = new boolean[]{
            false, true, true
    };

    public PubSubTableModel(List<MessageInfo> dataList) {
        var dataVector = new Object[dataList.size()][3];
        for (var i = 0; i < dataList.size(); i++) {
            dataVector[i][0] = dataList.get(i).getDate();
            dataVector[i][1] = dataList.get(i).getChannel();
            dataVector[i][2] = dataList.get(i).getMessage();
        }
        this.setDataVector(dataVector, new String[]{
                "时间",
                "通道",
                "消息",
        });
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
