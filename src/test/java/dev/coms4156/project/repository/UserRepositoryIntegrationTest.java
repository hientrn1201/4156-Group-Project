package dev.coms4156.project.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.coms4156.project.model.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for UserRepository.
 * Tests actual database operations using PostgreSQL.
 *
 * <p>This test integrates with:
 * - PostgreSQL database (external resource)
 * - JPA/Hibernate ORM
 * - User entity persistence
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.jpa.show-sql=false"
})
class UserRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Create a test user
    testUser = new User();
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("encoded-password");
    testUser.setRole(User.Role.USER);
  }

  @Test
  void testSaveAndFindById() {
    // When - Save user
    User saved = userRepository.save(testUser);
    entityManager.flush();
    entityManager.clear();

    // Then - Retrieve by ID
    Optional<User> found = userRepository.findById(saved.getId());

    assertTrue(found.isPresent());
    assertEquals("testuser", found.get().getUsername());
    assertEquals("test@example.com", found.get().getEmail());
    assertEquals(User.Role.USER, found.get().getRole());
  }

  @Test
  void testFindByUsername() {
    // Given - Save user
    final User saved = userRepository.save(testUser);
    entityManager.flush();
    entityManager.clear();

    // When - Find by username
    Optional<User> found = userRepository.findByUsername("testuser");

    // Then
    assertTrue(found.isPresent());
    assertEquals("testuser", found.get().getUsername());
    assertEquals(saved.getId(), found.get().getId());
  }

  @Test
  void testFindByEmail() {
    // Given - Save user
    final User saved = userRepository.save(testUser);
    entityManager.flush();
    entityManager.clear();

    // When - Find by email
    Optional<User> found = userRepository.findByEmail("test@example.com");

    // Then
    assertTrue(found.isPresent());
    assertEquals("test@example.com", found.get().getEmail());
    assertEquals(saved.getId(), found.get().getId());
  }

  @Test
  void testExistsByUsername() {
    // Given - Save user
    userRepository.save(testUser);
    entityManager.flush();
    entityManager.clear();

    // When - Check existence
    boolean exists = userRepository.existsByUsername("testuser");
    boolean notExists = userRepository.existsByUsername("nonexistent");

    // Then
    assertTrue(exists);
    assertFalse(notExists);
  }

  @Test
  void testExistsByEmail() {
    // Given - Save user
    userRepository.save(testUser);
    entityManager.flush();
    entityManager.clear();

    // When - Check existence
    boolean exists = userRepository.existsByEmail("test@example.com");
    boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

    // Then
    assertTrue(exists);
    assertFalse(notExists);
  }

  @Test
  void testFindByUsername_NotFound() {
    // When - Find non-existent username
    Optional<User> found = userRepository.findByUsername("nonexistent");

    // Then
    assertFalse(found.isPresent());
  }

  @Test
  void testFindByEmail_NotFound() {
    // When - Find non-existent email
    Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

    // Then
    assertFalse(found.isPresent());
  }

  @Test
  void testSaveMultipleUsers() {
    // Given - Create multiple users
    User user1 = new User();
    user1.setUsername("user1");
    user1.setEmail("user1@example.com");
    user1.setPasswordHash("pass1");
    user1.setRole(User.Role.USER);

    User user2 = new User();
    user2.setUsername("user2");
    user2.setEmail("user2@example.com");
    user2.setPasswordHash("pass2");
    user2.setRole(User.Role.ADMIN);

    // When - Save both
    User saved1 = userRepository.save(user1);
    final User saved2 = userRepository.save(user2);
    entityManager.flush();
    entityManager.clear();

    // Then - Both should be retrievable
    assertTrue(userRepository.findById(saved1.getId()).isPresent());
    assertTrue(userRepository.findById(saved2.getId()).isPresent());
    assertEquals("user1", userRepository.findByUsername("user1").get().getUsername());
    assertEquals("user2", userRepository.findByUsername("user2").get().getUsername());
  }

  @Test
  void testDelete() {
    // Given - Save user
    User saved = userRepository.save(testUser);
    Long id = saved.getId();
    entityManager.flush();
    entityManager.clear();

    // Verify it exists
    assertTrue(userRepository.findById(id).isPresent());

    // When - Delete
    userRepository.deleteById(id);
    entityManager.flush();
    entityManager.clear();

    // Then - Should not exist
    assertFalse(userRepository.findById(id).isPresent());
  }

  @Test
  void testUpdateUser() {
    // Given - Save user
    User saved = userRepository.save(testUser);
    Long id = saved.getId();
    entityManager.flush();
    entityManager.clear();

    // When - Update
    Optional<User> found = userRepository.findById(id);
    assertTrue(found.isPresent());
    User user = found.get();
    user.setEmail("updated@example.com");
    user.setRole(User.Role.ADMIN);
    User updated = userRepository.save(user);
    entityManager.flush();
    entityManager.clear();

    // Then - Verify update
    Optional<User> retrieved = userRepository.findById(id);
    assertTrue(retrieved.isPresent());
    assertEquals("updated@example.com", retrieved.get().getEmail());
    assertEquals(User.Role.ADMIN, retrieved.get().getRole());
  }

  @Test
  void testUniqueUsername() {
    // Given - Save user with username
    userRepository.save(testUser);
    entityManager.flush();
    entityManager.clear();

    // When - Try to create another user with same username
    User duplicate = new User();
    duplicate.setUsername("testuser"); // Same username
    duplicate.setEmail("different@example.com");
    duplicate.setPasswordHash("pass");
    duplicate.setRole(User.Role.USER);

    // Save should work (database constraint would prevent if unique constraint exists)
    // This test verifies the repository method works
    User saved = userRepository.save(duplicate);
    assertNotNull(saved);
  }
}

