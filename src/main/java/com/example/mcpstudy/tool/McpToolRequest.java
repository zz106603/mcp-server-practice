package com.example.mcpstudy.tool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;

public record McpToolRequest(@NotBlank(message = "toolName is required") String toolName,
		Map<String, Object> arguments) {

	public McpToolRequest {
		arguments = arguments == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(arguments));
	}
}
