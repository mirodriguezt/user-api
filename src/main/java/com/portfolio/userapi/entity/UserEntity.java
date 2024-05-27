package com.portfolio.userapi.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.portfolio.userapi.config.Config;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tb_user")
public class UserEntity implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@Column(nullable = false, unique = true, length = 20)
	private String userName;

	@Column(nullable = false, unique = true, length = 11)
	private String cpf;

	@Column(nullable = false, length = 100)
	private String firstName;

	@Column(nullable = false, length = 100)
	private String lastName;

	@Column(nullable = false)
	@JsonFormat(shape = JsonFormat.Shape.STRING,
			pattern = Config.DATE_FORMAT,
			locale = Config.LOCALIZATION,
			timezone = Config.TIME_ZONE)
	private LocalDate dateOfBirth;

	@Column(length = 50)
	private String email;

	@Column
	@CreationTimestamp
	private LocalDateTime creationDate;

	@Column
	@UpdateTimestamp
	private LocalDateTime updateDate;

	public UserEntity() {
	}

	@Override
	public String toString() {
		return "CPF: " + this.cpf + " Name: " + this.getLastName() + ", " + this.getFirstName();
	}

}
