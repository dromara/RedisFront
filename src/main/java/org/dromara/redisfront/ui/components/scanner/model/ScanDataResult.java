package org.dromara.redisfront.ui.components.scanner.model;

import lombok.Data;

import javax.swing.table.TableModel;

@Data
public class ScanDataResult<M extends TableModel> {

    private Long len;

    private String dataSize;

    private String loadSize;

    private Boolean isFinished;

    private M data;
}
