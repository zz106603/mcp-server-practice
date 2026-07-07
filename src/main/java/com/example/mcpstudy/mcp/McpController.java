package com.example.mcpstudy.mcp;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class McpController {

	private final McpProtocolHandler protocolHandler;

	public McpController(McpProtocolHandler protocolHandler) {
		this.protocolHandler = protocolHandler;
	}

	@PostMapping("/mcp")
	public ResponseEntity<McpJsonRpcResponse> handle(@RequestBody(required = false) McpJsonRpcRequest request) {
		return protocolHandler.handle(request)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.accepted().build());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public McpJsonRpcResponse handleUnreadableMessage(HttpMessageNotReadableException e) {
		return McpJsonRpcResponse.error(null, -32700, "Parse error");
	}
}
