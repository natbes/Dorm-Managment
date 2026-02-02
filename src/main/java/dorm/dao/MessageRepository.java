package dorm.dao;

import dorm.model.Message;

import java.util.List;

public interface MessageRepository {
    //Find all messages for a specific user (sent or received)
    List<Message> findByUser(String username);
    
    //Save a new message
    void save(Message message);
    
    //Update an existing message (e.g., mark as read)
    void update(Message message);
}
