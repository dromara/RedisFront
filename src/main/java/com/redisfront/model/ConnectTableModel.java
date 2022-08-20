package com.redisfront.model;

import com.redisfront.commons.util.LocaleUtils;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Redis Connection TableModel
 */
public class ConnectTableModel extends DefaultTableModel {

    private final Class<?>[] columnTypes = new Class<?>[]{
            String.class, String.class, String.class, String.class, String.class, String.class
    };
    private final boolean[] columnEditable = new boolean[]{
            false, false, false, false, false, false
    };

    public ConnectTableModel(List<ConnectInfo> dataList) {
        var dataVector = new Object[dataList.size()][6];
        for (var i = 0; i < dataList.size(); i++) {
            dataVector[i][0] = dataList.get(i).id();
            dataVector[i][1] = dataList.get(i).title();
            dataVector[i][2] = dataList.get(i).host();
            dataVector[i][3] = dataList.get(i).port();
            dataVector[i][4] = dataList.get(i).ssl().toString();
            dataVector[i][5] = dataList.get(i).connectMode();
        }
        this.setDataVector(dataVector, new String[]{
                LocaleUtils.getMessageFromBundle("OpenConnectDialog.ConnectTableModel.id"),
                LocaleUtils.getMessageFromBundle("OpenConnectDialog.ConnectTableModel.name"),
                LocaleUtils.getMessageFromBundle("OpenConnectDialog.ConnectTableModel.host"),
                LocaleUtils.getMessageFromBundle("OpenConnectDialog.ConnectTableModel.port"),
                LocaleUtils.getMessageFromBundle("OpenConnectDialog.ConnectTableModel.SSL"),
                LocaleUtils.getMessageFromBundle("OpenConnectDialog.ConnectTableModel.connectType")
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
