package dorm.dao;

import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;
import dorm.model.Student;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class MySqlApplicationRepository implements ApplicationRepository {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final StudentRepository studentRepository;
    
    public MySqlApplicationRepository(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
    
    @Override
    public Optional<DormApplication> findByStudent(Student student) {
        String sql = "SELECT * FROM dorm_applications WHERE studentId = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(resultSetToApplication(rs, student));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding application by student: " + student.getStudentId(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<DormApplication> findAll() {
        String sql = "SELECT * FROM dorm_applications";
        List<DormApplication> applications = new ArrayList<>();
        
        // First get all students for lookup
        List<Student> allStudents = studentRepository.findAll();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String studentUserId = rs.getString("studentId");
                
                // Find the student
                for (Student student : allStudents) {
                    if (student.getId().equals(studentUserId)) {
                        applications.add(resultSetToApplication(rs, student));
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding all applications", e);
        }
        
        return applications;
    }
    
    @Override
    public void save(DormApplication application) {
        String sql = """
            INSERT INTO dorm_applications (id, studentId, status, adminNote, submittedDate, responseHistory)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, application.getId());
            stmt.setString(2, application.getStudent().getId());
            stmt.setString(3, application.getStatus().name());
            stmt.setString(4, application.getAdminNote());
            
            // Convert date string to SQL Date
            Date submittedDate = parseSubmittedDate(application.getSubmittedDate());
            stmt.setDate(5, submittedDate);
            
            stmt.setString(6, application.getResponseHistory());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error saving application: " + application.getId(), e);
        }
    }
    
    @Override
    public void update(DormApplication application) {
        String sql = """
            UPDATE dorm_applications SET
                status = ?, adminNote = ?, submittedDate = ?, responseHistory = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, application.getStatus().name());
            stmt.setString(2, application.getAdminNote());
            
            Date submittedDate = parseSubmittedDate(application.getSubmittedDate());
            stmt.setDate(3, submittedDate);
            
            stmt.setString(4, application.getResponseHistory());
            stmt.setString(5, application.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating application: " + application.getId(), e);
        }
    }
    
    @Override
    public void delete(DormApplication application) {
        String sql = "DELETE FROM dorm_applications WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, application.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting application: " + application.getId(), e);
        }
    }
    
   
    //Convert ResultSet row to DormApplication object
     
    private DormApplication resultSetToApplication(ResultSet rs, Student student) throws SQLException {
        DormApplication app = new DormApplication(rs.getString("id"), student);
        
        String statusStr = rs.getString("status");
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                app.setStatus(ApplicationStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                app.setStatus(ApplicationStatus.PHASE_ONE_PENDING);
            }
        }
        
        app.setAdminNote(rs.getString("adminNote"));
        
        // Convert SQL Date to string format
        Date submittedDate = rs.getDate("submittedDate");
        if (submittedDate != null) {
            app.setSubmittedDate(submittedDate.toLocalDate().format(DATE_FORMAT));
        }
        
        app.setResponseHistory(rs.getString("responseHistory"));
        
        return app;
    }
    
    //Parse submitted date string to SQL Date
    
    private Date parseSubmittedDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return Date.valueOf(LocalDate.now());
        }
        
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMAT);
            return Date.valueOf(localDate);
        } catch (Exception e) {
            return Date.valueOf(LocalDate.now());
        }
    }
}
