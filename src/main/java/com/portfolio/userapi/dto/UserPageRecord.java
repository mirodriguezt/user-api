package com.portfolio.userapi.dto;

import com.portfolio.userapi.entity.UserEntity;

import java.util.List;

public record UserPageRecord(int actualPage,
							 long totalRecords,
							 int totalPages,
							 List<UserEntity> userList) {
}
