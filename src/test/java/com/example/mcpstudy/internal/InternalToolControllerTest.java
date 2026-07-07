package com.example.mcpstudy.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mcpstudy.tool.McpToolExecutor;
import com.example.mcpstudy.tool.McpToolResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalToolController.class)
class InternalToolControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private McpToolExecutor toolExecutor;

	@Test
	void callToolReturnsExecutorResponse() throws Exception {
		when(toolExecutor.execute(any())).thenReturn(McpToolResponse.success("ok"));

		mockMvc.perform(post("/internal/tools/call")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "toolName": "find_user_by_id",
					  "arguments": {
					    "userId": 1
					  }
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.result").value("ok"));
	}

	@Test
	void callToolReturnsErrorWhenToolNameIsMissing() throws Exception {
		mockMvc.perform(post("/internal/tools/call")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "arguments": {
					    "userId": 1
					  }
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.errorMessage").value("toolName is required"));
	}
}
