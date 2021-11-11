package com.bipro.ths.service;




import com.bipro.ths.repository.RoleRepository;
import com.bipro.ths.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void save(Role role) {
    		roleRepository.save(role);
    }


	@Override
	public List<Role> findAll() {
		return roleRepository.findAll();
	}

    @Override
    public Role findAllByName(String name) {
        return roleRepository.findAllByName(name);
    }


}
