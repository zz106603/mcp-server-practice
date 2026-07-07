package com.example.mcpstudy.mcp;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.example.mcpstudy.tool.McpTool;
import com.example.mcpstudy.tool.McpToolExecutor;
import com.example.mcpstudy.tool.McpToolResponse;
import com.example.mcpstudy.tool.McpToolRegistry;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(McpController.class)
class McpControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private McpToolRegistry toolRegistry;

	@MockitoBean
	private McpToolExecutor toolExecutor;

	@Test
	void initializesServer() throws Exception {
		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "2.0",
					  "id": 1,
					  "method": "initialize",
					  "params": {}
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.error").doesNotExist())
			.andExpect(jsonPath("$.result.protocolVersion").value("2025-06-18"))
			.andExpect(jsonPath("$.result.serverInfo.name").value("spring-mcp-study-server"))
			.andExpect(jsonPath("$.result.serverInfo.version").value("0.1.0"))
			.andExpect(jsonPath("$.result.capabilities.tools").exists());
	}

	@Test
	void handlesInitializedNotificationWithoutId() throws Exception {
		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "2.0",
					  "method": "notifications/initialized",
					  "params": {}
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").doesNotExist())
			.andExpect(jsonPath("$.error").doesNotExist())
			.andExpect(jsonPath("$.result").exists());
	}

	@Test
	void returnsToolsList() throws Exception {
		when(toolRegistry.findAll()).thenReturn(List.of(new TestTool()));

		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "2.0",
					  "id": 1,
					  "method": "tools/list",
					  "params": {}
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.error").doesNotExist())
			.andExpect(jsonPath("$.result.tools[0].name").value("find_user_by_id"))
			.andExpect(jsonPath("$.result.tools[0].description").value("Find a user by database id."))
			.andExpect(jsonPath("$.result.tools[0].inputSchema.type").value("object"))
			.andExpect(jsonPath("$.result.tools[0].inputSchema.properties.userId.type").value("integer"))
			.andExpect(jsonPath("$.result.tools[0].inputSchema.properties.userId.description").value("User id"))
			.andExpect(jsonPath("$.result.tools[0].inputSchema.required[0]").value("userId"));
	}

	@Test
	void callsToolAndReturnsToolResponseAsResult() throws Exception {
		when(toolExecutor.execute(org.mockito.ArgumentMatchers.any()))
			.thenReturn(McpToolResponse.success(Map.of("id", 1L, "username", "alice")));

		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "2.0",
					  "id": 2,
					  "method": "tools/call",
					  "params": {
					    "name": "find_user_by_id",
					    "arguments": {
					      "userId": 1
					    }
					  }
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").value(2))
			.andExpect(jsonPath("$.error").doesNotExist())
			.andExpect(jsonPath("$.result.success").value(true))
			.andExpect(jsonPath("$.result.result.id").value(1))
			.andExpect(jsonPath("$.result.result.username").value("alice"))
			.andExpect(jsonPath("$.result.errorMessage").doesNotExist());
	}

	@Test
	void returnsToolFailureInsideResult() throws Exception {
		when(toolExecutor.execute(org.mockito.ArgumentMatchers.any()))
			.thenReturn(McpToolResponse.error("MCP tool not found. name=missing"));

		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "2.0",
					  "id": 2,
					  "method": "tools/call",
					  "params": {
					    "name": "missing",
					    "arguments": {}
					  }
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").value(2))
			.andExpect(jsonPath("$.error").doesNotExist())
			.andExpect(jsonPath("$.result.success").value(false))
			.andExpect(jsonPath("$.result.result").doesNotExist())
			.andExpect(jsonPath("$.result.errorMessage").value("MCP tool not found. name=missing"));
	}

	@Test
	void toolsCallRequiresNameInsideResult() throws Exception {
		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "2.0",
					  "id": 2,
					  "method": "tools/call",
					  "params": {
					    "arguments": {}
					  }
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").value(2))
			.andExpect(jsonPath("$.error").doesNotExist())
			.andExpect(jsonPath("$.result.success").value(false))
			.andExpect(jsonPath("$.result.errorMessage").value("name is required"));
	}

	@Test
	void toolsCallRequiresArgumentsObjectInsideResult() throws Exception {
		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "2.0",
					  "id": 2,
					  "method": "tools/call",
					  "params": {
					    "name": "find_user_by_id",
					    "arguments": "invalid"
					  }
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").value(2))
			.andExpect(jsonPath("$.error").doesNotExist())
			.andExpect(jsonPath("$.result.success").value(false))
			.andExpect(jsonPath("$.result.errorMessage").value("arguments must be an object"));
	}

	@Test
	void returnsMethodNotFoundForUnsupportedMethod() throws Exception {
		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "2.0",
					  "id": 1,
					  "method": "unknown",
					  "params": {}
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").value(1))
			.andExpect(jsonPath("$.result").doesNotExist())
			.andExpect(jsonPath("$.error.code").value(-32601))
			.andExpect(jsonPath("$.error.message").value("Method not found. method=unknown"));
	}

	@Test
	void allowsOnlyJsonRpcVersion20() throws Exception {
		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "1.0",
					  "id": "abc",
					  "method": "tools/list"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").value("abc"))
			.andExpect(jsonPath("$.error.code").value(-32600))
			.andExpect(jsonPath("$.error.message").value("jsonrpc must be 2.0"));
	}

	@Test
	void requiresMethod() throws Exception {
		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "jsonrpc": "2.0",
					  "id": 1
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.error.code").value(-32600))
			.andExpect(jsonPath("$.error.message").value("method is required"));
	}

	@Test
	void returnsParseErrorForMalformedJson() throws Exception {
		mockMvc.perform(post("/mcp")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.jsonrpc").value("2.0"))
			.andExpect(jsonPath("$.id").doesNotExist())
			.andExpect(jsonPath("$.error.code").value(-32700))
			.andExpect(jsonPath("$.error.message").value("Parse error"));
	}

	private static class TestTool implements McpTool {

		@Override
		public String name() {
			return "find_user_by_id";
		}

		@Override
		public String description() {
			return "Find a user by database id.";
		}

		@Override
		public Map<String, Object> inputSchema() {
			return Map.of(
				"type", "object",
				"properties", Map.of(
					"userId", Map.of(
						"type", "integer",
						"description", "User id"
					)
				),
				"required", List.of("userId")
			);
		}

		@Override
		public com.example.mcpstudy.tool.McpToolResponse execute(com.example.mcpstudy.tool.McpToolRequest request) {
			return com.example.mcpstudy.tool.McpToolResponse.success(null);
		}
	}
}
