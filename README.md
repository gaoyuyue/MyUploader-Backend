# MyUploader-Backend
> 单文件上传，多文件上传，大文件上传，断点续传，文件秒传，图片上传
> 
> 前端项目地址： [https://github.com/gaoyuyue/MyUploader](https://github.com/gaoyuyue/MyUploader)

# 简介
采用前后端分离的方式进行开发，实现了几种常用的文件上传功能。 前端采用 vue.js + plupload + element-ui 实现了文件在浏览器端的发送, 后端采用 spring boot + spring + spring mvc + mybatis 实现了文件在服务器端的接收和存储。

***本项目为后端实现***

# 效果图
> 演示地址： [https://gaoyuyue.github.io/MyUploader](https://gaoyuyue.github.io/MyUploader)
>
> *ps: 用git pages搭建的静态页面，只能演示前端功能*
### 单文件上传
![](https://i.imgur.com/vBr8ZJq.gif)
### 多文件上传
![](https://i.imgur.com/Db6eaMJ.gif)
### 大文件上传
![](https://i.imgur.com/qAyksE8.gif)
### 断点续传
![](https://i.imgur.com/I1G01MT.gif)
### 文件秒传
![](https://i.imgur.com/XHZHGeo.gif)
### 图片上传
![](https://i.imgur.com/HFZQnV3.gif)

# 后端实现
## 未分块上传
采用MultipartFile接收上传文件并使用FileOutputStream写入文件
```java
/**
 * 写入文件
 * @param target
 * @param src
 * @throws IOException
 */
public static void write(String target, InputStream src) throws IOException {
    OutputStream os = new FileOutputStream(target);
    byte[] buf = new byte[1024];
    int len;
    while (-1 != (len = src.read(buf))) {
        os.write(buf,0,len);
    }
    os.flush();
    os.close();
}
```
## 分块上传
采用MultipartFile接收上传文件的分块文件，上传参数包括：size：文件大小，chunk:分块号，chunks:总分块数
```java
@PostMapping("/")
public void upload(String name,
                   String md5,
                   Long size,
                   Integer chunks,
                   Integer chunk,
                   MultipartFile file) throws IOException {
    if (chunks != null && chunks != 0) {
        fileService.uploadWithBlock(name, md5,size,chunks,chunk,file);
    } else {
        fileService.upload(name, md5,file);
    }
}
```
### 分块上传信息的存储
使用Map来存储分块的上传信息,key为文件唯一标识由前端发送这里采用文件MD5作为文件的唯一标识，value为自定义的静态内部类，其中name属性为文件在服务器端存储的随机文件名其在该文件的第一个分块上传到服务器时随机生成(UUID)，boolean数组记录了分块的上传情况已上传置为true。
```java
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
```
### 分块写入文件
使用RandomAccessFile随机读写文件，通过targetSize指定文件的大小，通过seek定位分块在文件中对应位置，通过write方法进行写入。
```java
/**
 * 分块写入文件
 * @param target
 * @param targetSize
 * @param src
 * @param srcSize
 * @param chunks
 * @param chunk
 * @throws IOException
 */
public static void writeWithBlok(String target, Long targetSize, InputStream src, Long srcSize, Integer chunks, Integer chunk) throws IOException {
    RandomAccessFile randomAccessFile = new RandomAccessFile(target,"rw");
    randomAccessFile.setLength(targetSize);
    if (chunk == chunks - 1) {
        randomAccessFile.seek(targetSize - srcSize);
    } else {
        randomAccessFile.seek(chunk * srcSize);
    }
    byte[] buf = new byte[1024];
    int len;
    while (-1 != (len = src.read(buf))) {
        randomAccessFile.write(buf,0,len);
    }
    randomAccessFile.close();
}

```
### 分块上传逻辑
每次分块上传时首先获取对应文件在服务器端生成的随机文件名，如果不存在即在这之前还没有分块上传，则创建文件分块存储信息的键值对并存入map中，同时返回刚生成的随机文件名。然后将分块写入文件，同时将boolean数组中当前对应分块的位置置为true。最后判断该文件的所有分块是否全部上传，如果已全部上传完成则将文件信息写入数据库同时删除map中对应的键值对。
> ps: 曾用文件夹存储上传分块，当分块全部上传完成时按顺序合并文件夹内所有分块，然后删除文件夹，后来发现RandomAccessFile果断弃坑。
```java
/**
 * 分块上传文件
 * @param md5
 * @param size
 * @param chunks
 * @param chunk
 * @param file
 * @throws IOException
 */
public void uploadWithBlock(String name,
                            String md5,
                            Long size,
                            Integer chunks,
                            Integer chunk,
                            MultipartFile file) throws IOException {
    String fileName = getFileName(md5, chunks);
    FileUtils.writeWithBlok(UploadConfig.path + fileName, size, file.getInputStream(), file.getSize(), chunks, chunk);
    addChunk(md5,chunk);
    if (isUploaded(md5)) {
        removeKey(md5);
        fileDao.save(new File(name, md5,UploadConfig.path + fileName, new Date()));
    }
}
```
