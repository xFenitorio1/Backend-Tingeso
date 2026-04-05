package org.example.tingesoback.service;

import org.example.tingesoback.entity.User;
import org.example.tingesoback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setFullName("Juan Perez");
        mockUser.setEmail("juan@test.com");
        mockUser.setActive(true);
    }

    @Test
    void createUser_Success() {
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        User result = userService.createUser(new User());
        assertNotNull(result);
        assertEquals("Juan Perez", result.getFullName());
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(mockUser));
        List<User> result = userService.getAllUsers();
        assertEquals(1, result.size());
    }

    @Test
    void getUserById_Found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        Optional<User> result = userService.getUserById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<User> result = userService.getUserById(99L);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateUser_Success() {
        User details = new User();
        details.setFullName("Juan Actualizado");
        details.setEmail("nuevo@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        User result = userService.updateUser(1L, details);

        assertEquals("Juan Actualizado", result.getFullName());
        assertEquals("nuevo@test.com", result.getEmail());
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, new User()));
    }

    @Test
    void softDeleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        userService.softDeleteUser(1L);

        assertFalse(mockUser.isActive()); // Verificamos el cambio de estado
        verify(userRepository).save(mockUser);
    }

    @Test
    void deleteUser_Success() {
        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void findByEmail_Found() {
        when(userRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(mockUser));
        Optional<User> result = userService.findByEmail("juan@test.com");
        assertTrue(result.isPresent());
        assertEquals("juan@test.com", result.get().getEmail());
    }
}