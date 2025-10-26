package com.aiassistant;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 浏览器帮助类
 * 用于打开浏览器并加载URL
 */
public class BrowserHelper {
    
    /**
     * 打开浏览器
     * @param url 要打开的URL
     */
    public static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    System.out.println("已打开浏览器: " + url);
                } else {
                    System.out.println("不支持BROWSE操作");
                }
            } else {
                System.out.println("Desktop不可用");
                // 尝试使用Runtime启动浏览器
                String os = System.getProperty("os.name").toLowerCase();
                Runtime runtime = Runtime.getRuntime();
                
                if (os.contains("win")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    runtime.exec("open " + url);
                } else if (os.contains("nix") || os.contains("nux")) {
                    runtime.exec("xdg-open " + url);
                }
            }
        } catch (Exception e) {
            System.err.println("打开浏览器失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 构建带参数的URL
     * @param baseUrl 基础URL
     * @param query 查询参数
     * @return 完整的URL
     */
    public static String buildUrl(String baseUrl, String query) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            return baseUrl + "?query=" + encodedQuery;
        } catch (Exception e) {
            e.printStackTrace();
            return baseUrl;
        }
    }
    
    /**
     * 打开聊天页面（针对chat.html）
     * @param chatUrl 聊天页面URL
     * @param message 要发送的消息
     */
    public static void openChat(String chatUrl, String message) {
        String url = buildUrl(chatUrl, message);
        openBrowser(url);
    }
}


