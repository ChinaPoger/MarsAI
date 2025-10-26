package com.aiassistant;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 音频录制器
 * 负责录制麦克风音频
 */
public class AudioRecorder {
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean BIG_ENDIAN = false;
    
    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;
    private ByteArrayOutputStream audioData;
    private boolean isRecording;
    
    public AudioRecorder() {
        audioFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            SAMPLE_RATE,
            SAMPLE_SIZE_IN_BITS,
            CHANNELS,
            (SAMPLE_SIZE_IN_BITS / 8) * CHANNELS,
            SAMPLE_RATE,
            BIG_ENDIAN
        );
    }
    
    /**
     * 开始录音
     * @throws LineUnavailableException
     */
    public void startRecording() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("音频格式不受支持");
        }
        
        targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        
        audioData = new ByteArrayOutputStream();
        isRecording = true;
        
        // 开始录音线程
        Thread recordingThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            
            while (isRecording) {
                int count = targetDataLine.read(buffer, 0, buffer.length);
                if (count > 0) {
                    audioData.write(buffer, 0, count);
                }
            }
        });
        
        recordingThread.start();
    }
    
    /**
     * 停止录音
     */
    public void stopRecording() {
        isRecording = false;
        
        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
            targetDataLine = null;
        }
    }
    
    /**
     * 获取录制好的音频文件
     * @return 音频文件
     * @throws IOException
     */
    public File getRecordedFile() throws IOException {
        if (audioData == null || audioData.size() == 0) {
            return null;
        }
        
        byte[] audioBytes = audioData.toByteArray();
        
        // 创建临时WAV文件
        File tempFile = File.createTempFile("audio_record_", ".wav");
        
        try (AudioInputStream audioInputStream = new AudioInputStream(
            new java.io.ByteArrayInputStream(audioBytes),
            audioFormat,
            audioBytes.length / audioFormat.getFrameSize()
        )) {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, tempFile);
        }
        
        return tempFile;
    }
    
    /**
     * 检查是否正在录音
     * @return 是否正在录音
     */
    public boolean isRecording() {
        return isRecording;
    }
}

