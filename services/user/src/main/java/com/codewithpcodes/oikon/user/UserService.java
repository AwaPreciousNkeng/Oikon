package com.codewithpcodes.oikon.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    public UUID createUser(UserCreationRequest request) {
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .createdAt(Instant.now())
                .phoneNumberVerified(false)
                .build();

        return repository.save(user).getId();
    }

    public UserResponse updateUser(UpdateUserRequest request) {
        User user = repository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User with ID::" + request.userId() + " not found"));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setEmail(request.email());

        return mapper.toResponse(repository.save(user));
    }

    public UserResponse getUserById(UUID userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID::" + userId + " not found"));
        return mapper.toResponse(user);
    }
}
