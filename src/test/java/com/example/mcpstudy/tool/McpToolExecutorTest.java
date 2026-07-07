package com.example.mcpstudy.tool;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class McpToolExecutorTest {

	@Test
	void executeFindsToolByNameAndRunsIt() {
		McpToolExecutor executor = new McpToolExecutor(new McpToolRegistry(List.of(new EchoTool())));

		McpToolResponse response = executor.execute(new McpToolRequest("echo", Map.of("message", "hello")));

		assertThat(response.success()).isTrue();
		assertThat(response.result()).isEqualTo("hello");
	}

	@Test
	void executeReturnsErrorWhenToolDoesNotExist() {
		McpToolExecutor executor = new McpToolExecutor(new McpToolRegistry(List.of()));

		McpToolResponse response = executor.execute(new McpToolRequest("missing", Map.of()));

		assertThat(response.success()).isFalse();
		assertThat(response.errorMessage()).isEqualTo("MCP tool not found. name=missing");
	}

	@Test
	void executeRequiresToolName() {
		McpToolExecutor executor = new McpToolExecutor(new McpToolRegistry(List.of()));

		McpToolResponse response = executor.execute(new McpToolRequest(null, Map.of()));

		assertThat(response.success()).isFalse();
		assertThat(response.errorMessage()).isEqualTo("toolName is required");
	}

	@Test
	void executeReturnsErrorWhenToolThrowsIllegalArgumentException() {
		McpToolExecutor executor = new McpToolExecutor(new McpToolRegistry(List.of(new InvalidInputTool())));

		McpToolResponse response = executor.execute(new McpToolRequest("invalid_input", Map.of()));

		assertThat(response.success()).isFalse();
		assertThat(response.errorMessage()).isEqualTo("message is required");
	}

	@Test
	void executeReturnsErrorWhenToolThrowsUnexpectedException() {
		McpToolExecutor executor = new McpToolExecutor(new McpToolRegistry(List.of(new BrokenTool())));

		McpToolResponse response = executor.execute(new McpToolRequest("broken", Map.of()));

		assertThat(response.success()).isFalse();
		assertThat(response.errorMessage()).isEqualTo("MCP tool execution failed. toolName=broken, message=boom");
	}

	private static class EchoTool implements McpTool {

		@Override
		public String name() {
			return "echo";
		}

		@Override
		public String description() {
			return "Echo a message.";
		}

		@Override
		public Map<String, Object> inputSchema() {
			return Map.of("type", "object");
		}

		@Override
		public McpToolResponse execute(McpToolRequest request) {
			return McpToolResponse.success(request.arguments().get("message"));
		}
	}

	private static class InvalidInputTool implements McpTool {

		@Override
		public String name() {
			return "invalid_input";
		}

		@Override
		public String description() {
			return "Invalid input test tool.";
		}

		@Override
		public Map<String, Object> inputSchema() {
			return Map.of("type", "object");
		}

		@Override
		public McpToolResponse execute(McpToolRequest request) {
			throw new IllegalArgumentException("message is required");
		}
	}

	private static class BrokenTool implements McpTool {

		@Override
		public String name() {
			return "broken";
		}

		@Override
		public String description() {
			return "Broken test tool.";
		}

		@Override
		public Map<String, Object> inputSchema() {
			return Map.of("type", "object");
		}

		@Override
		public McpToolResponse execute(McpToolRequest request) {
			throw new IllegalStateException("boom");
		}
	}
}
