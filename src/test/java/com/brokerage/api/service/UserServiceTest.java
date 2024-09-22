package com.brokerage.api.service;

import com.brokerage.api.entity.User;
import com.brokerage.api.enumeration.Role;
import com.brokerage.api.exception.ResourceNotFoundException;
import com.brokerage.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    private User user;
    private UUID userId;
    private String email;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        email = "test@example.com";

        user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(Role.CUSTOMER);
    }

    @Test
    public void testGetByEmail_UserExists() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User foundUser = userService.getByEmail(email);

        assertNotNull(foundUser);
        assertEquals(email, foundUser.getEmail());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    public void testGetByEmail_UserNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> userService.getByEmail(email));
        assertEquals("User not found with Email: '" + email + "'", exception.getMessage());
    }

    @Test
    public void testUserExists_UserExists() {
        when(userRepository.existsById(userId)).thenReturn(true);

        boolean exists = userService.userExists(userId);

        assertTrue(exists);
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    public void testUserExists_UserDoesNotExist() {
        when(userRepository.existsById(userId)).thenReturn(false);

        boolean exists = userService.userExists(userId);

        assertFalse(exists);
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    public void testLoadUserByUsername_UserExists() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("ROLE_CUSTOMER", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    public void testLoadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> userService.loadUserByUsername(email));
        assertEquals("User not found with Email: '" + email + "'", exception.getMessage());
    }
}