package org.dromara.redisfront.dao;

import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.dao.entity.ConnectGroupEntity;
import org.dromara.redisfront.model.ConnectInfo;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * ConnectGroupDao
 *
 * @author Jin
 */
@Slf4j
public class ConnectGroupDao {
    private final DataSource datasource;
    public static final String CONNECT_GROUP = "connect_group";
    public static ConnectGroupDao newInstance(DataSource datasource) {
        return new ConnectGroupDao(datasource);
    }

    private ConnectGroupDao(DataSource datasource) {
        this.datasource = datasource;
    }



    public long count() throws SQLException {
        return DbUtil.use(datasource).count(Entity.create(CONNECT_GROUP));
    }

    public ConnectGroupEntity getById(Object id) throws SQLException {
        Entity entity = DbUtil.use(datasource).get(CONNECT_GROUP, "", id);
        return entity.toBean(ConnectGroupEntity.class);
    }

    public void save(ConnectInfo connectInfo) {

    }

    public void update(ConnectInfo connectInfo) {

    }

    public void delete(Object id) throws SQLException {
        DbUtil.use(datasource).del(Entity.create(CONNECT_GROUP).set("group_id", id));
    }

}
