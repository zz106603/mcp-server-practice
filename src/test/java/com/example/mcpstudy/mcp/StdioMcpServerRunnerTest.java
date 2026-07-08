package com.example.mcpstudy.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

class StdioMcpServerRunnerTest {

	private final McpProtocolHandler protocolHandler = org.mockito.Mockito.mock(McpProtocolHandler.class);
	private final StdioMcpServerRunner runner = new StdioMcpServerRunner(new ObjectMapper(), protocolHandler);

	@Test
	void writesJsonRpcResponseToStdout() {
		when(protocolHandler.handle(any()))
			.thenReturn(Optional.of(McpJsonRpcResponse.result(1, Map.of("ok", true))));

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		runner.run(input("""
			{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}
			"""), stdout, stderr());

		assertThat(stdout.toString(StandardCharsets.UTF_8))
			.isEqualTo("""
				{"jsonrpc":"2.0","id":1,"result":{"ok":true}}
				""");
	}

	@Test
	void doesNotWriteAnythingForNotificationWithoutResponse() {
		when(protocolHandler.handle(any())).thenReturn(Optional.empty());

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		runner.run(input("""
			{"jsonrpc":"2.0","method":"notifications/initialized","params":{}}
			"""), stdout, stderr());

		assertThat(stdout.toString(StandardCharsets.UTF_8)).isEmpty();
	}

	@Test
	void writesParseErrorForMalformedJson() {
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		runner.run(input("{"), stdout, stderr());

		assertThat(stdout.toString(StandardCharsets.UTF_8))
			.isEqualTo("""
				{"jsonrpc":"2.0","error":{"code":-32700,"message":"Parse error"}}
				""");
	}

	private ByteArrayInputStream input(String value) {
		return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
	}

	private PrintStream stderr() {
		return new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
	}
}
