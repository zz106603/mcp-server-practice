package com.example.mcpstudy.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@Test
	void findUserByIdReturnsUser() {
		User user = new User("alice");

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		User foundUser = userService.findUserById(1L);

		assertThat(foundUser).isSameAs(user);
	}

	@Test
	void findUserByIdThrowsExceptionWhenUserDoesNotExist() {
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.findUserById(99L))
			.isInstanceOf(UserNotFoundException.class)
			.hasMessage("User not found. id=99");
	}
}
