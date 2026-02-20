package com.car_rental.service;

import com.car_rental.dao.DataAccessException;
import com.car_rental.dao.daointerface.UserDAO;
import com.car_rental.entity.Log;
import com.car_rental.entity.PageResult;
import com.car_rental.entity.Role;
import com.car_rental.entity.User;
import com.car_rental.form.user.UserPasswordDTO;
import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service implementation for user management operations
 */
@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;
    private final CurrentUserService currentUserService;
    private final UtilityService utilityService = new UtilityService();

    /**
     * Constructor with dependency injection
     */
    @Autowired
    public UserServiceImpl(UserDAO userDAO, PasswordEncoder passwordEncoder, LogService logService,
                           CurrentUserService currentUserService) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.logService = logService;
        this.currentUserService = currentUserService;
    }

    /**
     * Creates a new user with the given details
     *
     * @param user User object containing user details
     * @throws DataAccessException if user creation fails
     */
    @Override
    public void createUser(User user) {
        try {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            userDAO.createUser(user);
            logService.logEvent(currentUserService.getCurrentUserId(), Log.AuditEventType.CREATE_USER,
                                "Created user: " + user.getUsername());
        } catch (Exception e) {
            String errorMessage = "Failed to create user: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Registers a new client user
     *
     * @param user User object to be registered as client
     */
    public void registerClient(User user) {
        user.setRole(getRoleByName("CLIENT"));
        createUser(user);
    }

    /**
     * Updates existing user information
     *
     * @param oldUser User object containing original data
     * @param newUser User object containing updated data
     * @throws DataAccessException if update fails
     */
    @Override
    public void updateUser(User oldUser, User newUser) {
        try {
            userDAO.updateUser(oldUser, newUser);
            logService.logEvent(currentUserService.getCurrentUserId(), Log.AuditEventType.UPDATE_USER,
                                "Updated user: " + oldUser.getUsername());
        } catch (Exception e) {
            String errorMessage = "Failed to update user: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Updates user's password
     *
     * @param userId      ID of the user to update
     * @param passwordDTO Object containing old and new passwords
     * @throws DataAccessException if password update fails
     */
    @Override
    public void updateUserPassword(int userId, UserPasswordDTO passwordDTO) {
        try {
            User user = getUserById(userId);
            if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
                throw new DataAccessException("Incorrect old password");
            }

            String encodedNewPassword = passwordEncoder.encode(passwordDTO.getNewPassword());
            userDAO.updateUserPassword(userId, encodedNewPassword);
            logService.logEvent(currentUserService.getCurrentUserId(), Log.AuditEventType.UPDATE_USER,
                                "Updated password for user: " + user.getUsername());
        } catch (DataAccessException e) {
            log.error("Password update failed for user ID {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            String errorMessage = "Failed to update password: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Deletes a user by ID
     *
     * @param userId ID of the user to delete
     * @throws DataAccessException if deletion fails
     */
    @Override
    public void deleteUser(int userId) {
        try {
            userDAO.deleteUser(userId);
            logService.logEvent(currentUserService.getCurrentUserId(), Log.AuditEventType.DELETE_USER,
                                "Deleted user with ID: " + userId);
        } catch (Exception e) {
            String errorMessage = "Failed to delete user: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves a user by username
     *
     * @param username Username to search for
     * @return User object if found, null otherwise
     */
    @Override
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    /**
     * Retrieves a user by ID
     *
     * @param userId ID of the user to retrieve
     * @return User object if found, null otherwise
     */
    @Override
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    /**
     * Retrieves a paginated list of users
     *
     * @param roleId   Role ID filter
     * @param userId   User ID filter
     * @param username Username filter
     * @param lastName Last name filter
     * @param page     Page number
     * @param pageSize Number of items per page
     * @param sortBy   Field to sort by
     * @return PageResult containing users
     * @throws DataAccessException if retrieval fails
     */
    @Override
    public PageResult<User> getUsersPage(Integer roleId, Integer userId, String username, String lastName, int page,
                                         int pageSize, String sortBy) {
        try {
            return userDAO.getUsersPage(roleId, userId, username, lastName, page, pageSize, sortBy);
        } catch (Exception e) {
            String errorMessage = "Failed to retrieve users page: " + e.getMessage();
            log.error(errorMessage, e);
            throw new DataAccessException(utilityService.handleException(e));
        }
    }

    /**
     * Retrieves a role by ID
     *
     * @param roleId ID of the role to retrieve
     * @return Role object if found, null otherwise
     */
    @Override
    public Role getRoleById(int roleId) {
        return userDAO.getRoleById(roleId);
    }

    /**
     * Retrieves a role by name
     *
     * @param roleName Name of the role to retrieve
     * @return Role object if found, null otherwise
     */
    @Override
    public Role getRoleByName(String roleName) {
        return userDAO.getRoleByName(roleName);
    }

    /**
     * Retrieves all roles
     *
     * @return List of all roles
     */
    @Override
    public List<Role> getAllRoles() {
        return userDAO.getAllRoles();
    }

    /**
     * Initializes default users and roles
     */
    @PostConstruct
    public void init() {
        initUser("sadmin", "sadmin123", "Super Admin");
        initUser("admin", "admin123", "Admin");
        initUser("client", "client123", "Client");
        initUser("client2", "client123", "Client");
        initUser("client3", "client123", "Client");
        initUser("client4", "client123", "Client");
        initUser("fleet_manager", "fleet_manager123", "Fleet Manager");
    }

    /**
     * Initializes or updates a user with default credentials
     *
     * @param username    Username for the user
     * @param rawPassword Raw password to be encoded
     * @param roleName    Name of the role to assign
     */
    private void initUser(String username, String rawPassword, String roleName) {
        User user = getUserByUsername(username);
        if (user == null) {
            log.warn("User {} not found during initialization", username);
            return;
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        userDAO.updateUserPassword(user.getId(), encodedPassword);
        log.info("{} account created/upgraded", roleName);
    }
}
