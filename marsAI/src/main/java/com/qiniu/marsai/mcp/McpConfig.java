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

    @Value("${amap.maps-api-key:9561f121929a1b9d992150ad8746f16b}")
    private String amapMapsApiKey;

    @Value("${baidu-map.ak:M2bHjBtPzNYIPLipc28ZRK1TZObg7k3U}")
    private String baiduMapApiKey;

    @Value("${mcp.gaode.enabled:true}")
    private boolean gaodeEnabled;

    @Value("${mcp.baidu.enabled:false}")
    private boolean baiduEnabled;

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

            // 百度地图MCP
            if (baiduEnabled) {
                McpTransport transportForBaidu = new StdioMcpTransport.Builder()
                        .command(List.of("cmd","/c", "npx","-y", "@baidumap/mcp-server-baidu-map"))
                        .environment(Map.of("BAIDU_MAP_API_KEY", baiduMapApiKey))
                        .logEvents(true)
                        .build();

                McpClient mcpClientForBaidu = new DefaultMcpClient.Builder()
                        .key(baiduMapApiKey)
                        .transport(transportForBaidu)
                        .build();

                clients.add(mcpClientForBaidu);
                System.out.println("百度地图MCP客户端创建成功");
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
