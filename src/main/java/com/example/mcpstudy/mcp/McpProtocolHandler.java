package com.example.mcpstudy.mcp;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.mcpstudy.tool.McpToolExecutor;
import com.example.mcpstudy.tool.McpToolRequest;
import com.example.mcpstudy.tool.McpToolResponse;
import com.example.mcpstudy.tool.McpToolRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

@Component
public class McpProtocolHandler {

	private static final String JSON_RPC_VERSION = "2.0";
	private static final String MCP_PROTOCOL_VERSION = "2025-06-18";
	private static final String INITIALIZE_METHOD = "initialize";
	private static final String INITIALIZED_NOTIFICATION_METHOD = "notifications/initialized";
	private static final String TOOLS_LIST_METHOD = "tools/list";
	private static final String TOOLS_CALL_METHOD = "tools/call";

	private final McpToolRegistry toolRegistry;
	private final McpToolExecutor toolExecutor;
	private final ObjectMapper objectMapper;

	public McpProtocolHandler(McpToolRegistry toolRegistry, McpToolExecutor toolExecutor, ObjectMapper objectMapper) {
		this.toolRegistry = toolRegistry;
		this.toolExecutor = toolExecutor;
		this.objectMapper = objectMapper;
	}

	public Optional<McpJsonRpcResponse> handle(McpJsonRpcRequest request) {
		if (request == null) {
			return Optional.of(McpJsonRpcResponse.error(null, -32600, "Invalid Request"));
		}

		if (!JSON_RPC_VERSION.equals(request.jsonrpc())) {
			return Optional.of(McpJsonRpcResponse.error(request.id(), -32600, "jsonrpc must be 2.0"));
		}

		if (request.method() == null || request.method().isBlank()) {
			return Optional.of(McpJsonRpcResponse.error(request.id(), -32600, "method is required"));
		}

		if (INITIALIZE_METHOD.equals(request.method())) {
			return Optional.of(McpJsonRpcResponse.result(request.id(), initializeResult()));
		}

		if (INITIALIZED_NOTIFICATION_METHOD.equals(request.method())) {
			return Optional.empty();
		}

		if (TOOLS_LIST_METHOD.equals(request.method())) {
			return Optional.of(McpJsonRpcResponse.result(request.id(), toolsListResult()));
		}

		if (TOOLS_CALL_METHOD.equals(request.method())) {
			return Optional.of(McpJsonRpcResponse.result(request.id(), toToolCallResult(callTool(request.params()))));
		}

		return Optional.of(McpJsonRpcResponse.error(request.id(), -32601,
			"Method not found. method=" + request.method()));
	}

	private InitializeResult initializeResult() {
		return new InitializeResult(
			MCP_PROTOCOL_VERSION,
			new ServerInfo("spring-mcp-study-server", "0.1.0"),
			new Capabilities(new ToolsCapability())
		);
	}

	private ToolsListResult toolsListResult() {
		List<ToolDefinition> tools = toolRegistry.findAll()
			.stream()
			.map(tool -> new ToolDefinition(tool.name(), tool.description(), tool.inputSchema()))
			.toList();

		return new ToolsListResult(tools);
	}

	private McpToolResponse callTool(Map<String, Object> params) {
		Object name = params.get("name");
		if (!(name instanceof String toolName) || toolName.isBlank()) {
			return McpToolResponse.error("name is required");
		}

		Object arguments = params.get("arguments");
		if (arguments == null) {
			return toolExecutor.execute(new McpToolRequest(toolName, Map.of()));
		}

		if (!(arguments instanceof Map<?, ?> argumentsMap)) {
			return McpToolResponse.error("arguments must be an object");
		}

		return toolExecutor.execute(new McpToolRequest(toolName, toStringObjectMap(argumentsMap)));
	}

	private Map<String, Object> toStringObjectMap(Map<?, ?> map) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			result.put(String.valueOf(entry.getKey()), entry.getValue());
		}
		return result;
	}

	private ToolCallResult toToolCallResult(McpToolResponse response) {
		if (response.success()) {
			return new ToolCallResult(List.of(new TextContent("text", toText(response.result()))), false);
		}
		return new ToolCallResult(List.of(new TextContent("text", response.errorMessage())), true);
	}

	private String toText(Object value) {
		if (value == null) {
			return "";
		}

		if (value instanceof String text) {
			return text;
		}

		try {
			return objectMapper.writeValueAsString(value);
		}
		catch (JsonProcessingException e) {
			return String.valueOf(value);
		}
	}

	private record InitializeResult(String protocolVersion, ServerInfo serverInfo, Capabilities capabilities) {
	}

	private record ServerInfo(String name, String version) {
	}

	private record Capabilities(ToolsCapability tools) {
	}

	private record ToolsCapability() {
	}

	private record ToolsListResult(List<ToolDefinition> tools) {
	}

	private record ToolDefinition(String name, String description, Object inputSchema) {
	}

	private record ToolCallResult(List<TextContent> content, boolean isError) {
	}

	private record TextContent(String type, String text) {
	}
}
