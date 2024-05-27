package com.portfolio.userapi.repository;

import com.portfolio.userapi.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<UserEntity, Long> {

	boolean existsByUserName(String userName);

	boolean existsByCpf(String cpf);

	boolean existsByEmail(String email);

	Optional<UserEntity> findByUserName(String userName);

	List<UserEntity> findAllByUserName(String username);

	Optional<UserEntity> findByCpf(String cpf);

	List<UserEntity> findAllByCpf(String cpf);

	List<UserEntity> findUserByEmail(String email);

	List<UserEntity> findByFirstNameContainsIgnoreCase(String firstName);

	List<UserEntity> findByLastNameContainsIgnoreCase(String lastName);

}
