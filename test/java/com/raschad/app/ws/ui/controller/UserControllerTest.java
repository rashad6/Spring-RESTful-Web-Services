package com.raschad.app.ws.ui.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.raschad.app.ws.service.UserService;
import com.raschad.app.ws.service.impl.UserServiceImpl;
import com.raschad.app.ws.shared.dto.AddressDTO;
import com.raschad.app.ws.shared.dto.UserDto;
import com.raschad.app.ws.ui.model.response.UserRest;

class UserControllerTest {

	@InjectMocks
	UserController userController;

	@Mock
	UserServiceImpl userService;

	@Mock
	UserDto userDto;
	
	final String USER_ID = "adsuhhadks";
	
	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		userDto = new UserDto();
		userDto.setFirstName("Raschad");
		userDto.setLastName("Yusifov");
		userDto.setEmail("yusifov651@gmail.com");
		userDto.setEmailVerificationStatus(Boolean.FALSE);
		userDto.setEmailVerificationToken(null);
		userDto.setUserId(USER_ID);
		userDto.setEncryptedPassword("xaskdhkh12");
		userDto.setAddresses(getAddressesDto());
		
	}

	@Test
	void testGetUser() {
    
		when(userService.getUserById(anyString())).thenReturn(userDto);
		
		UserRest userRest = userController.getUser(USER_ID);
		
		assertNotNull(userRest);
		assertEquals(USER_ID, userRest.getUserId());
		assertEquals(userDto.getFirstName(), userRest.getFirstName());
		assertEquals(userDto.getLastName(), userRest.getLastName());
        //assertTrue(userDto.getAddresses().size()==userRest.getAddresses().size());
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
}
