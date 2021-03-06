package com.api.app.ui.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.app.exceptions.UserServiceException;
import com.api.app.service.AddressService;
import com.api.app.service.UserService;
import com.api.app.shared.dto.AddressDto;
import com.api.app.shared.dto.UserDto;
import com.api.app.ui.model.request.UserModel;
import com.api.app.ui.model.response.AddressRest;
import com.api.app.ui.model.response.ErrorMessages;
import com.api.app.ui.model.response.OperationNames;
import com.api.app.ui.model.response.OperationStatus;
import com.api.app.ui.model.response.OperationStatuses;
import com.api.app.ui.model.response.UserRest;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("users") // http://localhost:8080/users
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8040" })
public class UserController {
	static ModelMapper modelMapper = new ModelMapper();

	@Autowired
	UserService userService;

	@Autowired
	AddressService addressService;

	@ApiOperation(value = "${userController.Swagger.GetUser.value}", notes = "${userController.Swagger.GetUser.notes}")
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "bearer JWT token", paramType = "header") })
	@GetMapping(path = "/{userId}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public UserRest getUser(@PathVariable String userId) {
		UserDto userDto = userService.getUserByUserId(userId);

		UserRest returnUser = modelMapper.map(userDto, UserRest.class);
		return returnUser;
	}

	@ApiOperation(value = "${userController.Swagger.GetUserAddresses.value}", notes = "${userController.Swagger.GetUserAddresses.notes}")
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "bearer JWT token", paramType = "header") })
	@GetMapping(path = "/{userId}/addresses", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	@CrossOrigin(origins = "*")
	public List<AddressRest> getUserAddresses(@PathVariable String userId) {
		List<AddressDto> addressesDto = addressService.getAddressesByUserId(userId);

		java.lang.reflect.Type listType = new TypeToken<List<AddressRest>>() {
		}.getType();

		List<AddressRest> returnAddresses = modelMapper.map(addressesDto, listType);
		return returnAddresses;
	}

	@ApiOperation(value = "${userController.Swagger.GetUserAddress.value}", notes = "${userController.Swagger.GetUserAddress.notes}")
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "bearer JWT token", paramType = "header") })
	@GetMapping(path = "/{userId}/addresses/{addressId}", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE })
	public AddressRest getUserAddress(@PathVariable String addressId) {
		AddressDto addressDto = addressService.getAddress(addressId);

		AddressRest returnAddress = modelMapper.map(addressDto, AddressRest.class);

		return returnAddress;
	}

	@ApiOperation(value = "${userController.Swagger.GetUsers.value}", notes = "${userController.Swagger.GetUsers.notes}")
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "bearer JWT token", paramType = "header") })
	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "limit", defaultValue = "50") int limit) {

		List<UserRest> returnUsers = new ArrayList<>();
		List<UserDto> userDtos = userService.getUsers(page - 1, limit); // user uses entries from 1, but spring boot
																		// starts from zero
		for (UserDto userDto : userDtos) {
			returnUsers.add(modelMapper.map(userDto, UserRest.class));
		}

		return returnUsers;
	}

	@ApiOperation(value = "${userController.Swagger.CreateUser.value}", notes = "${userController.Swagger.CreateUser.notes}")
	@PostMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, consumes = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserRest createUser(@RequestBody UserModel userModel) {

		if (userModel.getFirstName() == null)
			throw new NullPointerException("the first name is null");

		if (userModel.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

		UserDto userDto = modelMapper.map(userModel, UserDto.class);

		UserDto createdUser = userService.createUser(userDto);

		UserRest returnCreatedUser = modelMapper.map(createdUser, UserRest.class);
		return returnCreatedUser;
	}

	@ApiOperation(value = "${userController.Swagger.UpdateUser.value}", notes = "${userController.Swagger.UpdateUser.notes}")
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "bearer JWT token", paramType = "header") })
	@PutMapping(path = "/{userId}", produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserRest updateUser(@PathVariable String userId, @RequestBody UserModel userModel) {
		UserDto userDto = modelMapper.map(userModel, UserDto.class);

		UserDto updatedUserDto = userService.updateUser(userDto, userId);
		UserRest returnUpdatedUser = modelMapper.map(updatedUserDto, UserRest.class);

		return returnUpdatedUser;
	}

	@ApiOperation(value = "${userController.Swagger.DeleteUser.value}", notes = "${userController.Swagger.DeleteUser.notes}")
	@ApiImplicitParams({ @ApiImplicitParam(name = "authorization", value = "bearer JWT token", paramType = "header") })
	@DeleteMapping(path = "/{userId}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public OperationStatus deleteUser(@PathVariable String userId) {
		OperationStatus returnStatus = new OperationStatus(OperationNames.DELETE.name());

		userService.deleteUserByUserId(userId);
		returnStatus.setOperationStatus(OperationStatuses.SUCCESS.name());

		return returnStatus;
	}
}
