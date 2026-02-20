package com.car_rental.convertor;

import com.car_rental.entity.Role;
import com.car_rental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToRoleConverter implements Converter<String, Role> {

    private final UserService roleService;

    @Autowired
    public StringToRoleConverter(UserService roleService) {
        this.roleService = roleService;
    }

    @Override
    public Role convert(@NonNull String source) {
        if (source.isEmpty()) {
            return null;
        }
        int id = Integer.parseInt(source);
        return roleService.getRoleById(id);
    }
}
