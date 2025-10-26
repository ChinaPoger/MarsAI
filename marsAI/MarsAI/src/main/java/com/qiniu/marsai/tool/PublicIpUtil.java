package com.qiniu.marsai.tool;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 获取公网IP的工具类
 * 使用多个国内IP查询服务作为备选方案
 */
@Component
public class PublicIpUtil {

    // 国内IP查询服务列表
    private static final List<String> IP_SERVICES = List.of(
            "https://ip.3322.net",           // 3322网络
            "https://myip.ipip.net",         // IPIP.net
            "https://ip.360.cn/ipquery",     // 360
            "https://ip.cn/api/index",       // IP.cn
            "https://ipinfo.io/ip",          // IPInfo
            "https://api.ipify.org",         // IPify
            "https://icanhazip.com",         // icanhazip
            "https://ident.me"              // ident.me
    );

    /**
     * 获取公网IP地址
     * @return 公网IP地址，如果获取失败返回null
     */
    @Tool(name = "getMyPublicIp", value = """
           This function ia aim to get my public address.
            """
    )
    public String getPublicIp() {
        return getPublicIp(5000); // 默认5秒超时
    }

    /**
     * 获取公网IP地址（带超时设置）
     * @param timeoutMs 超时时间（毫秒）
     * @return 公网IP地址，如果获取失败返回null
     */

    public String getPublicIp(int timeoutMs) {
        // 尝试多个服务，只要有一个成功就返回
        for (String service : IP_SERVICES) {
            try {
                String ip = getIpFromService(service, timeoutMs);
                if (ip != null && isValidIp(ip)) {
                    System.out.println("成功从 " + service + " 获取到公网IP: " + ip);
                    return ip;
                }
            } catch (Exception e) {
                System.err.println("从 " + service + " 获取IP失败: " + e.getMessage());
            }
        }
        
        System.err.println("所有IP查询服务都失败了");
        return null;
    }

    /**
     * 异步获取公网IP地址
     * @return CompletableFuture<String>
     */
    public CompletableFuture<String> getPublicIpAsync() {
        return CompletableFuture.supplyAsync(() -> getPublicIp());
    }

    /**
     * 并发获取公网IP，返回第一个成功的结果
     * @param timeoutMs 超时时间（毫秒）
     * @return 公网IP地址
     */
    public String getPublicIpConcurrent(int timeoutMs) {
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        // 为每个服务创建异步任务
        for (String service : IP_SERVICES) {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return getIpFromService(service, timeoutMs);
                } catch (Exception e) {
                    return null;
                }
            }).orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
            futures.add(future);
        }
        
        // 等待第一个成功的结果
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(
                futures.toArray(new CompletableFuture[0])
        );
        
        try {
            Object result = anyOf.get(timeoutMs, TimeUnit.MILLISECONDS);
            if (result instanceof String) {
                String ip = (String) result;
                if (isValidIp(ip)) {
                    return ip;
                }
            }
        } catch (Exception e) {
            System.err.println("并发获取IP失败: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * 从指定服务获取IP
     * @param serviceUrl 服务URL
     * @param timeoutMs 超时时间
     * @return IP地址
     * @throws IOException 网络异常
     */
    private String getIpFromService(String serviceUrl, int timeoutMs) throws IOException {
        URL url = new URL(serviceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // 设置请求属性
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            String result = response.toString().trim();
            
            // 处理不同服务的响应格式
            if (serviceUrl.contains("ip.360.cn")) {
                // 360服务返回JSON格式
                if (result.contains("\"ip\"")) {
                    int start = result.indexOf("\"ip\":\"") + 6;
                    int end = result.indexOf("\"", start);
                    if (start > 5 && end > start) {
                        result = result.substring(start, end);
                    }
                }
            } else if (serviceUrl.contains("ip.cn")) {
                // IP.cn服务返回JSON格式
                if (result.contains("\"ip\"")) {
                    int start = result.indexOf("\"ip\":\"") + 6;
                    int end = result.indexOf("\"", start);
                    if (start > 5 && end > start) {
                        result = result.substring(start, end);
                    }
                }
            }
            
            return result;
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 验证IP地址格式是否正确
     * @param ip IP地址字符串
     * @return 是否为有效IP
     */
    private boolean isValidIp(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        ip = ip.trim();
        
        // 简单的IP格式验证
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 获取IP地址的详细信息（地理位置等）
     * @param ip IP地址
     * @return IP信息
     */
    public String getIpInfo(String ip) {
        if (!isValidIp(ip)) {
            return "无效的IP地址";
        }
        
        try {
            String url = "https://ip.360.cn/ipquery?ip=" + ip;
            URL requestUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", 
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                return response.toString();
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            return "获取IP信息失败: " + e.getMessage();
        }
    }
}
