package com.bipro.ths.service;

import com.bipro.ths.model.User;

public interface SecurityService {
    String findLoggedInUsername();
    User findLoggedInUser();
    void autologin(String username, String password);
    boolean isUserAuthenticated();
}