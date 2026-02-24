package ru.kata.spring.boot_security.demo.dto;

import ru.kata.spring.boot_security.demo.model.Role;
import java.util.Set;
import java.util.stream.Collectors;

public class UserResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private int age;
    private String email;
    private Set<RoleDto> roles;

    public UserResponseDto() {}

    public UserResponseDto(Long id, String firstName, String lastName, int age, String email, Set<Role> roles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.email = email;
        this.roles = roles.stream().map(RoleDto::new).collect(Collectors.toSet());
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
    public Set<RoleDto> getRoles() { return roles; }
    public void setRoles(Set<RoleDto> roles) { this.roles = roles; }
}