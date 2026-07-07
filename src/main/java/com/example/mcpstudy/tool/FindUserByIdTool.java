package com.example.mcpstudy.tool;

import java.util.List;
import java.util.Map;

import com.example.mcpstudy.user.User;
import com.example.mcpstudy.user.UserNotFoundException;
import com.example.mcpstudy.user.UserService;

import org.springframework.stereotype.Component;

@Component
public class FindUserByIdTool implements McpTool {

	public static final String TOOL_NAME = "find_user_by_id";

	private final UserService userService;

	public FindUserByIdTool(UserService userService) {
		this.userService = userService;
	}

	@Override
	public String name() {
		return TOOL_NAME;
	}

	@Override
	public String description() {
		return "Find a user by database id.";
	}

	@Override
	public Map<String, Object> inputSchema() {
		return Map.of(
			"type", "object",
			"properties", Map.of(
				"userId", Map.of(
					"type", "integer",
					"description", "User id"
				)
			),
			"required", List.of("userId")
		);
	}

	@Override
	public McpToolResponse execute(McpToolRequest request) {
		Long userId = extractUserId(request);

		try {
			User user = userService.findUserById(userId);
			return McpToolResponse.success(new Result(user.getId(), user.getUsername()));
		}
		catch (UserNotFoundException e) {
			return McpToolResponse.error(e.getMessage());
		}
	}

	private Long extractUserId(McpToolRequest request) {
		if (request == null || !request.arguments().containsKey("userId")) {
			throw new IllegalArgumentException("userId is required");
		}

		Object userId = request.arguments().get("userId");
		if (userId == null) {
			throw new IllegalArgumentException("userId is required");
		}

		if (!(userId instanceof Number number)) {
			throw new IllegalArgumentException("userId must be a number");
		}

		return number.longValue();
	}

	public record Result(Long id, String username) {
	}
}
