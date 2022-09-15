package com.raschad.app.ws.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.raschad.app.ws.exceptions.UserServiceException;
import com.raschad.app.ws.io.entity.AddressEntity;
import com.raschad.app.ws.io.entity.UserEntity;
import com.raschad.app.ws.io.repositories.UserRepository;
import com.raschad.app.ws.shared.AmazonSES;
import com.raschad.app.ws.shared.Utils;
import com.raschad.app.ws.shared.dto.AddressDTO;
import com.raschad.app.ws.shared.dto.UserDto;

class UserServiceImplTest {

	
	@InjectMocks
	UserServiceImpl userService;
	
	@Mock
	UserRepository userRepository;
	
	@Mock
	Utils utils;

	@Mock
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Mock
	AmazonSES amazonSES;
	
	String userId = "asduajskh260";
	String encryptedPassword = "uyagsuydghjabs";
	UserEntity userEntity;
	
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setFirstName("Raschad");
		userEntity.setLastName("Yusifov");
		userEntity.setUserId(userId);
		userEntity.setEncryptedPassword(encryptedPassword);
		userEntity.setEmail("yusifov651@gmail.com");
		userEntity.setEmailVerificationToken("asdha");
		userEntity.setAddresses(getAddressesEntity());
	}

	@Test
	void testGetUser() {
		
		
		when( userRepository.findByEmail(anyString())).thenReturn(userEntity);
		UserDto userDto = userService.getUser("yusifov651@example.com");
		
		assertNotNull(userDto);
		assertEquals("Raschad", userDto.getFirstName());
	}
	
	@Test
	final void testGetUser_UsernamNotFoundException()
	{
		when( userRepository.findByEmail(anyString())).thenReturn(null);
		
		assertThrows(UsernameNotFoundException.class, 
				()->{
					userService.getUser("yusifov651@example.com");
				}
				);
	}
	
	
	@Test
	final void testCreateUser_CreateUserServiceException()
	{
		
		when( userRepository.findByEmail(anyString())).thenReturn(userEntity);
		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDto());
		userDto.setFirstName("Raschad");
		userDto.setLastName("Yusifov");
		userDto.setEmail("yusifov651@gmail.com");
		userDto.setPassword("resad123");

		assertThrows(UserServiceException.class, 
				()->{
					userService.createUser(userDto);
				}
				);
	}
	
    
	@Test
	final void testCreateUser()
	{
	    
		
		when( userRepository.findByEmail(anyString())).thenReturn(null);
		when( utils.generateAddressId(anyInt())).thenReturn("asdhguyadgs");
		when( utils.generateUserId( anyInt())).thenReturn(userId);
		when( bCryptPasswordEncoder.encode( anyString())).thenReturn(encryptedPassword);
		when( userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
		Mockito.doNothing().when(amazonSES).verifyEmail(any(UserDto.class));
		
		
		
		UserDto userDto = new UserDto();
		userDto.setAddresses(getAddressesDto());
		userDto.setFirstName("Raschad");
		userDto.setLastName("Yusifov");
		userDto.setEmail("yusifov651@gmail.com");
		userDto.setPassword("resad123");
		
		UserDto storedUserDetails = userService.createUser(userDto);
		assertNotNull(storedUserDetails);
		assertEquals(userEntity.getFirstName(), storedUserDetails.getFirstName());
		assertEquals(userEntity.getLastName(), storedUserDetails.getLastName());
		assertNotNull(storedUserDetails.getUserId());
		assertEquals(storedUserDetails.getAddresses().size(), userEntity.getAddresses().size());
		verify(utils,times(storedUserDetails.getAddresses().size())).generateAddressId(30);
		verify(bCryptPasswordEncoder,times(1)).encode("resad123");
		verify(userRepository,times(1)).save(any(UserEntity.class));
	}
	
	private List<AddressDTO> getAddressesDto()
	{
		AddressDTO addressDto = new AddressDTO();
		addressDto.setType("shipping");
		addressDto.setCity("Berlin");
		addressDto.setCountry("Deutschland");
		addressDto.setPostalCode("12205");
		addressDto.setStreetName("Unter den Eichen");
		
		
		AddressDTO billingAddressDto = new AddressDTO();
		billingAddressDto.setType("billing");
		billingAddressDto.setCity("Berlin");
		billingAddressDto.setCountry("Deutschland");
		billingAddressDto.setPostalCode("12205");
		billingAddressDto.setStreetName("Unter den Eichen");
		
		
		List<AddressDTO> addresses = new ArrayList<>();
		addresses.add(addressDto);
		addresses.add(billingAddressDto);
		
		return addresses;
	}
	
	private List<AddressEntity> getAddressesEntity()
	{
		List<AddressDTO> addresses = getAddressesDto();
		
		Type listType = new TypeToken<List<AddressEntity>>() {}.getType();
		
		return new ModelMapper().map(addresses, listType);
	}
	
}
