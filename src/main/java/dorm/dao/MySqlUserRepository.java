package dorm.dao;

import dorm.model.Role;
import dorm.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class MySqlUserRepository implements UserRepository {
    
    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, role, full_name FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(resultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding user by username: " + username, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<User> findByRole(Role role) {
        String sql = "SELECT id, username, password, role, full_name FROM users WHERE role = ?";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(resultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding users by role: " + role, e);
        }
        
        return users;
    }
    
    @Override
    public List<User> findAll() {
        String sql = "SELECT id, username, password, role, full_name FROM users";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(resultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all users", e);
        }
        
        return users;
    }
    
    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (id, username, password, role, full_name) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getDisplayName());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving user: " + user.getUsername(), e);
        }
    }
    
    @Override
    public void delete(User user) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting user: " + user.getUsername(), e);
        }
    }
    
    //Convert ResultSet row to User object

    private User resultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("password"),
            Role.valueOf(rs.getString("role")),
            rs.getString("full_name")
        );
    }
}
