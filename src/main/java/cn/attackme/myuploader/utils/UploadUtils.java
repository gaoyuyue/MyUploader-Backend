package cn.attackme.myuploader.utils;

import java.util.HashMap;
import java.util.Map;

import static cn.attackme.myuploader.utils.FileUtils.generateFileName;

/**
 * 分块上传工具类
 */
public class UploadUtils {
    /**
     * 内部类记录分块上传文件信息
     */
    private static class Value {
        String name;
        boolean[] status;

        Value(int n) {
            this.name = generateFileName();
            this.status = new boolean[n];
        }
    }

    private static Map<String, Value> chunkMap = new HashMap<>();

    /**
     * 判断文件所有分块是否已上传
     * @param key
     * @return
     */
    public static boolean isUploaded(String key) {
        if (isExist(key)) {
            for (boolean b : chunkMap.get(key).status) {
                if (!b) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 判断文件是否有分块已上传
     * @param key
     * @return
     */
    private static boolean isExist(String key) {
        return chunkMap.containsKey(key);
    }

    /**
     * 为文件添加上传分块记录
     * @param key
     * @param chunk
     */
    public static void addChunk(String key, int chunk) {
        chunkMap.get(key).status[chunk] = true;
    }

    /**
     * 从map中删除键为key的键值对
     * @param key
     */
    public static void removeKey(String key) {
        if (isExist(key)) {
            chunkMap.remove(key);
        }
    }

    /**
     * 获取随机生成的文件名
     * @param key
     * @param chunks
     * @return
     */
    public static String getFileName(String key, int chunks) {
        if (!isExist(key)) {
            synchronized (UploadUtils.class) {
                if (!isExist(key)) {
                    chunkMap.put(key, new Value(chunks));
                }
            }
        }
        return chunkMap.get(key).name;
    }
}
