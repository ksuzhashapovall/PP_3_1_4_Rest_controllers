package ru.kata.spring.boot_security.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dto.UserDto;
import ru.kata.spring.boot_security.demo.dto.UserResponseDto;
import ru.kata.spring.boot_security.demo.mapper.UserMapper;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           RoleService roleService,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public User add(UserDto userDto) {
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Set<Role> roleSet = new HashSet<>();
        if (userDto.getRoles() != null && !userDto.getRoles().isEmpty()) {
            roleSet.addAll(userDto.getRoles());
        } else {
            Role userRole = roleService.findByName("ROLE_USER");
            if (userRole != null) {
                roleSet.add(userRole);
            }
        }
        userDto.setRoles(roleSet);

        User user = userMapper.toEntity(userDto);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(Long id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setAge(userDto.getAge());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setUsername(userDto.getEmail());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            if (!userDto.getPassword().startsWith("$2a$")) {
                existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            } else {
                existingUser.setPassword(userDto.getPassword());
            }
        }

        if (userDto.getRoles() != null) {
            existingUser.setRoles(userDto.getRoles());
        }

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            user = userRepository.findByUsername(username);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllResponseDto() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getAge(),
                        user.getEmail(),
                        user.getRoles()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserResponseDtoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getAge(),
                user.getEmail(),
                user.getRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUserResponseDto(String username) {
        User currentUser = findByUsername(username);
        if (currentUser == null) {
            throw new RuntimeException("Current user not found");
        }
        return new UserResponseDto(
                currentUser.getId(),
                currentUser.getFirstName(),
                currentUser.getLastName(),
                currentUser.getAge(),
                currentUser.getEmail(),
                currentUser.getRoles());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== Loading user: " + username + " ===");

        User user = userRepository.findByEmail(username);
        if (user == null) {
            user = userRepository.findByUsername(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found with username/email: " + username);
        }

        user.getRoles().size();

        System.out.println("User found: " + user.getEmail());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Roles: " + user.getRoles());

        return user;
    }
}