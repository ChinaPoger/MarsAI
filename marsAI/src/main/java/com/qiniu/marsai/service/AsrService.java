package com.qiniu.marsai.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class AsrService {


    private static final String XFYUN_APPID = "4aeb1706";
    private static final String XFYUN_ACCESS_KEY_SECRET = "ZjA2MDNkOWU1ZTEwNzgxOGE5NDZkNTMz";
    private static final String XFYUN_ACCESS_KEY_ID = "2c63c56fc9dee8f89ea98f5fa23f57c5";


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
                    XFYUN_APPID,
                    XFYUN_ACCESS_KEY_ID,
                    XFYUN_ACCESS_KEY_SECRET,
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