package org.quizbe.service;

import org.quizbe.dao.RoleRepository;
import org.quizbe.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    public Role findByName(String roleName){
        return roleRepository.findByName(roleName);
    }

    public List<Role> findAllByOrderByName() {
        return roleRepository.findAllByOrderByName();
    }

  public void saveRole(Role role) {
        roleRepository.save(role);
  }
}
