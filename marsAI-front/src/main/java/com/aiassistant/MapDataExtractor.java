package com.aiassistant;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 地图数据提取器
 * 从后端JSON响应中提取地图相关数据
 */
public class MapDataExtractor {
    
    /**
     * 从JSON响应中提取地图数据
     * @param response JSON响应字符串
     * @return MapData对象包含所有地图参数
     */
    public static MapData extractMapData(String response) {
        try {
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            
            // 首先检查是否有错误信息
            if (json.has("err") && !json.get("err").isJsonNull()) {
                String err = json.get("err").getAsString();
                if (err != null && !err.trim().isEmpty()) {
                    System.out.println("检测到错误信息: " + err);
                    return null; // 有错误，不提取地图数据
                }
            }
            
            // 检查出发地和目的地是否为空对象
            JsonObject departure = json.getAsJsonObject("出发地");
            JsonObject destination = json.getAsJsonObject("目的地");
            
            if (departure == null || departure.isEmpty() || 
                destination == null || destination.isEmpty()) {
                System.out.println("出发地或目的地为空");
                return null;
            }
            
            // 提取出发地坐标
            double startLng = departure.get("经度").getAsDouble();
            double startLat = departure.get("维度").getAsDouble();
            
            // 提取目的地坐标
            double endLng = destination.get("经度").getAsDouble();
            double endLat = destination.get("维度").getAsDouble();
            
            // 提取出行方式
            String mode = json.get("出行方式").getAsString();
            String modeEn = convertTravelMode(mode);
            
            // 提取policy（如果有且有效）
            int policy = 0;
            if (json.has("policy") && !json.get("policy").isJsonNull()) {
                try {
                    String policyStr = json.get("policy").getAsString();
                    if (policyStr != null && !policyStr.trim().isEmpty()) {
                        policy = Integer.parseInt(policyStr);
                    }
                } catch (Exception e) {
                    // policy为空字符串或无效值时使用默认值0
                    policy = 0;
                }
            }
            
            MapData data = new MapData();
            data.startLng = startLng;
            data.startLat = startLat;
            data.endLng = endLng;
            data.endLat = endLat;
            data.mode = modeEn;
            data.policy = policy;
            
            return data;
                
        } catch (Exception e) {
            System.err.println("解析地图数据失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 地图数据类
     */
    public static class MapData {
        public double startLng;
        public double startLat;
        public double endLng;
        public double endLat;
        public String mode;
        public int policy;
    }
    
    /**
     * 转换出行方式为英文
     */
    private static String convertTravelMode(String mode) {
        switch (mode) {
            case "驾车":
            case "开车":
                return "driving";
            case "步行":
                return "walking";
            case "骑行":
            case "骑车":
                return "riding";
            case "公交":
            case "公共交通":
                return "transit";
            default:
                return "driving";
        }
    }
    
    /**
     * 构建完整的地图URL（高德地图格式）
     * @param baseUrl 基础URL（如: http://127.0.0.1:8081/api/map.html）
     * @param data 地图数据
     * @return 完整的URL（baseUrl?参数...）
     */
    public static String buildMapUrl(String baseUrl, MapData data) {
        if (data == null) return baseUrl;
        
        // baseUrl已经是完整的URL，直接在后面拼接参数
        StringBuilder url = new StringBuilder(baseUrl);
        
        // 检查URL是否已包含参数
        String separator = baseUrl.contains("?") ? "&" : "?";
        
        url.append(separator);
        url.append("startLng=").append(data.startLng);
        url.append("&startLat=").append(data.startLat);
        url.append("&endLng=").append(data.endLng);
        url.append("&endLat=").append(data.endLat);
        url.append("&mode=").append(data.mode);
        
        // 如果有policy，添加policy参数
        if (data.policy > 0) {
            url.append("&policy=").append(data.policy);
        }
        
        return url.toString();
    }
}

