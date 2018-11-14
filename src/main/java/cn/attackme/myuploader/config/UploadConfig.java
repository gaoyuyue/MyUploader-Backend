package cn.attackme.myuploader.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UploadConfig {

    public static String path;

    @Value("${upload.path}")
    public void setPath(String path) {
        UploadConfig.path = path;
    }
}
