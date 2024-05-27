package com.portfolio.userapi.config;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import java.time.format.DateTimeFormatter;

@OpenAPIDefinition(info=@Info(title="Users API"))
public class Config {
	public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String EMAIL_REGEXP_FORMAT = "(^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.(?:[a-zA-Z]{2}|com|org|net|edu|gov|mil|biz|info|mobi|name|aero|asia|jobs|museum)$)";
	public static final String LOCALIZATION = "pt-BR";
	public static final String TIME_ZONE = "Brazil/East";
	public static LocalDateTimeSerializer LOCAL_DATETIME_SERIALIZER =
			new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
	public static final byte ALLOWED_AGE_USERS_REGISTRATION = 19;
}
