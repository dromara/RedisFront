package org.dromara.redisfront.model.table;

import cn.hutool.json.JSONUtil;
import io.lettuce.core.StreamMessage;
import org.dromara.redisfront.commons.codec.RedisValueCodec;
import org.dromara.redisfront.commons.codec.ValueViewType;

import javax.swing.table.DefaultTableModel;
import java.util.LinkedHashMap;
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

    public StreamTableModel(List<StreamMessage<String, byte[]>> dataList) {
        var dataVector = new Object[dataList.size()][4];
        for (var i = 0; i < dataList.size(); i++) {
            var map = new LinkedHashMap<String, String>();
            dataList.get(i).getBody().forEach((k, v) -> map.put(k, RedisValueCodec.encode(v, ValueViewType.AUTO)));
            dataVector[i][0] = i + 1;
            dataVector[i][1] = dataList.get(i).getId();
            dataVector[i][2] = JSONUtil.toJsonStr(map) ;
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
