# MarsAI - 智能线路规划系统

一个基于大模型和 MCP（Model Context Protocol）联动的智能线路规划系统，支持文本和语音输入，提供精准的线路规划与网页展示功能。

## 核心功能

使用大模型跟 MCP 的联动，完成对文本线路规划输入跟语音线路规划输入需求的精准识别，最终打开网页展示线路。

## 特色功能

1. **目的地与出发地天气信息展示** - 实时显示出发地和目的地的天气情况，帮助用户规划行程
2. **线路上导航信息展示** - 详细的路线导航信息，包括途径地点、距离等
3. **自动选择出行方式** - 系统自动选择最优出行方式（开车、骑行、步行）
4. **智能线路推荐** - 结合用户提示、距离、耗时、天气等信息，给出多条最优线路
5. **多语言语音支持** - 支持中文、英文、202种方言，无需切换

## 项目结构

```
MarsAI/
├── marsAI/                      # 后端项目（Spring Boot）
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/qiniu/marsai/
│   │   │   │       ├── controller/    # 控制器
│   │   │   │       ├── service/       # 服务层
│   │   │   │       ├── mcp/          # MCP 配置
│   │   │   │       └── tool/         # 工具类
│   │   │   └── resources/
│   │   │       ├── application.yml   # 配置文件
│   │   │       └── static/           # 静态资源
│   │   └── test/                     # 测试代码
│   └── pom.xml
│
└── marsAI-front/                 # 前端项目
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   │   └── com/aiassistant/
    │   │   │       ├── App.java                     # 应用程序入口
    │   │   │       ├── ApiClient.java               # API 客户端
    │   │   │       ├── AssistantController.java     # 助手控制器
    │   │   │       ├── AudioRecorder.java           # 音频录制
    │   │   │       ├── BrowserHelper.java           # 浏览器辅助
    │   │   │       └── MapDataExtractor.java        # 地图数据提取
    │   │   └── resources/
    │   │       └── config.properties                # 配置文件
    │   └── pom.xml
    └── run.bat                    # 启动脚本
```

## 密钥填充指引

### 后端配置 (marsAI/src/main/resources/application.yml)

项目已内置默认配置，也可通过环境变量覆盖。配置文件已包含以下密钥配置：

```yaml
# LangChain4j 配置
langchain4j:
  community:
    dashscope:
      chat-model:
        model-name: qwen-max
        api-key: ${DASHSCOPE_API_KEY:YOUR_API_KEY_HERE}

# AI 配置
ai:
  api-key: ${DASHSCOPE_API_KEY:YOUR_API_KEY_HERE}
  api-url: ${AI_API_URL:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}

# 高德地图配置
amap:
  maps-api-key: ${AMAP_MAPS_API_KEY:YOUR_AMAP_MAPS_API_KEY}
  web-api-key: ${AMAP_WEB_API_KEY:YOUR_AMAP_WEB_API_KEY}
  security-js-code: ${AMAP_SECURITY_JS_CODE:YOUR_AMAP_SECURITY_JS_CODE}

# MCP 配置
mcp:
  gaode:
    enabled: ${MCP_GAODE_ENABLED:true}

# 讯飞语音识别配置
asr:
  xfyun:
    app-id: ${XFYUN_APP_ID:YOUR_XFYUN_APP_ID}
    access-key-id: ${XFYUN_ACCESS_KEY_ID:YOUR_XFYUN_ACCESS_KEY_ID}
    access-key-secret: ${XFYUN_ACCESS_KEY_SECRET:YOUR_XFYUN_ACCESS_KEY_SECRET}
```

#### 环境变量配置（可选）

如需自定义配置，可通过设置以下环境变量：

- `DASHSCOPE_API_KEY`: 阿里云 DashScope API 密钥
- `AMAP_MAPS_API_KEY`: 高德地图 MCP 密钥
- `AMAP_WEB_API_KEY`: 高德地图 Web API 密钥
- `AMAP_SECURITY_JS_CODE`: 高德地图安全密钥
- `MCP_GAODE_ENABLED`: 是否启用高德地图 MCP（默认：true）
- `XFYUN_APP_ID`: 讯飞语音识别 App ID
- `XFYUN_ACCESS_KEY_ID`: 讯飞语音识别 Access Key ID
- `XFYUN_ACCESS_KEY_SECRET`: 讯飞语音识别 Access Key Secret

## 项目启动方式

### 环境要求

- JDK 17 或更高版本
- Maven 3.6+
- Chrome 浏览器（用于地图展示）

### 后端启动

1. 进入后端项目目录：
```bash
cd marsAI
```

2. 安装依赖并启动：
```bash
mvn clean install
mvn spring-boot:run
```

或者使用 Maven Wrapper：
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

后端将在 `http://localhost:8081` 启动

### 前端启动

前端是一个 Java 桌面应用程序，需要在 Java 环境中运行。

1. 进入前端项目目录：
```bash
cd marsAI-front
```

2. 直接运行启动脚本：
```bash
# Windows
run.bat
```

或者手动编译运行：
```bash
mvn clean package
java -jar target/classes
```

启动后将打开桌面应用窗口。

## 使用说明

1. **启动后端服务**：首先启动 Spring Boot 后端服务（运行在 http://localhost:8081）
2. **启动前端桌面应用**：运行前端桌面应用程序
3. **文本输入**：在前端界面输入出发地和目的地，系统将自动规划线路
4. **语音输入**：点击语音输入按钮，支持中英文及多种方言
5. **查看结果**：系统将自动打开网页，展示详细的线路规划和相关信息

## 技术栈

- **后端**：Spring Boot, Maven
- **前端**：Java, JavaFX（地图展示）
- **AI 服务**：大模型 API
- **地图服务**：高德地图 API
- **语音识别**：讯飞 ASR
- **协议**：MCP (Model Context Protocol)

## 演示视频

🎥 观看演示视频：http://t4qpqle3i.hd-bkt.clouddn.com/video.mp4

## 贡献

欢迎提交 Issue 和 Pull Request！
