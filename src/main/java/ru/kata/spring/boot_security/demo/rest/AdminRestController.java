package ru.kata.spring.boot_security.demo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dto.UserDto;
import ru.kata.spring.boot_security.demo.mapper.UserMapper;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public AdminRestController(UserService userService, RoleService roleService,
                               PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/current-user")
    public ResponseEntity<User> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username);
        if (currentUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(currentUser);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAll();
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserDto userDto) {
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
        userService.add(user);

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        User existingUser = userService.getById(id);
        if (existingUser == null) {
            return ResponseEntity.notFound().build();
        }

        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setAge(userDto.getAge());
        existingUser.setEmail(userDto.getEmail());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        if (userDto.getRoles() != null) {
            existingUser.setRoles(userDto.getRoles());
        }

        userService.update(existingUser);
        return ResponseEntity.ok(existingUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/simple")
    public ResponseEntity<List<SimpleUserDto>> getSimpleUsers() {
        List<User> users = userService.getAll();
        List<SimpleUserDto> simpleUsers = users.stream()
                .map(user -> new SimpleUserDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getAge(),
                        user.getEmail(),
                        user.getRoles().stream()
                                .map(role -> role.getName().replace("ROLE_", ""))
                                .collect(Collectors.joining(" "))
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(simpleUsers);
    }

    /**
     * Внутренний DTO класс для упрощенного ответа
     */
    public static class SimpleUserDto {
        private Long id;
        private String firstName;
        private String lastName;
        private int age;
        private String email;
        private String roles;

        public SimpleUserDto() {}

        public SimpleUserDto(Long id, String firstName, String lastName, int age, String email, String roles) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.email = email;
            this.roles = roles;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getRoles() { return roles; }
        public void setRoles(String roles) { this.roles = roles; }
    }
}