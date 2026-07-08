package com.example.mcpstudy.mcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Component;

@Component
public class StdioMcpServerRunner {

	private final ObjectMapper objectMapper;
	private final McpProtocolHandler protocolHandler;

	public StdioMcpServerRunner(ObjectMapper objectMapper, McpProtocolHandler protocolHandler) {
		this.objectMapper = objectMapper;
		this.protocolHandler = protocolHandler;
	}

	public void run() {
		run(System.in, System.out, System.err);
	}

	void run(InputStream inputStream, OutputStream outputStream, PrintStream errorStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				handleLine(line, writer, errorStream);
			}
		}
		catch (IOException e) {
			errorStream.println("Stdio MCP server I/O failure: " + e.getMessage());
			e.printStackTrace(errorStream);
		}
	}

	private void handleLine(String line, BufferedWriter writer, PrintStream errorStream) throws IOException {
		if (line.isBlank()) {
			return;
		}

		McpJsonRpcRequest request = null;
		try {
			request = objectMapper.readValue(line, McpJsonRpcRequest.class);
			Optional<McpJsonRpcResponse> response = protocolHandler.handle(request);
			if (response.isPresent()) {
				writeResponse(response.get(), writer);
			}
		}
		catch (JsonProcessingException e) {
			writeResponse(McpJsonRpcResponse.error(null, -32700, "Parse error"), writer);
		}
		catch (RuntimeException e) {
			errorStream.println("Stdio MCP request handling failure: " + e.getMessage());
			e.printStackTrace(errorStream);
			Object id = request == null ? null : request.id();
			writeResponse(McpJsonRpcResponse.error(id, -32603, "Internal error"), writer);
		}
	}

	private void writeResponse(McpJsonRpcResponse response, BufferedWriter writer) throws IOException {
		writer.write(objectMapper.writeValueAsString(response));
		writer.write('\n');
		writer.flush();
	}
}
