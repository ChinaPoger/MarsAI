package com.qiniu.marsai.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class McpConfig {



        @Bean
        public McpToolProvider mcpToolProvider() {
            try {
                /*// 百度地图MCP
                McpTransport transportForBaidu = new StdioMcpTransport.Builder()
                        .command(List.of("cmd","/c", "npx","-y", "@baidumap/mcp-server-baidu-map"))
                        .environment(Map.of("BAIDU_MAP_API_KEY","M2bHjBtPzNYIPLipc28ZRK1TZObg7k3U"))
                        .logEvents(true)
                        .build();*/
        
                /*McpClient mcpClientForBaidu = new DefaultMcpClient.Builder()
                        .key("M2bHjBtPzNYIPLipc28ZRK1TZObg7k3U")
                        .transport(transportForBaidu)  // ✅ 修复这里
                        .build();*/
        
                // 高德地图MCP
                McpTransport transportForGaode = new StdioMcpTransport.Builder()
                        .command(List.of("cmd","/c", "npx","-y", "@amap/amap-maps-mcp-server"))
                        .environment(Map.of("AMAP_MAPS_API_KEY","9561f121929a1b9d992150ad8746f16b"))
                        .logEvents(true)
                        .build();

                McpClient mcpClientForGaode = new DefaultMcpClient.Builder()
                        .key("9561f121929a1b9d992150ad8746f16b")
                        .transport(transportForGaode)  // ✅ 修复这里
                        .build();
        
                McpToolProvider toolProvider = McpToolProvider.builder()
                        .mcpClients( mcpClientForGaode)
                        .build();
                
                System.out.println("MCP工具提供者创建成功");
                return toolProvider;
            } catch (Exception e) {
                System.err.println("创建MCP工具提供者失败: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
}
