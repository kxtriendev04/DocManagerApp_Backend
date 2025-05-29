package com.vn.document.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("userDetailsService")
public class UserDetailCustom implements UserDetailsService {
    @Autowired
    private  UserService userService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO Auto-generated method stub
        com.vn.document.domain.User user = this.userService.handleGetUserByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("Username/Mật khẩu không hợp lệ");
        }
        return new User(
            user.getEmail(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        // throw new UnsupportedOperationException("Unimplemented method 'loadUserByUsername'");
    }
}
