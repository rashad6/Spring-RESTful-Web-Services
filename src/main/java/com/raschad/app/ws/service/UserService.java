package com.raschad.app.ws.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.raschad.app.ws.shared.dto.UserDto;
import com.raschad.app.ws.ui.model.response.UserRest;

public interface UserService extends UserDetailsService {
   
	UserDto createUser(UserDto user);
	UserDto getUser(String email);
	UserDto getUserById(String id);
	UserDto updateUser(String userId, UserDto user);
	void deleteUser(String userId);
	List<UserDto> getUsers(int page, int limit);
	boolean verifyEmailToken(String token);
}
