package dev.mooka.translationnexus.service;

import dev.mooka.translationnexus.domain.entity.UserEntity;
import dev.mooka.translationnexus.repository.UserRepository;
import dev.mooka.translationnexus.security.JwtTokenProvider;
import dev.mooka.translationnexus.resource.dto.LoginRequest;
import dev.mooka.translationnexus.resource.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.mooka.translationnexus.exception.BusinessException;
import dev.mooka.translationnexus.exception.impl.InvalidCredentialsException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) throws BusinessException {
        UserEntity user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRoles(), user.getAllowedLocales());
        return new LoginResponse(token, user.getRoles(), user.getUsername(), user.getAllowedLocales());
    }
}
