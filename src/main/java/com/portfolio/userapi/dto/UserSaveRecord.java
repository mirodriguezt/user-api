package com.portfolio.userapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.portfolio.userapi.config.Config;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

public record UserSaveRecord(@NotBlank
							 @Size(max = 20)
							 String userName,
							 @NotBlank
							 @Size(max = 11)
							 String cpf,
							 @NotBlank
							 @Size(max = 100)
							 String firstName,
							 @NotBlank
							 @Size(max = 100)
							 String lastName,
							 @NotNull
							 @Temporal(TemporalType.DATE)
							 @JsonFormat(shape = JsonFormat.Shape.STRING,
									 pattern = Config.DATE_FORMAT,
									 locale = Config.LOCALIZATION,
									 timezone = Config.TIME_ZONE)
							 LocalDate dateOfBirth,
							 @Nullable
							 @Email(regexp = Config.EMAIL_REGEXP_FORMAT, flags = Pattern.Flag.CASE_INSENSITIVE)
							 String email) {
}
