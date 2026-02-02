package dorm.dao;

import dorm.model.BuildingAssignment;
import dorm.model.User;
import java.util.List;
import java.util.Optional;

public interface BuildingAssignmentRepository {
    
    //Find all building assignments
    List<BuildingAssignment> findAll();

     //Find building assignment by proctor
    Optional<BuildingAssignment> findByProctor(User proctor);
    
    
    //Save or update a building assignment
    void save(User proctor, String buildingName);
    
    //Delete assignment for a proctor
    void deleteByProctor(User proctor);
}
