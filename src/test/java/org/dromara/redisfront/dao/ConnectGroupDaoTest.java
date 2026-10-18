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
import java.util.List;

/**
 * 验证 ConnectGroupDao.save 能拿到自增主键 group_id。
 * <p>
 * 复现的 bug：save 原来返回的是自己拼出来的 Entity，里面只 set 过 group_name，
 * 从没 set 过 id，调用方 entity.getInt("id") 恒为 null（Dict.getInt 对缺失字段
 * 返回 null 而不抛异常），于是 connect_detail.group_id 被写成 NULL，
 * JSON 导入的连接全部丢掉分组关系掉到根级别，但提示仍然是"导入成功"。
 */
public class ConnectGroupDaoTest {

    @TempDir
    Path tempDir;

    private SQLiteDataSource dataSource() throws Exception {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + tempDir.resolve("redisfront-group-test.db").toAbsolutePath());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(Constants.SQL_CREATE_CONNECT_GROUP);
            statement.execute(Constants.SQL_CREATE_CONNECT_DETAIL);
        }
        return dataSource;
    }

    private static ConnectDetailEntity newDetail(String name) {
        ConnectDetailEntity entity = new ConnectDetailEntity();
        entity.setName(name);
        entity.setHost("127.0.0.1");
        entity.setPort(6379);
        entity.setConnectMode("NORMAL");
        return entity;
    }

    @Test
    void save_should_return_generated_group_id() throws Exception {
        SQLiteDataSource dataSource = dataSource();

        Long groupId = ConnectGroupDao.newInstance(dataSource).save("生产环境");

        Assertions.assertNotNull(groupId, "save 必须返回自增主键，否则分组下的关联会丢");
        Assertions.assertTrue(groupId > 0);
    }

    @Test
    void save_should_return_incrementing_group_ids() throws Exception {
        SQLiteDataSource dataSource = dataSource();
        ConnectGroupDao dao = ConnectGroupDao.newInstance(dataSource);

        Long first = dao.save("first");
        Long second = dao.save("second");

        Assertions.assertNotEquals(first, second);
        Assertions.assertEquals(first + 1, second);
    }

    @Test
    void details_saved_with_returned_group_id_should_stay_in_their_group() throws Exception {
        SQLiteDataSource dataSource = dataSource();
        ConnectDetailDao detailDao = ConnectDetailDao.newInstance(dataSource);

        Long groupId = ConnectGroupDao.newInstance(dataSource).save("生产环境");

        ConnectDetailEntity detail = newDetail("cache-01");
        detail.setGroupId(groupId.intValue());
        detailDao.save(detail);

        List<ConnectDetailEntity> inGroup = detailDao.loadByGroup(groupId.intValue());
        Assertions.assertEquals(1, inGroup.size(), "连接应留在导入时所属的分组里");
        Assertions.assertEquals("cache-01", inGroup.get(0).getName());

        List<ConnectDetailEntity> ungrouped = detailDao.loadNoGroup();
        Assertions.assertTrue(ungrouped.isEmpty(), "连接不该掉到未分组");
    }

    @Test
    void details_saved_with_null_group_id_should_fall_back_to_ungrouped() throws Exception {
        SQLiteDataSource dataSource = dataSource();
        ConnectDetailDao detailDao = ConnectDetailDao.newInstance(dataSource);

        // 复现旧行为：group_id 传 null 时 parseBean 的 ignoreNullValue 会整列跳过，
        // 落库即 NULL，既不属于任何分组也不在"未分组"（group_id = -1）里
        ConnectDetailEntity detail = newDetail("orphan");
        detail.setGroupId(null);
        detailDao.save(detail);

        Assertions.assertTrue(detailDao.loadNoGroup().isEmpty());
        Assertions.assertTrue(detailDao.loadAll().size() == 1,
                "连接确实入库了，但分组关系已经丢失");
    }
}
