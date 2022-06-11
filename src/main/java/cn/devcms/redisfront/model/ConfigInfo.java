package cn.devcms.redisfront.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class ConfigInfo {
    private List<ConnectInfo> connectInfoList;
}
