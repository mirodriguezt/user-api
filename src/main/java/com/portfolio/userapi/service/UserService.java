package com.portfolio.userapi.service;

import com.portfolio.userapi.config.Config;
import com.portfolio.userapi.entity.UserEntity;
import com.portfolio.userapi.repository.IUserRepository;
import com.portfolio.userapi.util.Tools;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private static final String USER_LEGEND_INVALID_CPF = "Invalid CPF!";
	private static final String USER_LEGEND_USERNAME_EXISTS = "Username already exist!";
	private static final String USER_LEGEND_CPF_EXISTS = "CPF already exist!";
	private static final String USER_LEGEND_AGE_NOT_ADMITED = "Only users over 18 years of age must be registered!";
	private static final String USER_LEGEND_EMAIL_IS_INVALID = "This email is invalid!";
	private static final String USER_LEGEND_EMAIL_ALREADY_ASSIGNED = "This email has been assigned another user!";

	@Autowired
	private IUserRepository iUserRepository;

	@Transactional
	public UserEntity save(UserEntity userEntity) {
		try {
			return iUserRepository.save(userEntity);
		} finally {
			log.info("User saved -> cpf:{}", userEntity.getCpf());
		}
	}

	@Transactional
	public void delete(UserEntity userEntity) {
		try {
			iUserRepository.delete(userEntity);
		} finally {
			log.info("User deleted -> cpf:{}", userEntity.getCpf());
		}
	}

	public boolean existsByUserName(String userName) {
		return iUserRepository.existsByUserName(userName);
	}

	public boolean existsByCpf(String cpf) {
		return iUserRepository.existsByCpf(cpf);
	}

	public boolean existsByEmail(String email) {
		return iUserRepository.existsByEmail(email);
	}

	public Optional<UserEntity> findById(long id) {
		return iUserRepository.findById(id);
	}

	public Optional<UserEntity> findByUserName(String userName) {
		return iUserRepository.findByUserName(userName);
	}

	public boolean existUsersExludingId(List<UserEntity> userEntityList, long id) {

		List<UserEntity> filteredList = userEntityList
				.stream()
				.filter(x -> x.getId() != id)
				.toList();

		return !filteredList.isEmpty();
	}

	public Optional<UserEntity> findByCpf(String cpf) {
		return iUserRepository.findByCpf(cpf);
	}

	public List<UserEntity> findByFirstNameContains(String firstName) {
		return iUserRepository.findByFirstNameContainsIgnoreCase(firstName);
	}

	public List<UserEntity> findByLastNameContains(String lastName) {
		return iUserRepository.findByLastNameContainsIgnoreCase(lastName);
	}

	public List<UserEntity> findUserByEmailAndCpfDiffersToGiven(String email, String cpf) {
		List<UserEntity> userModelList = iUserRepository.findUserByEmail(email);
		return userModelList.stream()
				.filter(x -> !cpf.equals(x.getCpf()))
				.collect(Collectors.toList());
	}

	public Page<UserEntity> findAll(Pageable pageable) {
		return iUserRepository.findAll(pageable);
	}

	public String validateUserData(UserEntity userEntity, boolean isNewUser) {

		if (isUserAgeAllowed(userEntity.getDateOfBirth())) {
			return USER_LEGEND_AGE_NOT_ADMITED;
		}

		String legendValidateUserName = validateUserName(userEntity, isNewUser);
		if (!legendValidateUserName.isEmpty()) {
			return legendValidateUserName;
		}

		String legendValidateCpf = validateCpf(userEntity, isNewUser);
		if (!legendValidateCpf.isEmpty()) {
			return legendValidateCpf;
		}

		return validateEmail(isNewUser, userEntity);
	}

	private String validateUserName(UserEntity userEntity, boolean isNewUser) {

		String userName = userEntity.getUserName();
		if (isNewUser) {
			if (existsByUserName(userName)) {
				return USER_LEGEND_USERNAME_EXISTS;
			}
		} else {
			if (existUsersExludingId(iUserRepository.findAllByUserName(userName), userEntity.getId())) {
				return USER_LEGEND_USERNAME_EXISTS;
			}
		}

		return "";
	}

	private String validateCpf(UserEntity userEntity, boolean isNewUser) {

		String cpf = userEntity.getCpf();

		if (!Tools.isValidCpf(cpf)) {
			return USER_LEGEND_INVALID_CPF;
		}

		if (isNewUser) {
			if (existsByCpf(cpf)) {
				return USER_LEGEND_CPF_EXISTS;
			}
		} else {
			if (existUsersExludingId(iUserRepository.findAllByCpf(cpf), userEntity.getId())) {
				return USER_LEGEND_CPF_EXISTS;
			}
		}

		return "";
	}

	private String validateEmail(boolean isNewUser, UserEntity userEntity) {

		String email = userEntity.getEmail();
		if (Objects.nonNull(email)) {
			if (!Tools.isValidEmail(email)) {
				return USER_LEGEND_EMAIL_IS_INVALID;
			}

			if (isNewUser) {
				if (existsByEmail(email)) {
					return USER_LEGEND_EMAIL_ALREADY_ASSIGNED;
				}
			} else {
				if (!findUserByEmailAndCpfDiffersToGiven(email, userEntity.getCpf()).isEmpty()) {
					return USER_LEGEND_EMAIL_ALREADY_ASSIGNED;
				}
			}
		}

		return "";
	}

	private boolean isUserAgeAllowed(LocalDate dateOfBirth) {
		LocalDate currentDate = LocalDate.now();
		var period = Period.between(dateOfBirth, currentDate);

		return period.getYears() < Config.ALLOWED_AGE_USERS_REGISTRATION;
	}

}
