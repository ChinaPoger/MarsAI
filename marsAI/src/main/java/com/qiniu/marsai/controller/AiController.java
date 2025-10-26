package com.qiniu.marsai.controller;

import com.qiniu.marsai.service.AiHelperService;
import com.qiniu.marsai.service.AsrService;
import com.qiniu.marsai.tool.PublicIpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * IP查询控制器
 */
@RestController
public class AiController {

    @Resource
    private AiHelperService aiHelperService;

    @Resource
    private AsrService asrService;

    @Value("${amap.web-api-key:7ce7be0fbc824997793aaf5c6f6005dc}")
    private String amapWebApiKey;

    @Value("${amap.security-js-code:d32844a5ef2ed1dce96e7d683a17fe49}")
    private String amapSecurityJsCode;

    @CrossOrigin
    @RequestMapping(value = "/aiWithText", produces = "application/json;charset=UTF-8")
    public String loadSessions(String cmd, @RequestBody(required = false) String body, HttpServletRequest request,
                               HttpServletResponse response) {
        String text = "";

        String returnJson = "";
        JSONObject bodyJson = null;
        try {
            bodyJson = JSONObject.fromObject(body);
            text = bodyJson.getString("text");

            returnJson = aiHelperService.chat("1", text);
        } catch (Exception e) {
            e.printStackTrace();
            return JSONObject.fromObject("{msg:'err'}").toString();
        }
        System.out.println(JSONObject.fromObject(returnJson).toString());
        return JSONObject.fromObject(returnJson).toString();
    }
    @CrossOrigin
  @RequestMapping("/aiWithAudio")
    public String transcribeAudio(@RequestParam("audio") MultipartFile audioFile) {
        //音频转文字
        String returnJson = "";
        try {
        String message = asrService.transferAudio(audioFile);
        String sessionId = "1";
        returnJson = aiHelperService.chat(sessionId, message);
            System.out.println(returnJson);
        } catch (Exception e) {
            e.printStackTrace();
            return JSONObject.fromObject("{msg:'err'}").toString();
        }

        System.out.println(JSONObject.fromObject(returnJson).toString());
        return JSONObject.fromObject(returnJson).toString();
    }

    @CrossOrigin
    @GetMapping("/map-config")
    public Map<String, String> getMapConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("webApiKey", amapWebApiKey);
        config.put("securityJsCode", amapSecurityJsCode);
        return config;
    }

}
