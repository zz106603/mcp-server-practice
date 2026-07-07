package com.example.mcpstudy.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record McpJsonRpcResponse(String jsonrpc, Object id, Object result, McpJsonRpcError error) {

	private static final String JSON_RPC_VERSION = "2.0";

	public static McpJsonRpcResponse result(Object id, Object result) {
		return new McpJsonRpcResponse(JSON_RPC_VERSION, id, result, null);
	}

	public static McpJsonRpcResponse error(Object id, int code, String message) {
		return new McpJsonRpcResponse(JSON_RPC_VERSION, id, null, new McpJsonRpcError(code, message));
	}
}
