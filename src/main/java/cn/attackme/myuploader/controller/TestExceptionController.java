package cn.attackme.myuploader.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.attackme.myuploader.utils.LogUtils.logToFile;

/**
 * 测试日志功能
 */
@RestController
@RequestMapping("/Ex")
public class TestExceptionController {
    /**
     * 测试日志切面
     * @return
     */
    @GetMapping("/aspect")
    public int aspect() {
        int i = 1 / 0;
        return i;
    }

    /**
     * 测试日志util
     */
    @GetMapping("/util")
    public void util() {
        try {
            System.out.println(1/0);
        } catch (Exception e) {
            logToFile(e);
        }
    }
}
