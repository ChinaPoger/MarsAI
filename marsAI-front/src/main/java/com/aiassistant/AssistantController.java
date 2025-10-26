package com.aiassistant;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.io.InputStream;
import java.util.Properties;

/**
 * AI助手控制器
 * 管理界面和控制逻辑
 */
public class AssistantController {
    private VBox root;
    private TextArea inputTextArea;
    private TextArea responseArea;
    private Button textSendButton;
    private Button voiceRecordButton;
    private Label statusLabel;
    
    private String currentQuery; // 存储当前查询内容
    
    private AudioRecorder audioRecorder;
    private ApiClient apiClient;
    private AtomicBoolean isRecording;
    
    private String chatUrl;
    private String mapUrl;
    private boolean autoOpenBrowser;
    
    public AssistantController() {
        isRecording = new AtomicBoolean(false);
        
        // 从配置文件读取后端地址
        String backendUrl = loadBackendUrl();
        apiClient = new ApiClient(backendUrl);
        audioRecorder = new AudioRecorder();
        
        // 从配置文件读取页面配置
        chatUrl = loadChatUrl();
        mapUrl = loadMapUrl();
        autoOpenBrowser = loadAutoOpenBrowser();
        
        initializeUI();
    }
    
    /**
     * 从配置文件加载后端URL
     */
    private String loadBackendUrl() {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("config.properties");
            
            if (inputStream != null) {
                prop.load(inputStream);
                String url = prop.getProperty("backend.url", "http://localhost:8080");
                System.out.println("后端地址配置: " + url);
                inputStream.close();
                return url;
            } else {
                System.out.println("未找到配置文件，使用默认地址: http://localhost:8080");
                return "http://localhost:8080";
            }
        } catch (Exception e) {
            System.out.println("加载配置文件失败，使用默认地址: http://localhost:8080");
            e.printStackTrace();
            return "http://localhost:8080";
        }
    }
    
    /**
     * 从配置文件加载聊天页面URL
     */
    private String loadChatUrl() {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("config.properties");
            
            if (inputStream != null) {
                prop.load(inputStream);
                String url = prop.getProperty("chat.url", "http://localhost:8081/api/chat.html");
                System.out.println("聊天页面地址配置: " + url);
                inputStream.close();
                return url;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "http://localhost:8081/api/chat.html";
    }
    
    /**
     * 从配置文件加载地图页面URL
     */
    private String loadMapUrl() {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("config.properties");
            
            if (inputStream != null) {
                prop.load(inputStream);
                String url = prop.getProperty("map.url", "http://127.0.0.1:8081/api/map.html");
                System.out.println("地图页面地址配置: " + url);
                inputStream.close();
                return url;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "http://127.0.0.1:8081/api/map.html";
    }
    
    /**
     * 从配置文件加载是否自动打开浏览器
     */
    private boolean loadAutoOpenBrowser() {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("config.properties");
            
            if (inputStream != null) {
                prop.load(inputStream);
                String autoOpen = prop.getProperty("auto.open.browser", "true");
                inputStream.close();
                return Boolean.parseBoolean(autoOpen);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    /**
     * 初始化用户界面
     */
    private void initializeUI() {
        root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: transparent;");  // 透明背景
        
        // 创建UI组件
        inputTextArea = new TextArea();
        inputTextArea.setPromptText("请输入您的问题...");
        inputTextArea.setPrefRowCount(5);
        inputTextArea.setWrapText(true);
        inputTextArea.setStyle("-fx-border-radius: 5; -fx-background-color: #fff;");
        
        responseArea = new TextArea();
        responseArea.setPromptText("AI的回复将显示在这里...");
        responseArea.setPrefRowCount(8);
        responseArea.setWrapText(true);
        responseArea.setEditable(false);
        responseArea.setStyle("-fx-border-radius: 5; -fx-background-color: #f0f9ff;");
        
        // 创建按钮
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        textSendButton = new Button("发送");
        textSendButton.setPrefWidth(120);
        textSendButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 5;");
        textSendButton.setOnAction(e -> sendTextMessage());
        
        voiceRecordButton = new Button("🎤 开始录音");
        voiceRecordButton.setPrefWidth(120);
        voiceRecordButton.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-background-radius: 5;");
        voiceRecordButton.setOnAction(e -> startVoiceRecording());
        
        buttonBox.getChildren().addAll(textSendButton, voiceRecordButton);
        
        // 创建状态标签
        statusLabel = new Label("就绪");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
        
        // 添加组件到根容器
        root.getChildren().addAll(
            new Label("📝 输入问题"),
            inputTextArea,
            buttonBox,
            statusLabel,
            new Label("AI回复"),
            responseArea
        );
    }
    
    /**
     * 创建紧凑输入区域（已弃用）
     */
    private StackPane createCharacterPane() {
        StackPane pane = new StackPane();
        pane.setPrefSize(200, 200);
        
        // 背景矩形
        javafx.scene.shape.Rectangle bg = new javafx.scene.shape.Rectangle(200, 200);
        bg.setFill(javafx.scene.paint.Color.valueOf("#F0F0F0"));
        bg.setEffect(new javafx.scene.effect.DropShadow(10, 0, 4, javafx.scene.paint.Color.valueOf("#00000044")));
        bg.setArcWidth(10);
        bg.setArcHeight(10);
        
        // AI助手图标和文字
        Label assistantIcon = new Label("🤖");
        assistantIcon.setStyle("-fx-font-size: 80px;");
        
        Label assistantName = new Label("AI助手");
        assistantName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        assistantName.setTranslateY(60);
        
        VBox contentBox = new VBox(5);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(assistantIcon, assistantName);
        
        pane.getChildren().addAll(bg, contentBox);
        
        return pane;
    }
    
    /**
     * 创建紧凑输入区域
     */
    private VBox createCompactInputArea() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 20; -fx-padding: 15; -fx-effect: dropshadow(gaussian, #00000044, 10, 0, 0, 2);");
        box.setMaxWidth(300);
        
        // 输入框
        inputTextArea = new TextArea();
        inputTextArea.setPromptText("输入你的问题...");
        inputTextArea.setPrefRowCount(2);
        inputTextArea.setWrapText(true);
        inputTextArea.setStyle("-fx-border-radius: 10;");
        
        // 按钮组
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        textSendButton = new Button("📝 发送");
        textSendButton.setPrefWidth(120);
        textSendButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 15;");
        textSendButton.setOnAction(e -> sendTextMessage());
        
        voiceRecordButton = new Button("🎤 说话");
        voiceRecordButton.setPrefWidth(120);
        voiceRecordButton.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-background-radius: 15;");
        voiceRecordButton.setOnAction(e -> startVoiceRecording());
        
        Button closeButton = new Button("✕");
        closeButton.setPrefWidth(40);
        closeButton.setStyle("-fx-background-color: #f56565; -fx-text-fill: white; -fx-background-radius: 15;");
        closeButton.setOnAction(e -> {
            // 关闭功能
        });
        
        buttonBox.getChildren().addAll(textSendButton, voiceRecordButton, closeButton);
        
        box.getChildren().addAll(inputTextArea, buttonBox);
        return box;
    }
    
    /**
     * 创建响应气泡
     */
    private VBox createResponseBubble() {
        VBox bubble = new VBox(5);
        bubble.setMaxWidth(280);
        bubble.setVisible(false);
        
        responseArea = new TextArea();
        responseArea.setPromptText("AI的回复...");
        responseArea.setPrefRowCount(4);
        responseArea.setWrapText(true);
        responseArea.setEditable(false);
        responseArea.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 15; -fx-border-radius: 15; -fx-padding: 10;");
        
        bubble.getChildren().addAll(responseArea);
        return bubble;
    }
    
    
    /**
     * 创建文字输入框
     */
    private VBox createTextInputBox() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 10; -fx-padding: 15;");
        
        Label label = new Label("💬 文字输入");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        inputTextArea = new TextArea();
        inputTextArea.setPromptText("请输入您的问题...");
        inputTextArea.setPrefRowCount(3);
        inputTextArea.setWrapText(true);
        inputTextArea.setStyle("-fx-border-radius: 5;");
        
        textSendButton = new Button("发送");
        textSendButton.setPrefWidth(200);
        textSendButton.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-size: 14px; -fx-padding: 10;");
        textSendButton.setOnAction(e -> sendTextMessage());
        
        box.getChildren().addAll(label, inputTextArea, textSendButton);
        box.setAlignment(Pos.CENTER);
        
        return box;
    }
    
    /**
     * 创建语音输入区域
     */
    private VBox createVoiceInputBox() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 10; -fx-padding: 15;");
        
        Label label = new Label("🎤 语音输入");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        voiceRecordButton = new Button("🎤 开始录音");
        voiceRecordButton.setPrefWidth(200);
        voiceRecordButton.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-size: 14px; -fx-padding: 10;");
        voiceRecordButton.setOnAction(e -> startVoiceRecording());
        
        Button stopButton = new Button("⏹ 停止录音");
        stopButton.setPrefWidth(200);
        stopButton.setStyle("-fx-background-color: #f56565; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-size: 14px; -fx-padding: 10;");
        stopButton.setOnAction(e -> stopRecordingAndSend());
        
        buttonBox.getChildren().addAll(voiceRecordButton, stopButton);
        
        Label tipLabel = new Label("点击开始录音后说话，完成后点击停止并自动发送");
        tipLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        tipLabel.setWrapText(true);
        
        box.getChildren().addAll(label, buttonBox, tipLabel);
        box.setAlignment(Pos.CENTER);
        
        return box;
    }
    
    /**
     * 创建响应显示区域
     */
    private VBox createResponseBox() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 10; -fx-padding: 15;");
        
        Label label = new Label("💡 AI回复");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        responseArea = new TextArea();
        responseArea.setPromptText("AI的回复将显示在这里...");
        responseArea.setPrefRowCount(8);
        responseArea.setWrapText(true);
        responseArea.setEditable(false);
        responseArea.setStyle("-fx-border-radius: 5;");
        
        box.getChildren().addAll(label, responseArea);
        
        return box;
    }
    
    /**
     * 发送文字消息
     */
    private void sendTextMessage() {
        String text = inputTextArea.getText().trim();
        
        if (text.isEmpty()) {
            showError("请输入文字内容");
            return;
        }
        
        // 保存当前查询
        currentQuery = text;
        
        textSendButton.setDisable(true);
        voiceRecordButton.setDisable(true);
        updateStatus("正在处理...");
        
        new Thread(() -> {
            try {
                String response = apiClient.sendText(text);
                System.out.println("收到后端响应: " + response);
                
                javafx.application.Platform.runLater(() -> {
                    responseArea.setText(response);
                    responseArea.setVisible(true); // 显示响应气泡
                    inputTextArea.clear();
                    textSendButton.setDisable(false);
                    voiceRecordButton.setDisable(false);
                    updateStatus("✅ 完成");
                    
                    // 隐藏输入区域
                    
                    // 收到响应后才自动打开浏览器
                    if (autoOpenBrowser && response != null && !response.isEmpty()) {
                        openBrowser();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    String errorMsg = getErrorMessage(e);
                    showError(errorMsg);
                    textSendButton.setDisable(false);
                    voiceRecordButton.setDisable(false);
                    updateStatus("❌ 失败");
                });
            }
        }).start();
    }
    
    /**
     * 获取友好的错误信息
     */
    private String getErrorMessage(Exception e) {
        String message = e.getMessage();
        
        if (message == null) {
            return "请求失败，请重试";
        }
        
        // 检查超时错误
        if (message.contains("timeout") || message.contains("Timed out") || 
            message.contains("连接超时") || message.contains("read timed out")) {
            return "请求超时（2分钟），请检查网络连接或稍后重试";
        }
        
        // 检查连接错误
        if (message.contains("Connection refused") || message.contains("连接被拒绝")) {
            return "无法连接到服务器，请确认后端服务已启动";
        }
        
        // 检查网络错误
        if (message.contains("Network is unreachable") || message.contains("无网络连接")) {
            return "网络连接失败，请检查网络设置";
        }
        
        // 返回原始错误信息
        return "请求失败: " + message;
    }
    
    /**
     * 打开浏览器
     */
    private void openBrowser() {
        // 尝试从响应中提取地图数据
        if (responseArea != null && !responseArea.getText().isEmpty()) {
            MapDataExtractor.MapData mapData = MapDataExtractor.extractMapData(responseArea.getText());
            
            // 只有驾车、骑行、步行可以打开地图页面
            if (mapData != null && 
                (mapData.mode.equals("driving") || mapData.mode.equals("riding") || 
                 mapData.mode.equals("walking"))) {
                // 构建地图URL - 使用配置的地图URL，参数拼接在后面
                String urlToOpen = MapDataExtractor.buildMapUrl(mapUrl, mapData);
                System.out.println("打开地图: " + urlToOpen);
                BrowserHelper.openBrowser(urlToOpen);
                updateStatus("正在打开地图...");
                return;
            } else {
                // 没有地图数据（可能是错误），不打开浏览器
                System.out.println("无法提取地图数据，路线规划失败");
                updateStatus("⚠️ 路线规划失败");
                return;
            }
        }
        
        // 如果没有响应，不打开浏览器
        System.out.println("无响应数据，不打开浏览器");
        updateStatus("⚠️ 路线规划失败");
    }
    
    /**
     * 开始语音录音并自动处理
     * 点击按钮 → 开始录音 → 自动停止并发送 → 显示结果
     */
    private void startVoiceRecording() {
        if (isRecording.get()) {
            // 如果正在录音，则停止并发送
            stopRecordingAndSend();
            return;
        }
        
        // 开始录音
        try {
            audioRecorder.startRecording();
            isRecording.set(true);
            voiceRecordButton.setText("⏹️ 停止录音");
            voiceRecordButton.setStyle("-fx-background-color: #f56565; -fx-text-fill: white; -fx-background-radius: 15;");
            updateStatus("🎤 正在录音...点击停止按钮结束");
            
        } catch (Exception e) {
            showError("录音启动失败: " + e.getMessage());
        }
    }
    
    /**
     * 停止录音并发送
     */
    private void stopRecordingAndSend() {
        if (!isRecording.get()) {
            return;
        }
        
        // 停止录音
        try {
            audioRecorder.stopRecording();
            isRecording.set(false);
            voiceRecordButton.setText("🎤 说话");
            voiceRecordButton.setStyle("-fx-background-color: #48bb78; -fx-text-fill: white; -fx-background-radius: 15;");
        } catch (Exception e) {
            System.err.println("录音停止失败: " + e.getMessage());
        }
        
        updateStatus("正在处理...");
        textSendButton.setDisable(true);
        voiceRecordButton.setDisable(true);
        
        new Thread(() -> {
            try {
                String response = apiClient.sendAudio(audioRecorder.getRecordedFile());
                System.out.println("收到后端响应: " + response);
                
                javafx.application.Platform.runLater(() -> {
                    responseArea.setText(response);
                    responseArea.setVisible(true); // 显示响应气泡
                    updateStatus("✅ 处理完成");
                    textSendButton.setDisable(false);
                    voiceRecordButton.setDisable(false);
                    
                    // 收到响应后才自动打开浏览器
                    if (autoOpenBrowser && response != null && !response.isEmpty()) {
                        openBrowser();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    String errorMsg = getErrorMessage(e);
                    showError(errorMsg);
                    updateStatus("❌ 处理失败");
                    textSendButton.setDisable(false);
                    voiceRecordButton.setDisable(false);
                });
            }
        }).start();
    }
    
    /**
     * 更新状态标签
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    /**
     * 显示错误消息
     */
    private void showError(String message) {
        updateStatus("错误: " + message);
        if (statusLabel != null) {
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff0000; -fx-alignment: center;");
        }
        
        // 3秒后恢复正常颜色
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-alignment: center;");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * 获取根节点
     */
    public VBox getRoot() {
        return root;
    }
}

