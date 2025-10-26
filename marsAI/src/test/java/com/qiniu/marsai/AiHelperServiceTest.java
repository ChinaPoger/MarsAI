package com.qiniu.marsai;

import com.qiniu.marsai.service.AiHelperService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiHelperServiceTest {


    @Resource
    private AiHelperService aiHelperService;



    @Test
    void chatWidthMemory() {
        String result = aiHelperService.chat("1","我要骑自行车去乌鲁木齐");
        System.out.printf("result=%s\n", result);
    }
}