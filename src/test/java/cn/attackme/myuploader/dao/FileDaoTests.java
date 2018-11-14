package cn.attackme.myuploader.dao;

import cn.attackme.myuploader.model.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileDaoTests {
    @Autowired
    private FileDao fileDao;

    private File testFile;

    /**
     * 生成File实例
     */
    @Before
    public void generateFile() {
        testFile = new File();
        testFile.setMd5("121221212");
        testFile.setName("test");
        testFile.setUploadTime(new Date());
        testFile.setPath(UUID.randomUUID().toString());
    }

    /**
     * File保存成功测试
     */
    @Test
    @Transactional
    @Rollback
    public void testSaveSuccess(){
        assertEquals(1,fileDao.save(testFile));
    }

    /**
     * name为null保存抛异常测试
     */
    @Test(expected = Exception.class)
    public void testSaveExceptionByNameIsNull() {
        testFile.setName(null);
        fileDao.save(testFile);
    }

    /**
     * 更新成功测试
     */
    @Test
    @Transactional
    @Rollback
    public void testUpdateSuccess() {
        fileDao.save(testFile);
        testFile.setPath(UUID.randomUUID().toString());
        assertEquals(1,fileDao.update(testFile));
    }

    /**
     * md5为null更新不抛异常测试
     */
    @Test
    @Transactional
    @Rollback
    public void testUpdateNoExceptionByMd5IsNull() {
        fileDao.save(testFile);
        testFile.setMd5(null);
        fileDao.update(testFile);
    }

    /**
     * 根据id获取File成功测试
     */
    @Test
    @Transactional
    @Rollback
    public void testGetByIdSuccess() {
        fileDao.save(testFile);
        assertNotNull(fileDao.getById(testFile.getId()));
    }

    /**
     * 根据不存在的id获取为null
     */
    @Test
    public void testGetByNotExitsIdReturnNull() {
        assertNull(fileDao.getById(new Long(0)));
    }

    /**
     * 根据id删除File成功
     */
    @Test
    @Transactional
    @Rollback
    public void testDeleteByIdSuccess() {
        fileDao.save(testFile);
        assertEquals(1,fileDao.deleteById(testFile.getId()));
    }

    /**
     * 根据不存在的id删除0条数据
     */
    @Test
    public void testDeleteByNotExitsReturn0() {
        assertEquals(0,fileDao.deleteById(new Long(0)));
    }

    /**
     * 根据多列查询成功
     */
    @Test
    @Transactional
    @Rollback
    public void testGetByFileSuccess() {
        fileDao.save(testFile);
        assertNotNull(fileDao.getByFile(testFile));
    }

    /**
     * 根据某一列获取不存在的file,返回null
     */
    @Test
    public void testGetByNotExistFileReturnNull() {
        File file = new File();
        file.setId(new Long(0));
        assertNull(fileDao.getByFile(file));
    }

    /**
     * File传null, where条件失效, 返回多行, 抛异常
     */
    @Test(expected = Exception.class)
    @Transactional
    @Rollback
    public void testGetByNullThrowException() {
        fileDao.save(testFile);
        fileDao.save(testFile);
        fileDao.getByFile(null);
    }

    /**
     * 根据不唯一列获取到多条数据, 抛出异常
     */
    @Test(expected = Exception.class)
    @Transactional
    @Rollback
    public void testGetByNotUniqueColumnThrowException() {
        fileDao.save(testFile);
        fileDao.save(testFile);
        File file = new File();
        file.setMd5(testFile.getMd5());
        fileDao.getByFile(file);
    }
}
