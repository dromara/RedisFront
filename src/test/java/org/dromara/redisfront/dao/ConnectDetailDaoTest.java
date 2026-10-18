package org.dromara.redisfront.dao;

import org.dromara.redisfront.commons.constant.Constants;
import org.dromara.redisfront.model.entity.ConnectDetailEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sqlite.SQLiteDataSource;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 验证 ConnectDetailDao.save 能拿到自增主键。
 * <p>
 * 这不是单元测试的范畴，而是真实的 SQLite 行为验证：RedisConnectContext.id 在此之前
 * 恒为 0，靠的就是 save 返回的主键回填。用 insert() 只能拿到受影响行数，拿不到主键，
 * 所以这里同时也验证换成的 insertForGeneratedKey 在这套 Hutool + sqlite-jdbc 组合下确实可用。
 */
public class ConnectDetailDaoTest {

    @TempDir
    Path tempDir;

    private SQLiteDataSource dataSource() throws Exception {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + tempDir.resolve("redisfront-test.db").toAbsolutePath());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(Constants.SQL_CREATE_CONNECT_DETAIL);
        }
        return dataSource;
    }

    private static ConnectDetailEntity newEntity(String name) {
        ConnectDetailEntity entity = new ConnectDetailEntity();
        entity.setName(name);
        entity.setHost("127.0.0.1");
        entity.setPort(6379);
        entity.setConnectMode("NORMAL");
        return entity;
    }

    @Test
    void save_should_return_generated_primary_key() throws Exception {
        SQLiteDataSource dataSource = dataSource();

        Long id = ConnectDetailDao.newInstance(dataSource).save(newEntity("first"));

        Assertions.assertNotNull(id, "save 必须返回自增主键，否则连接身份无法回填");
        Assertions.assertTrue(id > 0);
    }

    @Test
    void save_should_return_incrementing_primary_keys() throws Exception {
        SQLiteDataSource dataSource = dataSource();
        ConnectDetailDao dao = ConnectDetailDao.newInstance(dataSource);

        Long first = dao.save(newEntity("first"));
        Long second = dao.save(newEntity("second"));

        Assertions.assertNotEquals(first, second);
        Assertions.assertEquals(first + 1, second);
    }

    @Test
    void saved_entity_should_be_loadable_by_the_returned_id() throws Exception {
        SQLiteDataSource dataSource = dataSource();
        ConnectDetailDao dao = ConnectDetailDao.newInstance(dataSource);

        Long id = dao.save(newEntity("loadable"));
        ConnectDetailEntity loaded = dao.loadAll().stream()
                .filter(entity -> id.intValue() == entity.getId())
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(loaded, "用 save 返回的 id 应该能查回这条连接");
        Assertions.assertEquals("loadable", loaded.getName());
    }

    /**
     * 复现的 bug：save() 原来是 void，AddConnectDialog 又写成 setId(getId()) 自赋值，
     * 于是每个新建连接运行期 id 恒为 0。MainComponent.addTab 按 getId() 去重，
     * 导致打开第二个新建连接时被折叠进第一个标签页——用户看到的是上一个连接的数据。
     */
    @Test
    void two_new_connections_should_get_distinct_non_zero_ids() throws Exception {
        SQLiteDataSource dataSource = dataSource();
        ConnectDetailDao dao = ConnectDetailDao.newInstance(dataSource);

        Long first = dao.save(newEntity("first"));
        Long second = dao.save(newEntity("second"));

        Assertions.assertNotEquals(0, first.intValue());
        Assertions.assertNotEquals(0, second.intValue());
        Assertions.assertNotEquals(first, second);
    }
}
