package com.example.mcpstudy.internal;

import com.example.mcpstudy.tool.McpToolExecutor;
import com.example.mcpstudy.tool.McpToolRequest;
import com.example.mcpstudy.tool.McpToolResponse;

import jakarta.validation.Valid;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/tools")
public class InternalToolController {

	private final McpToolExecutor toolExecutor;

	public InternalToolController(McpToolExecutor toolExecutor) {
		this.toolExecutor = toolExecutor;
	}

	// Internal verification endpoint before implementing the official MCP JSON-RPC protocol.
	@PostMapping("/call")
	public McpToolResponse callTool(@Valid @RequestBody McpToolRequest request, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return McpToolResponse.error(bindingResult.getFieldError().getDefaultMessage());
		}

		return toolExecutor.execute(request);
	}
}
