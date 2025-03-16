package org.dromara.redisfront.dao;

import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * ConnectService
 *
 * @author Jin
 */
public class ConnectDetailDao {

    public static final String TABLE_NAME = "connect_detail";

    private final DataSource datasource;

    public static ConnectDetailDao newInstance(DataSource datasource) {
        return new ConnectDetailDao(datasource);
    }

    private ConnectDetailDao(DataSource datasource) {
        this.datasource = datasource;
    }

    public List<ConnectDetailEntity> loadAll() throws SQLException {
        return DbUtil.use(datasource).findAll(Entity.create(TABLE_NAME).set("group_id", -1), ConnectDetailEntity.class);
    }

    public List<ConnectDetailEntity> loadAll(Object id) throws SQLException {
        return DbUtil.use(datasource).findAll(Entity.create(TABLE_NAME).set("group_id", id), ConnectDetailEntity.class);
    }

    public ConnectDetailEntity getById(Object id) throws SQLException {
        Entity entity = DbUtil.use(datasource).get(TABLE_NAME, "group_id", id);
        return entity.toBean(ConnectDetailEntity.class);
    }

    public void save(ConnectDetailEntity connectDetailEntity) throws SQLException {
        Entity entity = Entity.create(TABLE_NAME).parseBean(connectDetailEntity, true, true);
        DbUtil.use(datasource).insert(entity);
    }

    public void update(Object id, ConnectDetailEntity connectDetailEntity) throws SQLException {
        Entity entity = Entity.create(TABLE_NAME)
                .parseBean(connectDetailEntity, true, true);
        DbUtil.use(datasource).update(entity, Entity.create().set("id", id));
    }

    public void delete(Object id) throws SQLException {
        DbUtil.use(datasource).del(TABLE_NAME, "id", id);
    }
    public void deleteByGroupId(Object id) throws SQLException {
        DbUtil.use(datasource).del(TABLE_NAME, "group_id", id);
    }

}
