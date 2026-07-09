package com.example.mcpstudy.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.mcpstudy.user.User;
import com.example.mcpstudy.user.UserService;

import org.junit.jupiter.api.Test;

class SdkUserMcpToolsTest {

	private final UserService userService = org.mockito.Mockito.mock(UserService.class);
	private final SdkUserMcpTools tools = new SdkUserMcpTools(userService);

	@Test
	void findUserByIdDelegatesToUserService() {
		User user = new User("alice");
		when(userService.findUserById(1L)).thenReturn(user);

		SdkUserMcpTools.Result result = tools.findUserById(1L);

		assertThat(result.username()).isEqualTo("alice");
	}
}
