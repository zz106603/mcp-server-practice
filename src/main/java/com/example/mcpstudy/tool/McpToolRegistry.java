package com.example.mcpstudy.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class McpToolRegistry {

	private final Map<String, McpTool> tools;

	public McpToolRegistry(List<McpTool> tools) {
		Map<String, McpTool> registeredTools = new LinkedHashMap<>();

		for (McpTool tool : tools) {
			McpTool duplicatedTool = registeredTools.putIfAbsent(tool.name(), tool);
			if (duplicatedTool != null) {
				throw new IllegalStateException("Duplicated MCP tool name. name=" + tool.name());
			}
		}

		this.tools = Collections.unmodifiableMap(new LinkedHashMap<>(registeredTools));
	}

	public McpTool findByName(String name) {
		McpTool tool = tools.get(name);
		if (tool == null) {
			throw new McpToolNotFoundException(name);
		}
		return tool;
	}

	public List<McpTool> findAll() {
		return List.copyOf(new ArrayList<>(tools.values()));
	}
}
