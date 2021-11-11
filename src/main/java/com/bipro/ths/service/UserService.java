package com.bipro.ths.service;

import com.bipro.ths.model.Role;
import com.bipro.ths.model.User;

import java.util.List;

public interface UserService {
    void save(User user);

    User findByUsername(String username);

    User findByEmail(String email);

    List<String> findAllUsernames();

    User findAllById(Integer parseInt);

    List<User> findAllByRoles(Role role);

    void delete(User user);
}