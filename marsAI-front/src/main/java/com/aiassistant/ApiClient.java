package com.aiassistant;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * API客户端
 * 负责与后端服务器通信
 */
public class ApiClient {
    private final String baseUrl;
    private final OkHttpClient httpClient;
    
    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        
        // 设置超长超时时间（24小时），让后端有充足时间处理
        // OkHttp 默认超时只有10秒，不设置会很快超时
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(86400, TimeUnit.SECONDS)    // 连接超时：24小时
            .readTimeout(86400, TimeUnit.SECONDS)        // 读取超时：24小时
            .writeTimeout(86400, TimeUnit.SECONDS)       // 写入超时：24小时
            .build();
        
        System.out.println("API客户端：超时设置为24小时，等待后端完成处理");
    }
    
    /**
     * 发送文字消息到后端
     * @param text 文字内容
     * @return AI回复
     * @throws IOException
     */
    public String sendText(String text) throws IOException {
        // 构建JSON请求体
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", text);
        
        String jsonString = jsonObject.toString();
        System.out.println("发送请求到: " + baseUrl + "/api/aiWithText");
        System.out.println("请求体: " + jsonString);
        
        RequestBody requestBody = RequestBody.create(
            jsonString,
            MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
            .url(baseUrl + "/api/aiWithText")
            .post(requestBody)
            .addHeader("Content-Type", "application/json;charset=UTF-8")
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            int code = response.code();
            String responseBody = response.body().string();
            
            System.out.println("响应状态码: " + code);
            System.out.println("响应内容: " + responseBody);
            
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: HTTP " + code + ", " + responseBody);
            }
            
            // 尝试解析为JSON
            try {
                JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
                
                // 提取回复内容（这里可能需要根据实际后端返回格式调整）
                if (responseJson.has("content")) {
                    return responseJson.get("content").getAsString();
                } else if (responseJson.has("message")) {
                    return responseJson.get("message").getAsString();
                } else {
                    return responseBody;
                }
            } catch (Exception e) {
                // 如果不是JSON格式，直接返回
                System.out.println("解析响应失败: " + e.getMessage());
                return responseBody;
            }
        }
    }
    
    /**
     * 发送音频文件到后端
     * @param audioFile 音频文件
     * @return AI回复
     * @throws IOException
     */
    public String sendAudio(File audioFile) throws IOException {
        if (audioFile == null || !audioFile.exists()) {
            throw new IOException("音频文件不存在");
        }
        
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("audio", audioFile.getName(),
                RequestBody.create(audioFile, MediaType.parse("audio/wav")))
            .build();
        
        Request request = new Request.Builder()
            .url(baseUrl + "/api/aiWithAudio")
            .post(requestBody)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response);
            }
            
            String responseBody = response.body().string();
            
            // 解析回复内容
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            
            // 提取回复内容
            if (responseJson.has("content")) {
                return responseJson.get("content").getAsString();
            } else if (responseJson.has("message")) {
                return responseJson.get("message").getAsString();
            } else {
                return responseBody;
            }
        }
    }
}

