package com.example.mcpstudy.mcp;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record McpJsonRpcRequest(String jsonrpc, Object id, String method, Map<String, Object> params) {

	public McpJsonRpcRequest {
		params = params == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(params));
	}
}
