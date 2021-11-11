package com.bipro.ths.service;

import com.bipro.ths.repository.RoleRepository;
import com.bipro.ths.model.Role;
import com.bipro.ths.model.User;
import com.bipro.ths.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<String> findAllUsernames() {
        return userRepository.findAllUsernames();
    }

    @Override
    public User findAllById(Integer id) {
        return userRepository.findAllById(id);
    }

    @Override
    public List<User> findAllByRoles(Role role) {
        return userRepository.findAllByRoles(role);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

}