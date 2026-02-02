package dorm.dao;

import dorm.model.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class MySqlMessageRepository implements MessageRepository {
    
    @Override
    public List<Message> findByUser(String username) {
        String sql = "SELECT * FROM messages WHERE fromUser = ? OR toUser = ? ORDER BY sentAt DESC";
        List<Message> messages = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(resultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding messages for user: " + username, e);
        }
        
        return messages;
    }
    
    @Override
    public void save(Message message) {
        String sql = "INSERT INTO messages (id, fromUser, toUser, content, sentAt, isRead) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, message.getId());
            stmt.setString(2, message.getFromUser());
            stmt.setString(3, message.getToUser());
            stmt.setString(4, message.getContent());
            stmt.setTimestamp(5, Timestamp.valueOf(message.getSentAt()));
            stmt.setBoolean(6, message.isRead());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving message: " + message.getId(), e);
        }
    }
    
    @Override
    public void update(Message message) {
        String sql = "UPDATE messages SET isRead = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, message.isRead());
            stmt.setString(2, message.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating message: " + message.getId(), e);
        }
    }
    
    //Convert ResultSet row to Message object

    private Message resultSetToMessage(ResultSet rs) throws SQLException {
        LocalDateTime sentAt;
        Timestamp ts = rs.getTimestamp("sentAt");
        if (ts != null) {
            sentAt = ts.toLocalDateTime();
        } else {
            sentAt = LocalDateTime.now();
        }
        
        return new Message(
            rs.getString("id"),
            rs.getString("fromUser"),
            rs.getString("toUser"),
            rs.getString("content"),
            sentAt,
            rs.getBoolean("isRead")
        );
    }
}
