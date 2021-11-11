package com.bipro.ths.repository;

import com.bipro.ths.model.Role;
import com.bipro.ths.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByUsername(String name);
    void delete(User user);
    User findAllById(Integer id);

    @Query("SELECT username FROM User")
    List<String> findAllUsernames();

    @Query("SELECT email FROM User")
    List<String> findAllEmail();

//    @Query( "select u from User u inner join u.roles r where r.role in :roles" )
//    List<User> findBySpecificRoles(@Param("roles") List<Role> roles);

//    @Query("SELECT u FROM User u WHERE u.roles = 1")
    List<User> findAllByRoles(Role role);


}