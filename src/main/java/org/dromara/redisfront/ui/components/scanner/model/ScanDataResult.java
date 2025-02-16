package org.dromara.redisfront.ui.components.scanner.model;

import lombok.Data;

@Data
public class ScanDataResult<M> {

    private Long len;

    private String dataSize;

    private String loadSize;

    private Boolean isFinished;

    private M data;
}
