package com.qiniu.marsai.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Base64;
import javax.sound.sampled.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

public class XfyunAsrClient {
    private final String appid;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String audioFilePath;
    private final int audioDuration;
    private String orderId;
    private final String signatureRandom;
    private String lastBaseString;
    private String lastSignature;
    private static final String LFASR_HOST = "https://office-api-ist-dx.iflyaisol.com";
    private static final String API_UPLOAD = "/v2/upload";
    private static final String API_GET_RESULT = "/v2/getResult";

    public XfyunAsrClient(String appid, String accessKeyId, String accessKeySecret, String audioFilePath) {
        this.appid = appid;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.audioFilePath = checkAudioPath(audioFilePath);
        this.audioDuration = getWavDurationMs();
        this.signatureRandom = generateRandomStr();
    }

    private String checkAudioPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("音频文件不存在：" + path);
        }
        if (!path.toLowerCase().endsWith(".wav")) {
            throw new RuntimeException("当前代码仅支持WAV格式音频，您的文件格式为：" + getFileExtension(path));
        }
        return file.getAbsolutePath();
    }

    private static String getFileExtension(String path) {
        int lastDot = path.lastIndexOf(".");
        return lastDot > 0 ? path.substring(lastDot) : "";
    }

    private String generateRandomStr() {
        return generateRandomStr(16);
    }

    private String generateRandomStr(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String getLocalTimeWithTz() {
        // 使用SimpleDateFormat生成正确的格式：yyyy-MM-dd'T'HH:mm:ssZ
        // 注意：Z格式会生成如 2023-10-01T12:00:00+0800（没有冒号）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        sdf.setTimeZone(TimeZone.getDefault());

        // 讯飞API要求的是+0800格式（没有冒号），所以不需要修改
        return sdf.format(new Date());
    }

    private int getWavDurationMs() {
        try {
            File audioFile = new File(audioFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            long frames = audioInputStream.getFrameLength();
            float durationInSeconds = (float) frames / audioInputStream.getFormat().getFrameRate();
            audioInputStream.close();
            return (int) (durationInSeconds * 1000);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException("WAV文件解析失败：" + e.getMessage() + "，请确认文件为标准WAV格式（非损坏、非压缩）");
        }
    }

    public String generateSignature(Map<String, String> params) {
        // 排除signature参数，按参数名自然排序
        Map<String, String> signParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!"signature".equals(entry.getKey()) && entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                signParams.put(entry.getKey(), entry.getValue());
            }
        }

        // 构建baseString：对key和value都进行URL编码
        List<String> baseParts = new ArrayList<>();
        for (Map.Entry<String, String> entry : signParams.entrySet()) {
            try {
                String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                baseParts.add(encodedKey + "=" + encodedValue);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("URL编码失败", e);
            }
        }

        this.lastBaseString = String.join("&", baseParts);

        // HMAC-SHA1加密 + Base64编码
        try {
            Mac hmac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secretKey = new SecretKeySpec(accessKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            hmac.init(secretKey);
            byte[] hmacBytes = hmac.doFinal(lastBaseString.getBytes(StandardCharsets.UTF_8));
            this.lastSignature = Base64.getEncoder().encodeToString(hmacBytes);
            return this.lastSignature;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA1加密失败", e);
        }
    }

    public void uploadAudio() {
        // 1. 基础参数准备
        File audioFile = new File(audioFilePath);
        String audioSize = String.valueOf(audioFile.length());
        String audioName = audioFile.getName();
        String dateTime = getLocalTimeWithTz();

        System.out.println("音频文件：" + audioName);
        System.out.println("文件大小：" + audioSize + " 字节");
        System.out.println("音频时长：" + audioDuration + " 毫秒");
        System.out.println("生成的时间戳：" + dateTime); // 调试信息

        // 2. 构建URL参数
        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("appId", appid);
        urlParams.put("accessKeyId", accessKeyId);
        urlParams.put("dateTime", dateTime);
        urlParams.put("signatureRandom", signatureRandom);
        urlParams.put("fileSize", audioSize);
        urlParams.put("fileName", audioName);
        urlParams.put("language", "autodialect");
        urlParams.put("duration", String.valueOf(audioDuration));

        // 3. 生成签名
        String signature = generateSignature(urlParams);
        if (signature == null || signature.isEmpty()) {
            throw new RuntimeException("签名生成失败，结果为空");
        }

        // 4. 构建请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/octet-stream");
        headers.put("signature", signature);

        // 5. 构建最终请求URL
        List<String> encodedParams = new ArrayList<>();
        for (Map.Entry<String, String> entry : urlParams.entrySet()) {
            try {
                String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                encodedParams.add(encodedKey + "=" + encodedValue);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("URL编码失败", e);
            }
        }
        String uploadUrl = LFASR_HOST + API_UPLOAD + "?" + String.join("&", encodedParams);

        // 6. 读取音频文件并发送POST请求
        try {
            byte[] audioData = Files.readAllBytes(Paths.get(audioFilePath));
            String response = sendPostRequest(uploadUrl, headers, audioData, false);

            // 7. 解析响应结果
            JSONObject result = new JSONObject(response);
            System.out.println("上传结果：" + result.toString(2));

            // 8. 处理API业务错误
            if (!"000000".equals(result.optString("code"))) {
                throw new RuntimeException(String.format("上传失败（API错误）：\n错误码：%s\n错误描述：%s\n请求URL：%s\n签名原始串：%s\n签名值：%s", result.optString("code"), result.optString("descInfo", "未知错误"), uploadUrl, lastBaseString, lastSignature));
            }

            // 9. 上传成功，记录订单ID
            this.orderId = result.getJSONObject("content").getString("orderId");
            System.out.println("上传成功！订单ID：" + this.orderId);

        } catch (IOException e) {
            throw new RuntimeException("文件读取或网络请求失败：" + e.getMessage());
        }
    }

    public JSONObject getTranscribeResult() {
        if (orderId == null || orderId.isEmpty()) {
            System.out.println("未检测到订单ID，自动执行上传流程...");
            uploadAudio();
        }
        if (orderId == null || orderId.isEmpty()) {
            throw new RuntimeException("未获取到订单ID，无法查询转写结果");
        }

        // 构建查询参数
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("appId", appid);
        queryParams.put("accessKeyId", accessKeyId);
        queryParams.put("dateTime", getLocalTimeWithTz());
        queryParams.put("ts", String.valueOf(System.currentTimeMillis() / 1000)); // 秒级时间戳
        queryParams.put("orderId", orderId);
        queryParams.put("signatureRandom", signatureRandom);

        // 生成查询签名
        String querySignature = generateSignature(queryParams);
        Map<String, String> queryHeaders = new HashMap<>();
        queryHeaders.put("Content-Type", "application/json");
        queryHeaders.put("signature", querySignature);

        // 构建查询URL
        List<String> encodedQueryParams = new ArrayList<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            try {
                String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                encodedQueryParams.add(encodedKey + "=" + encodedValue);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("URL编码失败", e);
            }
        }
        String queryUrl = LFASR_HOST + API_GET_RESULT + "?" + String.join("&", encodedQueryParams);

        // 轮询查询
        int maxRetry = 10000;
        int retryCount = 0;
        while (retryCount < maxRetry) {
            try {
                String response = sendPostRequest(queryUrl, queryHeaders, "{}".getBytes(StandardCharsets.UTF_8), true);
                JSONObject result = new JSONObject(response);
                System.out.println(result.toString(2));

                if (!"000000".equals(result.optString("code"))) {
                    throw new RuntimeException("查询失败（API错误）：" + result.optString("descInfo", "未知错误"));
                }

                // 转写状态：3=处理中，4=完成
                int processStatus = result.getJSONObject("content").getJSONObject("orderInfo").getInt("status");
                if (processStatus == 4) {
                    System.out.println("转写完成！");
                    return result;
                } else if (processStatus != 3) {
                    throw new RuntimeException("转写异常：状态码=" + processStatus + "，描述=" + result.optString("descInfo"));
                }

                // 处理中，等待10秒后重试
                retryCount++;
                System.out.printf("转写处理中（已查询%d/%d次），10秒后再次查询...%n", retryCount, maxRetry);
                Thread.sleep(10000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("查询被中断", e);
            } catch (Exception e) {
                throw new RuntimeException("查询请求失败：" + e.getMessage());
            }
        }

        throw new RuntimeException("查询超时：已重试" + maxRetry + "次，订单ID：" + orderId);
    }

    private String sendPostRequest(String urlStr, Map<String, String> headers, byte[] data, boolean isJson) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);

            // 设置请求头
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            // 禁用SSL证书验证（仅用于测试环境）
            if (urlStr.startsWith("https")) {
                // 在生产环境中应该使用正确的SSL证书验证
                // 这里为了简化测试，禁用SSL验证
                trustAllCertificates();
            }

            // 发送数据
            try (OutputStream os = connection.getOutputStream()) {
                os.write(data);
                os.flush();
            }

            // 读取响应
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                // 尝试读取错误流
                String errorResponse = readErrorStream(connection);
                throw new RuntimeException("HTTP请求失败，状态码：" + responseCode + "，错误信息：" + errorResponse);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }

        } catch (Exception e) {
            throw new RuntimeException("网络请求失败：" + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readErrorStream(HttpURLConnection connection) {
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    return errorResponse.toString();
                }
            }
        } catch (Exception e) {
            // 忽略错误流读取异常
        }
        return "";
    }

    // 简单的SSL证书信任方法（仅用于测试）
    private void trustAllCertificates() {
        // 这里应该实现完整的SSL证书信任逻辑
        // 由于代码较长，这里简化处理，实际生产环境应该使用正确的证书验证
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
        System.setProperty("https.protocols", "TLSv1.2");
    }

    public static String parseOrderResult(JSONObject apiResponse) {
        try {
            // 从API响应中获取orderResult字段
            String orderResultStr = apiResponse.getJSONObject("content").optString("orderResult", "{}");

            // 处理转义字符问题
            String cleanedStr = orderResultStr.replace("\\\\", "\\");

            // 解析orderResult字符串为JSON对象
            JSONObject orderResult = new JSONObject(cleanedStr);

            // 提取所有w字段的值
            List<String> wValues = new ArrayList<>();

            // 遍历lattice数组
            if (orderResult.has("lattice")) {
                JSONArray lattice = orderResult.getJSONArray("lattice");
                for (int i = 0; i < lattice.length(); i++) {
                    JSONObject latticeItem = lattice.getJSONObject(i);
                    if (latticeItem.has("json_1best")) {
                        // 解析json_1best字段
                        JSONObject json1best = new JSONObject(latticeItem.getString("json_1best"));

                        // 处理st对象
                        if (json1best.has("st") && json1best.getJSONObject("st").has("rt")) {
                            JSONArray rtArray = json1best.getJSONObject("st").getJSONArray("rt");
                            for (int j = 0; j < rtArray.length(); j++) {
                                JSONObject rtItem = rtArray.getJSONObject(j);
                                if (rtItem.has("ws")) {
                                    JSONArray wsArray = rtItem.getJSONArray("ws");
                                    for (int k = 0; k < wsArray.length(); k++) {
                                        JSONObject wsItem = wsArray.getJSONObject(k);
                                        if (wsItem.has("cw")) {
                                            JSONArray cwArray = wsItem.getJSONArray("cw");
                                            for (int l = 0; l < cwArray.length(); l++) {
                                                JSONObject cwItem = cwArray.getJSONObject(l);
                                                if (cwItem.has("w")) {
                                                    wValues.add(cwItem.getString("w"));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 拼接所有w值
            return String.join("", wValues);

        } catch (Exception e) {
            System.out.println("JSON解析错误: " + e.getMessage());
            return "";
        }
    }
}
