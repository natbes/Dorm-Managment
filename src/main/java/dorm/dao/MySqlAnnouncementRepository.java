package dorm.dao;

import dorm.model.Announcement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class MySqlAnnouncementRepository implements AnnouncementRepository {
    
    @Override
    public List<Announcement> findAll() {
        String sql = "SELECT * FROM announcements ORDER BY createdAt DESC";
        List<Announcement> announcements = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                announcements.add(resultSetToAnnouncement(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all announcements", e);
        }
        
        return announcements;
    }
    
    @Override
    public void save(Announcement announcement) {
        String sql = "INSERT INTO announcements (id, title, body, createdBy, createdAt) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, announcement.getId());
            stmt.setString(2, announcement.getTitle());
            stmt.setString(3, announcement.getBody());
            stmt.setString(4, announcement.getCreatedBy());
            stmt.setTimestamp(5, Timestamp.valueOf(announcement.getCreatedAt()));
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving announcement: " + announcement.getId(), e);
        }
    }
    
    @Override
    public void update(Announcement announcement) {
        String sql = "UPDATE announcements SET title = ?, body = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, announcement.getTitle());
            stmt.setString(2, announcement.getBody());
            stmt.setString(3, announcement.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating announcement: " + announcement.getId(), e);
        }
    }
    
    @Override
    public void delete(Announcement announcement) {
        String sql = "DELETE FROM announcements WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, announcement.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting announcement: " + announcement.getId(), e);
        }
    }
    
    //Convert ResultSet row to Announcement object

    private Announcement resultSetToAnnouncement(ResultSet rs) throws SQLException {
        LocalDateTime createdAt;
        Timestamp ts = rs.getTimestamp("createdAt");
        if (ts != null) {
            createdAt = ts.toLocalDateTime();
        } else {
            createdAt = LocalDateTime.now();
        }
        
        return new Announcement(
            rs.getString("id"),
            rs.getString("title"),
            rs.getString("body"),
            rs.getString("createdBy"),
            createdAt
        );
    }
}
