package com.qiniu.marsai.service;

import com.qiniu.marsai.mcp.McpConfig;
import com.qiniu.marsai.tool.PublicIpUtil;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiHelperServiceFactory {

    @Autowired
    private ChatModel deepSeekChatModel;

    @Autowired
    private McpConfig mapMcpConfig;



    @Bean
    public AiHelperService aiHelperService(){


        return AiServices.builder(AiHelperService.class)
                .chatModel(deepSeekChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(1000))
                .toolProvider(mapMcpConfig.mcpToolProvider())
                .tools(new PublicIpUtil())
                .build();
    }
}
