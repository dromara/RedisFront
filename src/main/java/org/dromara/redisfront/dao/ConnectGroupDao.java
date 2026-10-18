package org.dromara.redisfront.dao;

import cn.hutool.db.DbUtil;
import cn.hutool.db.Entity;
import cn.hutool.db.dialect.Dialect;
import cn.hutool.db.dialect.DialectFactory;
import lombok.extern.slf4j.Slf4j;
import org.dromara.redisfront.model.entity.ConnectGroupEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    /**
     * 插入连接分组并返回自增主键 group_id。
     * <p>
     * 调用方（导入连接）需要拿这个 id 关联分组下的连接明细。原来的实现返回自己拼出来的
     * Entity，里面从没 set 过 id，调用方 getInt("id") 恒得到 null，导致导入的连接
     * 全部丢掉分组关系掉到根级别。
     * <p>
     * 注意不能用 insertForGeneratedKey：sqlite-jdbc 未实现 Statement.getGeneratedKeys()。
     *
     * @return 自增主键 group_id
     */
    public Long save(String groupName) throws SQLException {
        Entity connectGroup = Entity.create(TABLE_NAME);
        connectGroup.set("group_name", groupName);
        Dialect dialect = DialectFactory.newDialect(datasource);
        try (Connection connection = datasource.getConnection();
             PreparedStatement statement = dialect.psForInsert(connection, connectGroup)) {
            statement.executeUpdate();
            // last_insert_rowid() 是连接级状态，必须和上面的 insert 共用同一条连接
            try (Statement query = connection.createStatement();
                 ResultSet resultSet = query.executeQuery("select last_insert_rowid()")) {
                return resultSet.next() ? resultSet.getLong(1) : null;
            }
        }
    }

    public void update(Object id, String groupName) throws SQLException {
        DbUtil.use(datasource).update(Entity.create(TABLE_NAME)
                        .set("group_name", groupName),
                Entity.create(TABLE_NAME)
                        .set("group_id", id));
    }

    public void delete(Object id) throws SQLException {
        DbUtil.use(datasource).del(Entity.create(TABLE_NAME).set("group_id", id));
    }
}
