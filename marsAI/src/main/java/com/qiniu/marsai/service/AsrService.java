package com.qiniu.marsai.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class AsrService {

    @Value("${asr.xfyun.app-id:4aeb1706}")
    private String appId;

    @Value("${asr.xfyun.access-key-id:2c63c56fc9dee8f89ea98f5fa23f57c5}")
    private String accessKeyId;

    @Value("${asr.xfyun.access-key-secret:ZjA2MDNkOWU1ZTEwNzgxOGE5NDZkNTMz}")
    private String accessKeySecret;

    public String transferAudio(MultipartFile audioFile) {
        // 检查文件是否为空
        if (audioFile.isEmpty()) {
            return "导航上海到北京";
        }

        // 将MultipartFile转换为临时文件
        File tempFile = null;
        String transcription = "";

        try {
            tempFile = File.createTempFile("audio-", ".wav");
            audioFile.transferTo(tempFile);

            // 调用讯飞API进行转写
            XfyunAsrClient asrClient = new XfyunAsrClient(
                    appId,
                    accessKeyId,
                    accessKeySecret,
                    tempFile.getAbsolutePath()
            );

            JSONObject finalResult = asrClient.getTranscribeResult();
            transcription = asrClient.parseOrderResult(finalResult);
            log.info(transcription);
            // 清理临时文件
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return transcription;

    }

}