package com.raschad.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.raschad.app.ws.io.entity.AddressEntity;
import com.raschad.app.ws.io.entity.UserEntity;
import com.raschad.app.ws.io.repositories.AddressRepository;
import com.raschad.app.ws.io.repositories.UserRepository;
import com.raschad.app.ws.service.AddressService;
import com.raschad.app.ws.shared.dto.AddressDTO;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	UserRepository userRepository;
	
	
	@Autowired
	AddressRepository addressRepository;
	
	@Override
	public List<AddressDTO> getAddresses(String userId) {
		
		List<AddressDTO> returnValue = new ArrayList<>();
		ModelMapper modelMapper = new ModelMapper();
		
		UserEntity userEntity = userRepository.findByUserId(userId);
		if(userEntity == null) return returnValue;
		
		Iterable<AddressEntity> addresses = addressRepository.findAllByUserDetails(userEntity);
		for(AddressEntity addressEntity : addresses) {
			returnValue.add( modelMapper.map(addressEntity, AddressDTO.class));
		}
		
		return returnValue;
	}

	@Override
	public AddressDTO getAddress(String addressId) {
		
		AddressDTO returnValue = null;
		ModelMapper modelMapper = new ModelMapper();
		
		AddressEntity addressEntity = addressRepository.findByAddressId(addressId);
		
		if(addressEntity!=null)
			returnValue =  modelMapper.map(addressEntity, AddressDTO.class);

		return returnValue;
	}

}
