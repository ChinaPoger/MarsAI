package com.qiniu.marsai.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class McpConfig {

    @Value("${amap.maps-api-key}")
    private String amapMapsApiKey;

    @Value("${mcp.gaode.enabled:true}")
    private boolean gaodeEnabled;

    @Bean
    public McpToolProvider mcpToolProvider() {
        try {
            List<McpClient> clients = new ArrayList<>();

            // 高德地图MCP
            if (gaodeEnabled) {
                McpTransport transportForGaode = new StdioMcpTransport.Builder()
                        .command(List.of("cmd","/c", "npx","-y", "@amap/amap-maps-mcp-server"))
                        .environment(Map.of("AMAP_MAPS_API_KEY", amapMapsApiKey))
                        .logEvents(true)
                        .build();

                McpClient mcpClientForGaode = new DefaultMcpClient.Builder()
                        .key(amapMapsApiKey)
                        .transport(transportForGaode)
                        .build();

                clients.add(mcpClientForGaode);
                System.out.println("高德地图MCP客户端创建成功");
            }

            McpToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(clients)
                    .build();
            
            System.out.println("MCP工具提供者创建成功，已启用 " + clients.size() + " 个地图服务");
            return toolProvider;
        } catch (Exception e) {
            System.err.println("创建MCP工具提供者失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
