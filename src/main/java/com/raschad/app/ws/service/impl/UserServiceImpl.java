package com.raschad.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.raschad.app.ws.exceptions.UserServiceException;
import com.raschad.app.ws.io.entity.PasswordResetTokenEntity;
import com.raschad.app.ws.io.entity.UserEntity;
import com.raschad.app.ws.io.repositories.PasswordResetTokenRepository;
import com.raschad.app.ws.io.repositories.UserRepository;
import com.raschad.app.ws.service.UserService;
import com.raschad.app.ws.shared.AmazonSES;
import com.raschad.app.ws.shared.Utils;
import com.raschad.app.ws.shared.dto.AddressDTO;
import com.raschad.app.ws.shared.dto.UserDto;
import com.raschad.app.ws.ui.model.response.ErrorMessages;

import ch.qos.logback.classic.pattern.Util;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Autowired
	AmazonSES amazonSES;

	@Override
	public UserDto createUser(UserDto user) {

		if (userRepository.findByEmail(user.getEmail()) != null)
			throw new UserServiceException("Record already exists");

		for (int i = 0; i < user.getAddresses().size(); i++) {

			AddressDTO address = user.getAddresses().get(i);
			address.setUserDetails(user);
			address.setAddressId(utils.generateAddressId(30));
			user.getAddresses().set(i, address);

		}

		UserEntity userEntity = new UserEntity();
		ModelMapper modelMapper = new ModelMapper();

		userEntity = modelMapper.map(user, UserEntity.class);

		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
		userEntity.setEmailVerificationStatus(false);

		UserEntity storedUserDetails = userRepository.save(userEntity);

		UserDto returnValue = new UserDto();
		// BeanUtils.copyProperties(storedUserDetails, returnValue);

		returnValue = modelMapper.map(storedUserDetails, UserDto.class);
		
		//send an email message to user to verify their email
		amazonSES.verifyEmail(returnValue);

		return returnValue;
	}

	@Override
	public UserDto getUser(String email) {

		UserEntity userEntity = userRepository.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		UserEntity userEntity = userRepository.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(),
				userEntity.getEmailVerificationStatus(),
				true, true, 
				true, new ArrayList<>());
		
		//return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDto getUserById(String userId) {

		UserDto returnValue = new UserDto();

		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException(userId);

		BeanUtils.copyProperties(userEntity, returnValue);

		return returnValue;
	}

	@Override
	public UserDto updateUser(String userId, UserDto user) {
		UserDto returnValue = new UserDto();

		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userEntity.setFirstName(user.getFirstName());
		UserEntity updatedUserDetails = userRepository.save(userEntity);

		BeanUtils.copyProperties(updatedUserDetails, returnValue);

		return returnValue;
	}

	@Override
	public void deleteUser(String userId) {

		UserEntity userEntity = userRepository.findByUserId(userId);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userRepository.delete(userEntity);

	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {

		List<UserDto> returnValue = new ArrayList<>();

		if (page > 0)
			page = page - 1;

		Pageable pageableRequest = PageRequest.of(page, limit);

		Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = usersPage.getContent();

		for (UserEntity userEntity : users) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			returnValue.add(userDto);
		}

		return returnValue;
	}

	@Override
	public boolean verifyEmailToken(String token) {
		boolean returnValue = false;
		
		UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
		if(userEntity != null) {
			boolean hasTokenExpired = Utils.hasTokenExpired(token);
			if(!hasTokenExpired) {
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				returnValue = true;
			}
		}
		
		return returnValue;
	}

	@Override
	public boolean requestPasswordReset(String email) {
		
		boolean returnValue = false;
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if(userEntity==null)
		{
			return returnValue;
		}
		
		String token = new Utils().generatePasswordResetToken(userEntity.getUserId());
		
		PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
		passwordResetTokenEntity.setToken(token);
		passwordResetTokenEntity.setUserDetails(userEntity);
		passwordResetTokenRepository.save(passwordResetTokenEntity);	
		
		returnValue = new AmazonSES().sendPasswordResetRequest(
				userEntity.getFirstName(),
				userEntity.getEmail(),
				token
				);
		return returnValue;
	}

	@Override
	public boolean resetPassword(String token, String password) {
		
	 	boolean returnValue = false;
	 	
	 	if(Utils.hasTokenExpired(token))
	 	{
	 		return returnValue;
	 	}
	    
	 	PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);
	 	
	 	if(passwordResetTokenEntity == null)
	 	{
	 		return returnValue;
	 	}
	 	
	 	//Prepare new Password
	 	String endcodedPassword = bCryptPasswordEncoder.encode(password);
		
	 	//update user password in database
	 	UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
	 	userEntity.setEncryptedPassword(endcodedPassword);
	 	UserEntity savedUserEntity = userRepository.save(userEntity);
	 	
	 	//verify if password was saved successfully
	 	if(savedUserEntity!=null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(endcodedPassword))
	 	{
	 		returnValue=true;
	 	}
		//remove password reset token from database
	 	passwordResetTokenRepository.delete(passwordResetTokenEntity);
	 	
		return returnValue;
	}

}
