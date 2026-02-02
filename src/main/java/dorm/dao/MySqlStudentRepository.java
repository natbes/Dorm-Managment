package dorm.dao;

import dorm.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class MySqlStudentRepository implements StudentRepository {
    
    @Override
    public Optional<Student> findByStudentId(String studentId) {
        String sql = "SELECT * FROM Student WHERE studentId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(resultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding student by studentId: " + studentId, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM Student";
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                students.add(resultSetToStudent(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all students", e);
        }
        
        return students;
    }
    
    @Override
    public List<Student> findByBuilding(String buildingName) {
        String sql = "SELECT * FROM Student WHERE assignedBuilding = ?";
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, buildingName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(resultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding students by building: " + buildingName, e);
        }
        
        return students;
    }
    
    @Override
    public void save(Student student) {
        String sql = """
            INSERT INTO Student (id, username, password, role, displayName, studentId, gender, college,
                residency, city, subcity, woreda, sponsorshipType, disabilityInfo,
                emergencyContactName, emergencyContactPhone, transactionId, assignedBuilding)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setStudentParameters(stmt, student);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving student: " + student.getStudentId(), e);
        }
    }
    
    @Override
    public void update(Student student) {
        String sql = """
            UPDATE Student SET
                username = ?, password = ?, role = ?, displayName = ?, studentId = ?,
                gender = ?, college = ?, residency = ?, city = ?, subcity = ?, woreda = ?,
                sponsorshipType = ?, disabilityInfo = ?, emergencyContactName = ?,
                emergencyContactPhone = ?, transactionId = ?, assignedBuilding = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getUsername());
            stmt.setString(2, student.getPassword());
            stmt.setString(3, student.getRole().name());
            stmt.setString(4, student.getDisplayName());
            stmt.setString(5, student.getStudentId());
            stmt.setString(6, student.getGender() != null ? student.getGender().name() : null);
            stmt.setString(7, student.getCollege() != null ? student.getCollege().name() : null);
            stmt.setString(8, student.getResidency() != null ? student.getResidency().name() : null);
            stmt.setString(9, student.getCity());
            stmt.setString(10, student.getSubcity());
            stmt.setString(11, student.getWoreda());
            stmt.setString(12, student.getSponsorshipType() != null ? student.getSponsorshipType().name() : null);
            stmt.setString(13, student.getDisabilityInfo());
            stmt.setString(14, student.getEmergencyContactName());
            stmt.setString(15, student.getEmergencyContactPhone());
            stmt.setString(16, student.getTransactionId());
            stmt.setString(17, student.getAssignedBuilding());
            stmt.setString(18, student.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating student: " + student.getStudentId(), e);
        }
    }
    
    //Set PreparedStatement parameters for student insert

    private void setStudentParameters(PreparedStatement stmt, Student student) throws SQLException {
        stmt.setString(1, student.getId());
        stmt.setString(2, student.getUsername());
        stmt.setString(3, student.getPassword());
        stmt.setString(4, student.getRole().name());
        stmt.setString(5, student.getDisplayName());
        stmt.setString(6, student.getStudentId());
        stmt.setString(7, student.getGender() != null ? student.getGender().name() : null);
        stmt.setString(8, student.getCollege() != null ? student.getCollege().name() : null);
        stmt.setString(9, student.getResidency() != null ? student.getResidency().name() : null);
        stmt.setString(10, student.getCity());
        stmt.setString(11, student.getSubcity());
        stmt.setString(12, student.getWoreda());
        stmt.setString(13, student.getSponsorshipType() != null ? student.getSponsorshipType().name() : null);
        stmt.setString(14, student.getDisabilityInfo());
        stmt.setString(15, student.getEmergencyContactName());
        stmt.setString(16, student.getEmergencyContactPhone());
        stmt.setString(17, student.getTransactionId());
        stmt.setString(18, student.getAssignedBuilding());
    }
    
    //Convert ResultSet row to Student object

    private Student resultSetToStudent(ResultSet rs) throws SQLException {
        Gender gender = null;
        String genderStr = rs.getString("gender");
        if (genderStr != null && !genderStr.isEmpty()) {
            try {
                gender = Gender.valueOf(genderStr);
            } catch (IllegalArgumentException e) {
                gender = Gender.MALE;
            }
        }
        
        College college = null;
        String collegeStr = rs.getString("college");
        if (collegeStr != null && !collegeStr.isEmpty()) {
            try {
                college = College.valueOf(collegeStr);
            } catch (IllegalArgumentException e) {
                // Ignore invalid college
            }
        }
        
        Student student = new Student(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("displayName"),
            rs.getString("studentId"),
            gender,
            college
        );
        
        // Set optional fields
        String residencyStr = rs.getString("residency");
        if (residencyStr != null && !residencyStr.isEmpty()) {
            try {
                student.setResidency(Residency.valueOf(residencyStr));
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        
        student.setCity(rs.getString("city"));
        student.setSubcity(rs.getString("subcity"));
        student.setWoreda(rs.getString("woreda"));
        
        String sponsorshipStr = rs.getString("sponsorshipType");
        if (sponsorshipStr != null && !sponsorshipStr.isEmpty()) {
            try {
                student.setSponsorshipType(SponsorshipType.valueOf(sponsorshipStr));
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        
        student.setDisabilityInfo(rs.getString("disabilityInfo"));
        student.setEmergencyContactName(rs.getString("emergencyContactName"));
        student.setEmergencyContactPhone(rs.getString("emergencyContactPhone"));
        student.setTransactionId(rs.getString("transactionId"));
        
        String assignedBuilding = rs.getString("assignedBuilding");
        if (assignedBuilding != null) {
            student.setAssignedBuilding(assignedBuilding);
        }
        
        return student;
    }
}
