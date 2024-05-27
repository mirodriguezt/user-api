package com.portfolio.userapi.controller;

import com.portfolio.userapi.dto.UserPageRecord;
import com.portfolio.userapi.dto.UserSaveRecord;
import com.portfolio.userapi.entity.UserEntity;
import com.portfolio.userapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

	private final static String LEGEND_USER_NOT_FOUND = "User not found";
	private final static String LEGEND_USER_DELETED = "User has been deleted";

	@Autowired
	private UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@Operation(summary = "Add a user", description = "Allows adding a user record")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "User created"),
			@ApiResponse(responseCode = "422", description = "Unprocessable entity")
	})
	@PostMapping(value = "/add",
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<Object> addUser(@RequestBody @Valid UserSaveRecord userSaveRecord) {
		UserEntity userEntity = new UserEntity();
		userEntity.setUserName(userSaveRecord.userName());
		userEntity.setCpf(userSaveRecord.cpf());
		userEntity.setFirstName(userSaveRecord.firstName());
		userEntity.setLastName(userSaveRecord.lastName());
		userEntity.setDateOfBirth(userSaveRecord.dateOfBirth());
		userEntity.setEmail(userSaveRecord.email());

		String validateNewUserLegend = userService.validateUserData(userEntity, true);

		if (!validateNewUserLegend.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validateNewUserLegend);
		} else {

			return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(userEntity));
		}
	}

	@Operation(summary = "Get all users", description = "Returns all users")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved"),
	})
	@GetMapping("/all")
	public ResponseEntity<UserPageRecord> getAllUsers(@RequestParam(defaultValue = "0") int page,
													  @RequestParam(defaultValue = "10") int size) {

		Page<UserEntity> pagedUsers = userService.findAll(PageRequest.of(page, size));
		UserPageRecord userPageRecord = new UserPageRecord(pagedUsers.getNumber(), pagedUsers.getTotalElements(), pagedUsers.getTotalPages(), pagedUsers.getContent());

		return ResponseEntity.status(HttpStatus.OK).body(userPageRecord);
	}

	@Operation(summary = "Get a user by username", description = "Returns a user record given its username")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved"),
	})
	@GetMapping("/username/{username}")
	public ResponseEntity<Object> getUserByUserName(@PathVariable(value = "username") String userName) {
		Optional<UserEntity> userEntityOptional = userService.findByUserName(userName);
		return !userEntityOptional.isPresent()
				? ResponseEntity.status(HttpStatus.NOT_FOUND).body(LEGEND_USER_NOT_FOUND)
				: ResponseEntity.status(HttpStatus.OK).body(userEntityOptional.get());
	}

	@Operation(summary = "Get a user by cpf", description = "Returns a user record given its cpf")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved"),
	})
	@GetMapping("/cpf/{cpf}")
	public ResponseEntity<Object> getUserByCpf(@PathVariable(value = "cpf") String cpf) {
		Optional<UserEntity> userEntityOptional = userService.findByCpf(cpf);
		return userEntityOptional.
				<ResponseEntity<Object>>map(userEntity -> ResponseEntity.status(HttpStatus.OK).body(userEntity))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(LEGEND_USER_NOT_FOUND));
	}

	@Operation(summary = "Gets users given first name", description = "Returns the users given the first name or part of it")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved"),
	})
	@RequestMapping(value = {"/user/filter/firstname"}, method = RequestMethod.GET, params = "firstname")
	public ResponseEntity<List<UserEntity>> getUserbyFirstName(
			@RequestParam(value = "firstname") String firstName) {

		return ResponseEntity.status(HttpStatus.OK).body(userService.findByFirstNameContains(firstName));
	}

	@Operation(summary = "Gets users given last name", description = "Returns the users given the last name or part of it")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved"),
	})
	@RequestMapping(value = {"/user/filter/lastname"}, method = RequestMethod.GET, params = "lastname")
	public ResponseEntity<List<UserEntity>> getUserbyLastName(
			@RequestParam(value = "lastname") String lastName) {

		return ResponseEntity.status(HttpStatus.OK).body(userService.findByLastNameContains(lastName));
	}

	@Operation(summary = "Delete a user given their id", description = "Delete a user record by giving its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User has been deleted"),
			@ApiResponse(responseCode = "404", description = "User not found"),
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteUserById(@PathVariable(value = "id") long id) {

		return deleteUser(userService.findById(id));
	}

	@Operation(summary = "Delete a user given their username", description = "Delete a user record by giving its username")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User has been deleted"),
			@ApiResponse(responseCode = "404", description = "User not found"),
	})
	@DeleteMapping("/username/{username}")
	public ResponseEntity<Object> deleteUserByUserName(@PathVariable(value = "username") String userName) {

		return deleteUser(userService.findByUserName(userName));
	}

	@Operation(summary = "Delete a user given their cpf", description = "Delete a user record by giving its cpf")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User has been deleted"),
			@ApiResponse(responseCode = "404", description = "User not found"),
	})
	@DeleteMapping("/cpf/{cpf}")
	public ResponseEntity<Object> deleteUserByCpf(@PathVariable(value = "cpf") String cpf) {

		return deleteUser(userService.findByCpf(cpf));
	}

	@Operation(summary = "Modify a user record given its id", description = "Modify a user record given its id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User has been modify"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "422", description = "Unprocessable Entity")
	})
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateUserById(@PathVariable(value = "id") long id,
												 @RequestBody @Valid UserSaveRecord userSaveRecord) {

		return UpdateUser(userService.findById(id), userSaveRecord);
	}

	@Operation(summary = "Modify a user given their username", description = "Modify a user given their username")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User has been modify"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "422", description = "Unprocessable Entity")
	})
	@PutMapping("/username/{username}")
	public ResponseEntity<Object> updateUserByUserName(@PathVariable(value = "username") String userName,
													   @RequestBody @Valid UserSaveRecord userSaveRecord) {

		return UpdateUser(userService.findByUserName(userName), userSaveRecord);
	}

	@Operation(summary = "Modify a user given their cpf", description = "Modify a user given their cpf")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User has been modify"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "422", description = "Unprocessable Entity")
	})
	@PutMapping("/cpf/{cpf}")
	public ResponseEntity<Object> updateUserByCpf(@PathVariable(value = "cpf") String cpf,
												  @RequestBody @Valid UserSaveRecord userSaveRecord) {

		return UpdateUser(userService.findByCpf(cpf), userSaveRecord);
	}

	private ResponseEntity<Object> deleteUser(Optional<UserEntity> userEntityOptional) {

		if (userEntityOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(LEGEND_USER_NOT_FOUND);
		}

		userService.delete(userEntityOptional.get());

		return ResponseEntity.status(HttpStatus.OK).body(LEGEND_USER_DELETED);
	}

	private ResponseEntity<Object> UpdateUser(Optional<UserEntity> userEntityOptional, UserSaveRecord userSaveRecord) {

		if (userEntityOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(LEGEND_USER_NOT_FOUND);
		}

		UserEntity userEntity = userEntityOptional.get();
		userEntity.setCpf(userSaveRecord.cpf());
		userEntity.setUserName(userSaveRecord.userName());
		userEntity.setFirstName(userSaveRecord.firstName());
		userEntity.setLastName(userSaveRecord.lastName());
		userEntity.setDateOfBirth(userSaveRecord.dateOfBirth());
		userEntity.setEmail(userSaveRecord.email());

		String validateUserLegend = userService.validateUserData(userEntity, false);

		if (!validateUserLegend.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(validateUserLegend);
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(userService.save(userEntity));
		}
	}
}
