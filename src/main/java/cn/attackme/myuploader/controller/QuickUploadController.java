package cn.attackme.myuploader.controller;

import cn.attackme.myuploader.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒传
 */
@RestController
@RequestMapping("/QuickUpload")
@CrossOrigin
public class QuickUploadController {
    @Autowired
    private FileService fileService;

    @GetMapping("/")
    public boolean upload(String md5) {
        return fileService.checkMd5(md5);
    }
}
