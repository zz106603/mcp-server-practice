package com.example.mcpstudy.tool;

import java.util.Map;

public interface McpTool {

	String name();

	String description();

	Map<String, Object> inputSchema();

	McpToolResponse execute(McpToolRequest request);
}
