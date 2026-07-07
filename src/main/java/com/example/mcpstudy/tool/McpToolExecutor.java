package com.example.mcpstudy.tool;

import org.springframework.stereotype.Component;

@Component
public class McpToolExecutor {

	private final McpToolRegistry toolRegistry;

	public McpToolExecutor(McpToolRegistry toolRegistry) {
		this.toolRegistry = toolRegistry;
	}

	public McpToolResponse execute(McpToolRequest request) {
		if (request == null || request.toolName() == null || request.toolName().isBlank()) {
			return McpToolResponse.error("toolName is required");
		}

		try {
			McpTool tool = toolRegistry.findByName(request.toolName());
			return tool.execute(request);
		}
		catch (McpToolNotFoundException | IllegalArgumentException e) {
			return McpToolResponse.error(e.getMessage());
		}
		catch (RuntimeException e) {
			return McpToolResponse.error("MCP tool execution failed. toolName=" + request.toolName()
				+ ", message=" + e.getMessage());
		}
	}
}
