package com.example.mcpstudy.tool;

public class McpToolNotFoundException extends RuntimeException {

	public McpToolNotFoundException(String name) {
		super("MCP tool not found. name=" + name);
	}
}
