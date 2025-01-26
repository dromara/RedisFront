package org.dromara.redisfront.model;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * ConnectInfo
 *
 * @author Jin
 */
@Setter
@Getter
public class MessageInfo implements Serializable {

    private String date;
    private String channel;
    private String message;

}
