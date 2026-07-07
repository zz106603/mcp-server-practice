package com.example.mcpstudy.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.example.mcpstudy.user.User;
import com.example.mcpstudy.user.UserNotFoundException;
import com.example.mcpstudy.user.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindUserByIdToolTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private FindUserByIdTool tool;

	@Test
	void nameReturnsMcpToolName() {
		assertThat(tool.name()).isEqualTo("find_user_by_id");
	}

	@Test
	void executeFindsUserByUserId() {
		User user = new User("alice");

		when(userService.findUserById(1L)).thenReturn(user);

		McpToolResponse response = tool.execute(new McpToolRequest("find_user_by_id", Map.of("userId", 1L)));

		assertThat(response.success()).isTrue();
		FindUserByIdTool.Result result = (FindUserByIdTool.Result) response.result();
		assertThat(result.username()).isEqualTo("alice");
	}

	@Test
	void executeThrowsExceptionWhenUserIdIsMissing() {
		assertThatThrownBy(() -> tool.execute(new McpToolRequest("find_user_by_id", Map.of())))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("userId is required");
	}

	@Test
	void executeThrowsExceptionWhenUserIdIsNull() {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put("userId", null);

		assertThatThrownBy(() -> tool.execute(new McpToolRequest("find_user_by_id", arguments)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("userId is required");
	}

	@Test
	void executeThrowsExceptionWhenUserIdTypeIsInvalid() {
		assertThatThrownBy(() -> tool.execute(new McpToolRequest("find_user_by_id", Map.of("userId", "one"))))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("userId must be a number");
	}

	@Test
	void executeReturnsErrorWhenUserDoesNotExist() {
		when(userService.findUserById(99L)).thenThrow(new UserNotFoundException(99L));

		McpToolResponse response = tool.execute(new McpToolRequest("find_user_by_id", Map.of("userId", 99L)));

		assertThat(response.success()).isFalse();
		assertThat(response.errorMessage()).isEqualTo("User not found. id=99");
	}
}
