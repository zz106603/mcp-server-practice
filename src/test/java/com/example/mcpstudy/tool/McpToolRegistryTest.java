package com.example.mcpstudy.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class McpToolRegistryTest {

	@Test
	void findByNameReturnsRegisteredTool() {
		McpTool tool = new TestTool("hello");
		McpToolRegistry registry = new McpToolRegistry(List.of(tool));

		McpTool foundTool = registry.findByName("hello");

		assertThat(foundTool).isSameAs(tool);
	}

	@Test
	void constructorThrowsExceptionWhenToolNameIsDuplicated() {
		McpTool firstTool = new TestTool("duplicated");
		McpTool secondTool = new TestTool("duplicated");

		assertThatThrownBy(() -> new McpToolRegistry(List.of(firstTool, secondTool)))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Duplicated MCP tool name. name=duplicated");
	}

	@Test
	void findByNameThrowsExceptionWhenToolDoesNotExist() {
		McpToolRegistry registry = new McpToolRegistry(List.of());

		assertThatThrownBy(() -> registry.findByName("missing"))
			.isInstanceOf(McpToolNotFoundException.class)
			.hasMessage("MCP tool not found. name=missing");
	}

	@Test
	void findAllReturnsRegisteredTools() {
		McpTool firstTool = new TestTool("first");
		McpTool secondTool = new TestTool("second");
		McpToolRegistry registry = new McpToolRegistry(List.of(firstTool, secondTool));

		assertThat(registry.findAll()).containsExactly(firstTool, secondTool);
	}

	private record TestTool(String name) implements McpTool {

		@Override
		public String description() {
			return "test tool";
		}

		@Override
		public Map<String, Object> inputSchema() {
			return Map.of("type", "object");
		}

		@Override
		public McpToolResponse execute(McpToolRequest request) {
			return McpToolResponse.success(request);
		}
	}
}
