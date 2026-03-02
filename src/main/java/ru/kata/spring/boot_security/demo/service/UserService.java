package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.dto.UserDto;
import ru.kata.spring.boot_security.demo.dto.UserResponseDto;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

public interface UserService {
    User add(UserDto userDto);
    User update(Long id, UserDto userDto);
    void delete(Long id);
    User getById(Long id);
    User findByUsername(String username);
    List<User> getAll();
    List<UserResponseDto> getAllResponseDto();
    UserResponseDto getUserResponseDtoById(Long id);
    UserResponseDto getCurrentUserResponseDto(String username);
}