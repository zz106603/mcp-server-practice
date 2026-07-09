package com.example.mcpstudy.mcp;

import com.example.mcpstudy.user.User;
import com.example.mcpstudy.user.UserService;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class SdkUserMcpTools {

	private final UserService userService;

	public SdkUserMcpTools(UserService userService) {
		this.userService = userService;
	}

	@McpTool(name = "find_user_by_id", description = "Find a user by database id.")
	public Result findUserById(
			@McpToolParam(description = "User id", required = true) Long userId) {
		User user = userService.findUserById(userId);
		return new Result(user.getId(), user.getUsername());
	}

	public record Result(Long id, String username) {
	}
}
