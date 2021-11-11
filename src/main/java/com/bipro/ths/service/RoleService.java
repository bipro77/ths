package com.bipro.ths.service;

import com.bipro.ths.model.Role;

import java.util.List;



public interface RoleService {
    void save(Role role);
    
    List<Role>findAll();

    Role findAllByName(String Name);
}
