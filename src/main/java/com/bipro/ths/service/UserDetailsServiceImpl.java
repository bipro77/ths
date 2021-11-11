package com.bipro.ths.service;

import com.bipro.ths.model.Role;
import com.bipro.ths.model.User;
import com.bipro.ths.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;
import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;



//    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException, DataAccessException {
//        // returns the get(0) of the user list obtained from the db
//        User user = userRepository.findByUsername(name);
//
//
//        Set<Role> roles = user.getRoles();
////        logger.debug("role of the user" + roles);
//
//        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
//        for(Role role: roles){
//            authorities.add(new SimpleGrantedAuthority(role.getName()));
////            logger.debug("role" + role + " role.getRole()" + (role.getRole()));
//        }
//
//        UserDetails userDetails= new UserDetails();
////        userDetails.setUser(user);
////        userDetails.setAuthorities(authorities);
//
//        return userDetails;
//
//    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
//        User user = userRepository.findByEmail(username);
        if(user == null){
            throw new UsernameNotFoundException("User not authorized.");
        }

        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (Role role : user.getRoles()){
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
        }

//        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), grantedAuthorities);
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), user.isAccountEnabled(), true, true, true, grantedAuthorities);
    }
}