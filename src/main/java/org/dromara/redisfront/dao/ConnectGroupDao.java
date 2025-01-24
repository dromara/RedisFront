package org.dromara.redisfront.dao;

import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.model.entity.ConnectGroupEntity;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * ConnectGroupDao
 *
 * @author Jin
 */
@Slf4j
public class ConnectGroupDao {
    private final DataSource datasource;
    public static final String TABLE_NAME = "connect_group";
    public static ConnectGroupDao newInstance(DataSource datasource) {
        return new ConnectGroupDao(datasource);
    }

    private ConnectGroupDao(DataSource datasource) {
        this.datasource = datasource;
    }

    public long count() throws SQLException {
        return DbUtil.use(datasource).count(Entity.create(TABLE_NAME));
    }

    public List<ConnectGroupEntity> loadAll() throws SQLException {
        return DbUtil.use(datasource).findAll(Entity.create(TABLE_NAME), ConnectGroupEntity.class);
    }

    public ConnectGroupEntity getById(Object id) throws SQLException {
        Entity entity = DbUtil.use(datasource).get(TABLE_NAME, "group_id", id);
        return entity.toBean(ConnectGroupEntity.class);
    }

    public void save(String groupName) throws SQLException {
        Entity connectGroup = Entity.create(TABLE_NAME);
        connectGroup.set("group_name", groupName);
        DbUtil.use(datasource).insert(connectGroup);
    }

    public void update(Object id,String groupName) throws SQLException {
        DbUtil.use(datasource).update(Entity.create(TABLE_NAME)
                .set("group_name", groupName),
                Entity.create(TABLE_NAME)
                .set("group_id", id));
    }

    public void delete(Object id) throws SQLException {
        DbUtil.use(datasource).del(Entity.create(TABLE_NAME).set("group_id", id));
    }

}
