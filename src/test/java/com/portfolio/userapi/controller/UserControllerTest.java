package com.portfolio.userapi.controller;

import com.portfolio.userapi.dto.UserPageRecord;
import com.portfolio.userapi.dto.UserSaveRecord;
import com.portfolio.userapi.entity.UserEntity;
import com.portfolio.userapi.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserControllerTest {

	@Mock
	private UserService userServiceMock;

	@Mock
	private ValidatorFactoryImpl validatorFactoryImpMock;

	@Mock
	private ValidatorImpl validatorImpMock;

	private static MockedStatic<Validation> validationMock;

	@InjectMocks
	private UserController userController;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		validationMock = mockStatic(Validation.class);
	}

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(userServiceMock);
		verifyNoMoreInteractions(validatorFactoryImpMock);
		verifyNoMoreInteractions(validatorImpMock);
		validationMock.close();
	}

	@Test
	public void should_add_an_user_if_there_are_no_conflicts() {
		UserSaveRecord userSaveRecord = new UserSaveRecord("userFake",
				"1234567890",
				"firstNameFake",
				"lastNameFake",
				LocalDate.of(1999, 12, 31),
				"updated@fake.com");

		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUserName("userFake");
		userEntity.setCpf("1234567890");
		userEntity.setFirstName("First Name");
		userEntity.setLastName("Last Name");
		userEntity.setDateOfBirth(LocalDate.of(1999, 12, 31));
		userEntity.setEmail("updated@fake.com");
		userEntity.setCreationDate(LocalDateTime.of(2022, 9, 15, 1, 1, 1));
		userEntity.setUpdateDate(null);

		when(userServiceMock.validateUserData(any(UserEntity.class), eq(true))).thenReturn(StringUtils.EMPTY);
		when(userServiceMock.save(any(UserEntity.class))).thenReturn(userEntity);

		ResponseEntity<Object> responseEntity = userController.addUser(userSaveRecord);

		verify(userServiceMock).validateUserData((any(UserEntity.class)), eq(true));
		verify(userServiceMock).save(any(UserEntity.class));

		UserEntity userEntitySaved = (UserEntity) responseEntity.getBody();

		assertThat(userEntitySaved.getId(), is(1L));
		assertThat(userEntitySaved.getUserName(), is("userFake"));
		assertThat(userEntitySaved.getCpf(), is("1234567890"));
		assertThat(userEntitySaved.getFirstName(), is("First Name"));
		assertThat(userEntitySaved.getLastName(), is("Last Name"));
		assertThat(userEntitySaved.getDateOfBirth(), is(LocalDate.of(1999, 12, 31)));
		assertThat(userEntitySaved.getEmail(), is("updated@fake.com"));
		assertThat(userEntitySaved.getCreationDate(), is(LocalDateTime.of(2022, 9, 15, 1, 1, 1)));
		assertNull(userEntitySaved.getUpdateDate());
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.CREATED.value()));
	}

	@Test
	public void should_not_add_an_user_when_exists_conflicts() {
		UserSaveRecord userSaveRecord = new UserSaveRecord("userFake",
				"1234567890",
				"firstNameFake",
				"lastNameFake",
				LocalDate.of(1999, 12, 31),
				"updated@fake.com");

		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUserName("userFake");
		userEntity.setCpf("1234567890");
		userEntity.setFirstName("First Name");
		userEntity.setLastName("Last Name");
		userEntity.setDateOfBirth(LocalDate.of(1999, 12, 31));
		userEntity.setEmail("updated@fake.com");
		userEntity.setCreationDate(LocalDateTime.of(2022, 9, 15, 1, 1, 1));
		userEntity.setUpdateDate(null);

		when(userServiceMock.validateUserData(any(UserEntity.class), eq(true))).thenReturn("There are conflicts");

		ResponseEntity<Object> responseEntity = userController.addUser(userSaveRecord);

		assertThat(responseEntity.getBody(), is("There are conflicts"));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.UNPROCESSABLE_ENTITY.value()));

		verify(userServiceMock).validateUserData((any(UserEntity.class)), eq(true));
	}

	@Test
	public void should_return_a_records_page_when_all_users_are_consulted() {
		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUserName("userFake");
		userEntity.setCpf("1234567890");
		userEntity.setFirstName("First Name");
		userEntity.setLastName("Last Name");
		userEntity.setDateOfBirth(LocalDate.of(1999, 12, 31));
		userEntity.setEmail("updated@fake.com");
		userEntity.setCreationDate(LocalDateTime.of(2022, 9, 15, 1, 1, 1));
		userEntity.setUpdateDate(null);

		List<UserEntity> userEntityList = Arrays.asList(userEntity);
		Page<UserEntity> pageUsers = new PageImpl<>(userEntityList, PageRequest.of(0, 10), userEntityList.size());

		when(userServiceMock.findAll(any(PageRequest.class))).thenReturn(pageUsers);

		ResponseEntity<UserPageRecord> responseEntity = userController.getAllUsers(0, 10);

		UserPageRecord userPageRecord = responseEntity.getBody();

		assertThat(userPageRecord.actualPage(), is(0));
		assertThat(userPageRecord.totalPages(), is(1));
		assertThat(userPageRecord.totalRecords(), is(1L));

		var userList = userPageRecord.userList();
		assertThat(userList.size(), is(1));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));

		verify(userServiceMock).findAll(any(PageRequest.class));
	}

	@Test
	public void should_return_a_user_entity_when_searching_by_username() {
		when(userServiceMock.findByUserName("userFake")).thenReturn(Optional.of(new UserEntity()));

		ResponseEntity<Object> responseEntity = userController.getUserByUserName("userFake");

		assertNotNull(responseEntity);
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
		var userEntity = responseEntity.getBody();
		assertTrue(userEntity instanceof UserEntity);

		verify(userServiceMock).findByUserName("userFake");
	}

	@Test
	public void should_return_a_message_indicating_that_the_user_was_not_found_when_searching_by_username() {
		when(userServiceMock.findByUserName("userFake")).thenReturn(Optional.empty());

		ResponseEntity<Object> responseEntity = userController.getUserByUserName("userFake");

		assertNotNull(responseEntity);
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		var body = responseEntity.getBody();
		assertTrue(body instanceof String);
		assertThat(body, is("User not found"));

		verify(userServiceMock).findByUserName("userFake");
	}

	@Test
	public void should_return_a_user_entity_when_searching_by_cpf() {
		when(userServiceMock.findByCpf("1234567890")).thenReturn(Optional.of(new UserEntity()));

		ResponseEntity<Object> responseEntity = userController.getUserByCpf("1234567890");

		assertNotNull(responseEntity);
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
		var userEntity = responseEntity.getBody();
		assertTrue(userEntity instanceof UserEntity);

		verify(userServiceMock).findByCpf("1234567890");
	}

	@Test
	public void should_return_a_message_indicating_that_the_user_was_not_found_when_searching_by_cpf() {
		when(userServiceMock.findByCpf("1234567890")).thenReturn(Optional.empty());

		ResponseEntity<Object> responseEntity = userController.getUserByCpf("1234567890");

		assertNotNull(responseEntity);
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		var body = responseEntity.getBody();
		assertTrue(body instanceof String);
		assertThat(body, is("User not found"));

		verify(userServiceMock).findByCpf("1234567890");
	}

	@Test
	void should_return_a_list_of_UserEntities_when_searching_for_the_users_first_name() {
		List<UserEntity> userEntityList = new ArrayList<>();

		when(userServiceMock.findByFirstNameContains("name")).thenReturn(userEntityList);

		ResponseEntity<List<UserEntity>> responseEntity = userController.getUserbyFirstName("name");

		verify(userServiceMock).findByFirstNameContains("name");
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
	}

	@Test
	public void should_return_a_list_of_UserEntities_when_searching_for_the_users_last_name() {
		List<UserEntity> userEntityList = new ArrayList<>();

		when(userServiceMock.findByLastNameContains("name")).thenReturn(userEntityList);

		ResponseEntity<List<UserEntity>> responseEntity = userController.getUserbyLastName("name");

		verify(userServiceMock).findByLastNameContains("name");
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
	}

	@Test
	public void should_delete_a_user_when_you_search_by_id_and_find_it() {
		UserEntity userEntity = new UserEntity();
		when(userServiceMock.findById(1L)).thenReturn(Optional.of(userEntity));
		doNothing().when(userServiceMock).delete(userEntity);

		ResponseEntity<Object> responseEntity = userController.deleteUserById(1L);

		verify(userServiceMock).findById(1L);
		verify(userServiceMock).delete(userEntity);
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
		assertThat(responseEntity.getBody(), is("User has been deleted"));
	}

	@Test
	public void should_not_delete_a_user_when_you_search_by_id_and_not_find_it() {
		when(userServiceMock.findById(1L)).thenReturn(Optional.empty());

		ResponseEntity<Object> responseEntity = userController.deleteUserById(1L);

		verify(userServiceMock).findById(1L);
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		assertThat(responseEntity.getBody(), is("User not found"));
	}

	@Test
	public void should_delete_a_user_when_you_search_by_username_and_find_it() {
		UserEntity userEntity = new UserEntity();
		when(userServiceMock.findByUserName("fakeUsername")).thenReturn(Optional.of(userEntity));
		doNothing().when(userServiceMock).delete(userEntity);

		ResponseEntity<Object> responseEntity = userController.deleteUserByUserName("fakeUsername");

		verify(userServiceMock).findByUserName("fakeUsername");
		verify(userServiceMock).delete(userEntity);
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
		assertThat(responseEntity.getBody(), is("User has been deleted"));
	}

	@Test
	public void should_not_delete_a_user_when_you_search_by_username_and_not_find_it() {
		UserEntity userEntity = new UserEntity();
		when(userServiceMock.findByUserName("fakeUsername")).thenReturn(Optional.empty());

		ResponseEntity<Object> responseEntity = userController.deleteUserByUserName("fakeUsername");

		verify(userServiceMock).findByUserName("fakeUsername");
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		assertThat(responseEntity.getBody(), is("User not found"));
	}

	@Test
	public void should_delete_a_user_when_you_search_by_cpf_and_find_it() {
		UserEntity userEntity = new UserEntity();
		when(userServiceMock.findByCpf("1234567890")).thenReturn(Optional.of(userEntity));
		doNothing().when(userServiceMock).delete(userEntity);

		ResponseEntity<Object> responseEntity = userController.deleteUserByCpf("1234567890");

		verify(userServiceMock).findByCpf("1234567890");
		verify(userServiceMock).delete(userEntity);
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
		assertThat(responseEntity.getBody(), is("User has been deleted"));
	}

	@Test
	public void should_not_delete_a_user_when_you_search_by_cpf_and_not_find_it() {
		UserEntity userEntity = new UserEntity();
		when(userServiceMock.findByCpf("1234567890")).thenReturn(Optional.empty());

		ResponseEntity<Object> responseEntity = userController.deleteUserByCpf("1234567890");

		verify(userServiceMock).findByCpf("1234567890");
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		assertThat(responseEntity.getBody(), is("User not found"));
	}

	@Test
	public void should_update_all_the_fields_of_a_user_when_it_is_updated_by_id_and_it_is_found() {
		UserSaveRecord userSaveRecord = new UserSaveRecord("newUserName",
				"1234567890",
				"New First Name",
				"New Last Name",
				LocalDate.of(1999, 12, 31),
				"new_email@fake.com");

		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUserName("oldUserName");
		userEntity.setCpf("1234567890");
		userEntity.setFirstName("Old Name");
		userEntity.setLastName("Old Name");
		userEntity.setDateOfBirth(LocalDate.of(1999, 12, 31));
		userEntity.setEmail("new_email@fake.com");
		userEntity.setCreationDate(LocalDateTime.of(2022, 9, 15, 1, 1, 1));
		userEntity.setUpdateDate(LocalDateTime.of(2022, 9, 15, 1, 1, 1));

		Set<ConstraintViolation<UserSaveRecord>> constraintViolations = new HashSet<>();

		ArgumentCaptor<UserEntity> userModelArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);

		when(userServiceMock.findById(1L)).thenReturn(Optional.of(userEntity));
		validationMock.when(() -> Validation.buildDefaultValidatorFactory()).thenReturn(validatorFactoryImpMock);
		when(validatorFactoryImpMock.getValidator()).thenReturn(validatorImpMock);
		when(validatorImpMock.validate(any(UserSaveRecord.class))).thenReturn(constraintViolations);
		when(userServiceMock.validateUserData(any(UserEntity.class), eq(false))).thenReturn(StringUtils.EMPTY);
		when(userServiceMock.save(any(UserEntity.class))).thenReturn(any(UserEntity.class));

		ResponseEntity<Object> responseEntity = userController.updateUserById(1L, userSaveRecord);

		verify(userServiceMock).findById(1L);
		verify(userServiceMock).validateUserData(userModelArgumentCaptor.capture(), eq(false));
		verify(userServiceMock).save((any(UserEntity.class)));

		UserEntity UserEntitySaved = userModelArgumentCaptor.getValue();

		assertThat(UserEntitySaved.getCpf(), is("1234567890"));
		assertThat(UserEntitySaved.getFirstName(), is("New First Name"));
		assertThat(UserEntitySaved.getLastName(), is("New Last Name"));
		assertThat(UserEntitySaved.getDateOfBirth(), is(LocalDate.of(1999, 12, 31)));
		assertThat(UserEntitySaved.getEmail(), is("new_email@fake.com"));
		assertThat(UserEntitySaved.getCreationDate(), is(LocalDateTime.of(2022, 9, 15, 1, 1, 1)));
		assertThat(UserEntitySaved.getUpdateDate(), is(LocalDateTime.of(2022, 9, 15, 1, 1, 1)));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
	}

	@Test
	public void should_no_update_user_record_when_id_not_exists() {
		UserSaveRecord userSaveRecord = new UserSaveRecord("newUserName",
				"1234567890",
				"New First Name",
				"New Last Name",
				LocalDate.of(1999, 12, 31),
				"new_email@fake.com");

		when(userServiceMock.findById(1L)).thenReturn(Optional.ofNullable(null));

		ResponseEntity<Object> responseEntity = userController.updateUserById(1L, userSaveRecord);

		verify(userServiceMock).findById(1L);
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		assertThat(responseEntity.getBody(), is("User not found"));
	}

	@Test
	public void should_update_all_the_fields_of_a_user_when_it_is_updated_by_username_and_it_is_found() {
		UserSaveRecord userSaveRecord = new UserSaveRecord("newUserName",
				"1234567890",
				"New First Name",
				"New Last Name",
				LocalDate.of(1999, 12, 31),
				"new_email@fake.com");

		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUserName("oldUserName");
		userEntity.setCpf("1234567890");
		userEntity.setFirstName("Old Name");
		userEntity.setLastName("Old Name");
		userEntity.setDateOfBirth(LocalDate.of(1999, 12, 31));
		userEntity.setEmail("new_email@fake.com");
		userEntity.setCreationDate(LocalDateTime.of(2022, 9, 15, 1, 1, 1));
		userEntity.setUpdateDate(LocalDateTime.of(2022, 9, 15, 1, 1, 1));

		Set<ConstraintViolation<UserSaveRecord>> constraintViolations = new HashSet<>();

		ArgumentCaptor<UserEntity> userModelArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);

		when(userServiceMock.findByUserName("fakeUserName")).thenReturn(Optional.of(userEntity));
		validationMock.when(() -> Validation.buildDefaultValidatorFactory()).thenReturn(validatorFactoryImpMock);
		when(validatorFactoryImpMock.getValidator()).thenReturn(validatorImpMock);
		when(validatorImpMock.validate(any(UserSaveRecord.class))).thenReturn(constraintViolations);
		when(userServiceMock.validateUserData(any(UserEntity.class), eq(false))).thenReturn(StringUtils.EMPTY);
		when(userServiceMock.save(any(UserEntity.class))).thenReturn(any(UserEntity.class));

		ResponseEntity<Object> responseEntity = userController.updateUserByUserName("fakeUserName", userSaveRecord);

		verify(userServiceMock).findByUserName("fakeUserName");
		verify(userServiceMock).validateUserData(userModelArgumentCaptor.capture(), eq(false));
		verify(userServiceMock).save((any(UserEntity.class)));

		UserEntity UserEntitySaved = userModelArgumentCaptor.getValue();

		assertThat(UserEntitySaved.getCpf(), is("1234567890"));
		assertThat(UserEntitySaved.getFirstName(), is("New First Name"));
		assertThat(UserEntitySaved.getLastName(), is("New Last Name"));
		assertThat(UserEntitySaved.getDateOfBirth(), is(LocalDate.of(1999, 12, 31)));
		assertThat(UserEntitySaved.getEmail(), is("new_email@fake.com"));
		assertThat(UserEntitySaved.getCreationDate(), is(LocalDateTime.of(2022, 9, 15, 1, 1, 1)));
		assertThat(UserEntitySaved.getUpdateDate(), is(LocalDateTime.of(2022, 9, 15, 1, 1, 1)));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
	}

	@Test
	public void should_no_update_user_record_when_username_not_exists() {
		UserSaveRecord userSaveRecord = new UserSaveRecord("newUserName",
				"1234567890",
				"New First Name",
				"New Last Name",
				LocalDate.of(1999, 12, 31),
				"new_email@fake.com");

		when(userServiceMock.findByUserName("fakeUserName")).thenReturn(Optional.empty());

		ResponseEntity<Object> responseEntity = userController.updateUserByUserName("fakeUserName", userSaveRecord);

		verify(userServiceMock).findByUserName("fakeUserName");
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		assertThat(responseEntity.getBody(), is("User not found"));
	}

	@Test
	public void should_update_all_the_fields_of_a_user_when_it_is_updated_by_cpf_and_it_is_found() {
		UserSaveRecord userSaveRecord = new UserSaveRecord("newUserName",
				"1234567890",
				"New First Name",
				"New Last Name",
				LocalDate.of(1999, 12, 31),
				"new_email@fake.com");

		UserEntity userEntity = new UserEntity();
		userEntity.setId(1L);
		userEntity.setUserName("oldUserName");
		userEntity.setCpf("1234567890");
		userEntity.setFirstName("Old Name");
		userEntity.setLastName("Old Name");
		userEntity.setDateOfBirth(LocalDate.of(1999, 12, 31));
		userEntity.setEmail("new_email@fake.com");
		userEntity.setCreationDate(LocalDateTime.of(2022, 9, 15, 1, 1, 1));
		userEntity.setUpdateDate(LocalDateTime.of(2022, 9, 15, 1, 1, 1));

		Set<ConstraintViolation<UserSaveRecord>> constraintViolations = new HashSet<>();

		ArgumentCaptor<UserEntity> userModelArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);

		when(userServiceMock.findByCpf("1234567890")).thenReturn(Optional.of(userEntity));
		validationMock.when(() -> Validation.buildDefaultValidatorFactory()).thenReturn(validatorFactoryImpMock);
		when(validatorFactoryImpMock.getValidator()).thenReturn(validatorImpMock);
		when(validatorImpMock.validate(any(UserSaveRecord.class))).thenReturn(constraintViolations);
		when(userServiceMock.validateUserData(any(UserEntity.class), eq(false))).thenReturn(StringUtils.EMPTY);
		when(userServiceMock.save(any(UserEntity.class))).thenReturn(any(UserEntity.class));

		ResponseEntity<Object> responseEntity = userController.updateUserByCpf("1234567890", userSaveRecord);

		verify(userServiceMock).findByCpf("1234567890");
		verify(userServiceMock).validateUserData(userModelArgumentCaptor.capture(), eq(false));
		verify(userServiceMock).save((any(UserEntity.class)));

		UserEntity UserEntitySaved = userModelArgumentCaptor.getValue();

		assertThat(UserEntitySaved.getCpf(), is("1234567890"));
		assertThat(UserEntitySaved.getFirstName(), is("New First Name"));
		assertThat(UserEntitySaved.getLastName(), is("New Last Name"));
		assertThat(UserEntitySaved.getDateOfBirth(), is(LocalDate.of(1999, 12, 31)));
		assertThat(UserEntitySaved.getEmail(), is("new_email@fake.com"));
		assertThat(UserEntitySaved.getCreationDate(), is(LocalDateTime.of(2022, 9, 15, 1, 1, 1)));
		assertThat(UserEntitySaved.getUpdateDate(), is(LocalDateTime.of(2022, 9, 15, 1, 1, 1)));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.OK.value()));
	}

	@Test
	public void should_no_update_user_record_when_cpf_not_exists() {
		UserSaveRecord userSaveRecord = new UserSaveRecord("newUserName",
				"1234567890",
				"New First Name",
				"New Last Name",
				LocalDate.of(1999, 12, 31),
				"new_email@fake.com");

		when(userServiceMock.findByCpf("1234567890")).thenReturn(Optional.empty());

		ResponseEntity<Object> responseEntity = userController.updateUserByCpf("1234567890", userSaveRecord);

		verify(userServiceMock).findByCpf("1234567890");
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		assertThat(responseEntity.getBody(), is("User not found"));
	}
}
