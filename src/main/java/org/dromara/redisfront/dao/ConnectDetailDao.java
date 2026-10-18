package org.dromara.redisfront.dao;

import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.dialect.Dialect;
import cn.hutool.db.dialect.DialectFactory;
import cn.hutool.json.JSONUtil;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        return DbUtil.use(datasource).findAll(Entity.create(TABLE_NAME), ConnectDetailEntity.class);
    }

    public List<ConnectDetailEntity> loadNoGroup() throws SQLException {
        return DbUtil.use(datasource).findAll(Entity.create(TABLE_NAME).set("group_id", -1), ConnectDetailEntity.class);
    }

    public List<ConnectDetailEntity> loadByGroup(Object id) throws SQLException {
        return DbUtil.use(datasource).findAll(Entity.create(TABLE_NAME).set("group_id", id), ConnectDetailEntity.class);
    }

    public ConnectDetailEntity getById(Object id) throws SQLException {
        Entity entity = DbUtil.use(datasource).get(TABLE_NAME, "group_id", id);
        return entity.toBean(ConnectDetailEntity.class);
    }

    /**
     * 插入连接明细并返回自增主键。
     * <p>
     * 调用方需要拿这个 id 回填到 RedisConnectContext.id——它是连接身份，
     * SSH 会话表、事件路由、状态栏线程池、以及标签页去重都以它为 key。
     * <p>
     * 注意：这里不能用 Hutool 的 insertForGeneratedKey，sqlite-jdbc 没有实现
     * Statement.getGeneratedKeys()，会抛 SQLFeatureNotSupportedException。
     * SQLite 取自增主键只能从插入时那条连接上读 last_insert_rowid()。
     *
     * @return 自增主键
     */
    public Long save(ConnectDetailEntity connectDetailEntity) throws SQLException {
        Entity entity = Entity.create(TABLE_NAME).parseBean(connectDetailEntity, true, true);
        Dialect dialect = DialectFactory.newDialect(datasource);
        try (Connection connection = datasource.getConnection();
             PreparedStatement statement = dialect.psForInsert(connection, entity)) {
            statement.executeUpdate();
            // last_insert_rowid() 是连接级状态，必须和上面的 insert 共用同一条连接
            try (Statement query = connection.createStatement();
                 ResultSet resultSet = query.executeQuery("select last_insert_rowid()")) {
                return resultSet.next() ? resultSet.getLong(1) : null;
            }
        }
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
    public void updateGroupId(Object connectId, Object newGroupId) throws SQLException {
        DbUtil.use(datasource).update(
            Entity.create(TABLE_NAME).set("group_id", newGroupId),
            Entity.create().set("id", connectId)
        );
    }
}
