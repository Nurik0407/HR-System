package com.example.border.service;

import com.example.border.model.entity.User;

import java.util.UUID;

public interface UserService {

    User findUserById(UUID userId);
}
