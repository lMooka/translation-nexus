package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.domain.model.UserModel;
import dev.mooka.translationnexus.domain.entity.UserEntity;
import dev.mooka.translationnexus.repository.UserRepository;
import dev.mooka.translationnexus.resource.dto.UserCreateDTO;
import dev.mooka.translationnexus.resource.dto.UserDTO;
import dev.mooka.translationnexus.resource.dto.UserUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.UserAlreadyExistsException;
import dev.mooka.translationnexus.exception.impl.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MapperService mapperService;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapperService::toDTO)
                .toList();
    }

    public UserDTO createUser(UserCreateDTO dto) throws BusinessException {
        if (userRepository.findByUsername(dto.username()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        List<String> upperRoles = (dto.roles() == null) ? List.of() : dto.roles().stream()
                .map(String::toUpperCase)
                .toList();

        UserModel model = UserModel.builder()
                .username(dto.username())
                .passwordHash(passwordEncoder.encode(dto.password()))
                .roles(upperRoles)
                .allowedLocales(dto.allowedLocales())
                .build();

        UserEntity user = mapperService.toEntity(model);
        return mapperService.toDTO(userRepository.save(user));
    }

    public UserDTO updateUser(String id, UserUpdateDTO dto) throws BusinessException {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);

        UserModel model = mapperService.toModel(entity);

        List<String> upperRoles = (dto.roles() == null) ? List.of() : dto.roles().stream()
                .map(String::toUpperCase)
                .toList();

        model.setRoles(upperRoles);
        model.setAllowedLocales(dto.allowedLocales());

        UserEntity user = mapperService.toEntity(model);
        return mapperService.toDTO(userRepository.save(user));
    }

    public void deleteUser(String id) throws BusinessException {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException();
        }
        userRepository.deleteById(id);
    }
}
