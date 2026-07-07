package com.example.mcpstudy.tool;

public record McpToolResponse(boolean success, Object result, String errorMessage) {

	public static McpToolResponse success(Object result) {
		return new McpToolResponse(true, result, null);
	}

	public static McpToolResponse error(String errorMessage) {
		return new McpToolResponse(false, null, errorMessage);
	}
}
