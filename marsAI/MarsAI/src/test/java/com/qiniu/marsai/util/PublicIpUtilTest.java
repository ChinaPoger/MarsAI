package com.qiniu.marsai.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import com.qiniu.marsai.tool.PublicIpUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 公网IP工具测试类
 */
@SpringBootTest
class PublicIpUtilTest {

    @Resource
    private PublicIpUtil publicIpUtil;

    @Test
    void testGetPublicIp() {
        System.out.println("=== 测试获取公网IP ===");
        
        String ip = publicIpUtil.getPublicIp();
        System.out.println("获取到的公网IP: " + ip);
        
        assertNotNull(ip, "公网IP不应为空");
        assertTrue(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"), 
                "IP格式应该正确");
        
        System.out.println("IP格式验证通过");
    }

    @Test
    void testGetPublicIpWithTimeout() {
        System.out.println("=== 测试带超时的公网IP获取 ===");
        
        String ip = publicIpUtil.getPublicIp(3000); // 3秒超时
        System.out.println("获取到的公网IP: " + ip);
        
        if (ip != null) {
            assertTrue(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"), 
                    "IP格式应该正确");
            System.out.println("IP格式验证通过");
        } else {
            System.out.println("获取IP超时或失败");
        }
    }

    @Test
    void testGetPublicIpAsync() throws Exception {
        System.out.println("=== 测试异步获取公网IP ===");
        
        var future = publicIpUtil.getPublicIpAsync();
        String ip = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
        
        System.out.println("异步获取到的公网IP: " + ip);
        
        if (ip != null) {
            assertTrue(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"), 
                    "IP格式应该正确");
            System.out.println("异步IP格式验证通过");
        }
    }

    @Test
    void testGetPublicIpConcurrent() {
        System.out.println("=== 测试并发获取公网IP ===");
        
        String ip = publicIpUtil.getPublicIpConcurrent(5000);
        System.out.println("并发获取到的公网IP: " + ip);
        
        if (ip != null) {
            assertTrue(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"), 
                    "IP格式应该正确");
            System.out.println("并发IP格式验证通过");
        } else {
            System.out.println("并发获取IP失败");
        }
    }

    @Test
    void testGetIpInfo() {
        System.out.println("=== 测试获取IP详细信息 ===");
        
        // 先获取公网IP
        String ip = publicIpUtil.getPublicIp();
        if (ip != null) {
            System.out.println("当前公网IP: " + ip);
            
            // 获取IP详细信息
            String ipInfo = publicIpUtil.getIpInfo(ip);
            System.out.println("IP详细信息: " + ipInfo);
            
            assertNotNull(ipInfo, "IP信息不应为空");
        } else {
            System.out.println("无法获取公网IP，跳过IP信息测试");
        }
    }

    @Test
    void testMultipleCalls() {
        System.out.println("=== 测试多次调用 ===");
        
        for (int i = 1; i <= 3; i++) {
            System.out.println("第" + i + "次调用:");
            String ip = publicIpUtil.getPublicIp();
            System.out.println("IP: " + ip);
            
            if (ip != null) {
                assertTrue(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"), 
                        "第" + i + "次获取的IP格式应该正确");
            }
            
            // 避免请求过于频繁
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("多次调用测试完成");
    }
}



