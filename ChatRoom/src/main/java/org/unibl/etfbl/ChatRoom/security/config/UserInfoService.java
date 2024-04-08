package org.unibl.etfbl.ChatRoom.security.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.unibl.etfbl.ChatRoom.exceptions.NotFoundException;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;

import java.util.Optional;

@Component
@Transactional
public class UserInfoService implements UserDetailsService {
    @Autowired
    private UserEntityRepository userEntityRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        return userEntityRepository.getUserEntityByUsername(username).orElseThrow(
                () -> new NotFoundException("User with username " + username + " not found")
        );
    }

    public Optional<UserEntity> loadUserByEmail(String email) {
        return userEntityRepository.findByEmail(email);
    }
    public Boolean existsByUsername(String username){
        return userEntityRepository.existsByUsername(username);
    }

    public void saveAndFlush(UserEntity userEntity) {
        userEntityRepository.saveAndFlush(userEntity);
    }
}
