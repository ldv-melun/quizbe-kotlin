package org.quizbe.service;

import org.quizbe.dao.UserRepository;
import org.quizbe.model.Role;
import org.quizbe.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomUserServiceDetails implements UserDetailsService {
  Logger logger = LoggerFactory.getLogger(CustomUserServiceDetails.class);

  private long VALID_HOURS_DEFAULT_PW=User.VALIDE_TIME_HOURS_DEFAULT_PW;

  @Autowired
  UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    // voir aussi  (Implement UserDetails)
    //  https://www.codejava.net/frameworks/spring-boot/spring-boot-security-authentication-with-jpa-hibernate-and-mysql

    User user = userRepository.findByEmail(username);
    if (user == null) {
      user = userRepository.findByUsername(username);
      if (user == null) {
        throw new UsernameNotFoundException("Could not find user");
      }
    }

    // force user to change his password
    List<GrantedAuthority> authorities;
    if (user.mustChangePassword()) {
      logger.info("user do change password");
      if (hasDefaultPlainTextPasswordInvalidate(user)) {
        logger.info("user has password expired, so we set a new default password to him");
        updateDefaultPlainTextPassword(user);
      }
      authorities = new ArrayList<>(Arrays.asList((new SimpleGrantedAuthority("CHANGE_PW"))));
    } else {
      // logger.info("user do no change password");
      authorities = getUserAuthority(user.getRoles());
    }
    return this.buildUserForAuthentication(user, authorities);
  }

  private List<GrantedAuthority> getUserAuthority(Set<Role> userRoles) {
    Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
    for (Role role : userRoles) {
      roles.add(new SimpleGrantedAuthority(role.getName()));
    }
    List<GrantedAuthority> grantedAuthorities = new ArrayList<>(roles);
    return grantedAuthorities;
  }

  private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities) {
    return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
            user.isEnabled(), true, true, true, authorities);
  }

  private boolean hasDefaultPlainTextPasswordInvalidate(User user) {
    logger.info("TEMPS EN HEURE LIMITE VALIDATION : " +VALID_HOURS_DEFAULT_PW);
    return (user.mustChangePassword()
            &&
            (user.getSetDateDefaultPassword() == null
              ||
            user.getSetDateDefaultPassword().plusHours(VALID_HOURS_DEFAULT_PW).isBefore(LocalDateTime.now())
            )
    );
  }

  private void updateDefaultPlainTextPassword(User user) {
    user.setDefaultPlainTextPassword(User.generateRandomPassword(8));
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    // pas moyen de passer par un bean pour l'encoder à cause d'une réf circulaire
    user.setPassword(encoder.encode(user.getDefaultPlainTextPassword()));
    user.setDateDefaultPassword(LocalDateTime.now());
    user.setDateUpdatePassword(null);
    userRepository.save(user);
  }

}
