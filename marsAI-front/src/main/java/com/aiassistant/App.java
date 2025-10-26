package com.aiassistant;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * AI桌面助手主程序
 * 支持语音和文字输入，与后端AI服务交互
 */
public class App extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        AssistantController controller = new AssistantController();
        Scene scene = new Scene(controller.getRoot(), 600, 500);
        
        primaryStage.setTitle("AI桌面助手");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        
        // 设置窗口关闭事件
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("用户关闭窗口");
        });
        
        primaryStage.show();
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
}

