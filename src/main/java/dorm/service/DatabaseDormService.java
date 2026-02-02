package dorm.service;

import dorm.dao.*;
import dorm.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DatabaseDormService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final AnnouncementRepository announcementRepository;
    private final MessageRepository messageRepository;
    
    public DatabaseDormService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            ApplicationRepository applicationRepository,
            AnnouncementRepository announcementRepository,
            MessageRepository messageRepository) {
        
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.applicationRepository = applicationRepository;
        this.announcementRepository = announcementRepository;
        this.messageRepository = messageRepository;
    }
    
    // ========== Authentication ==========
    
    public Optional<Object> authenticate(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username)
                .filter(u -> u.getPassword().equals(password));
        if (user.isPresent()) {
            return Optional.of(user.get());
        }
        
        for (Student student : studentRepository.findAll()) {
            if (student.getUsername().equals(username) && student.getPassword().equals(password)) {
                return Optional.of(student);
            }
        }
        
        return Optional.empty();
    }
    
    // ========== Student Management ==========

    public boolean isUsernameAvailable(String username) {
        // Check in users
        if (userRepository.findByUsername(username).isPresent()) {
            return false;
        }
        
        // Check in students
        for (Student s : studentRepository.findAll()) {
            if (s.getUsername().equals(username)) {
                return false;
            }
        }
        
        return true;
    }
    

    public boolean isStudentIdAvailable(String studentId) {
        return studentRepository.findByStudentId(studentId).isEmpty();
    }

    public String validateStudentIdFormat(String studentId) {
        if (studentId == null || studentId.isEmpty()) {
            return "Student ID cannot be empty";
        }
        
        // Check length: UGR/XXXX/YY = 11 characters
        if (studentId.length() != 11) {
            return "Student ID must be exactly 11 characters (format: UGR/XXXX/YY)";
        }
        
        // Check prefix "UGR/"
        String prefix = studentId.substring(0, 4);
        if (!prefix.equals("UGR/")) {
            return "Student ID must start with 'UGR/'";
        }
        
        // Check 4 digits after prefix (positions 4-7)
        for (int i = 4; i < 8; i++) {
            char c = studentId.charAt(i);
            if (c < '0' || c > '9') {
                return "Student ID must have 4 digits after 'UGR/' (format: UGR/XXXX/YY)";
            }
        }
        
        // Check separator "/" at position 8
        if (studentId.charAt(8) != '/') {
            return "Student ID must have '/' after the 4 digits (format: UGR/XXXX/YY)";
        }
        
        // Check 2 digits for year (positions 9-10)
        for (int i = 9; i < 11; i++) {
            char c = studentId.charAt(i);
            if (c < '0' || c > '9') {
                return "Student ID must end with 2 digits for year (format: UGR/XXXX/YY)";
            }
        }
        
        return null; // Valid
    }
    
    public Student registerStudent(String username, String password, String fullName, 
                                   String studentId, Gender gender, College college) {
        Student student = new Student(
            UUID.randomUUID().toString(),
            username,
            password,
            fullName,
            studentId,
            gender,
            college
        );
        
        studentRepository.save(student);
        return student;
    }
    
    public List<Student> getStudents() {
        return studentRepository.findAll();
    }
    
    public Optional<Student> findStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }
    
    public void updateStudent(Student student) {
        studentRepository.update(student);
    }
    
    // ========== Application Management ==========
    
    public DormApplication submitPhaseOneApplication(Student student, SponsorshipType sponsorshipType,
                                                      Residency residency, String city, String subcity, 
                                                      String woreda, String disabilityInfo) {
        student.setSponsorshipType(sponsorshipType);
        student.setResidency(residency);
        student.setCity(city);
        student.setSubcity(subcity);
        student.setWoreda(woreda);
        student.setDisabilityInfo(disabilityInfo);
        studentRepository.update(student);
        
        Optional<DormApplication> existing = applicationRepository.findByStudent(student);
        if (existing.isPresent()) {
            DormApplication app = existing.get();
            app.setStatus(ApplicationStatus.PHASE_ONE_PENDING);
            applicationRepository.update(app);
            return app;
        }
        
        DormApplication application = new DormApplication(
            UUID.randomUUID().toString(),
            student
        );
        application.setStatus(ApplicationStatus.PHASE_ONE_PENDING);
        
        applicationRepository.save(application);
        return application;
    }
    
    public void submitPhaseTwoApplication(Student student, String emergencyContactName, 
                                          String emergencyContactPhone, String transactionId) {
        student.setEmergencyContactName(emergencyContactName);
        student.setEmergencyContactPhone(emergencyContactPhone);
        student.setTransactionId(transactionId);
        studentRepository.update(student);
        
        applicationRepository.findByStudent(student).ifPresent(app -> {
            app.setStatus(ApplicationStatus.PHASE_TWO_PENDING);
            applicationRepository.update(app);
        });
    }
    
    public Optional<DormApplication> getApplicationForStudent(Student student) {
        return applicationRepository.findByStudent(student);
    }
    
    public List<DormApplication> getApplications() {
        return applicationRepository.findAll();
    }

    public void changeApplicationStatus(DormApplication application, ApplicationStatus newStatus, String note) {
        application.setStatus(newStatus);
        application.setAdminNote(note);
        application.addResponseEntry(newStatus);
        applicationRepository.update(application);
    }
    
    public void approvePhaseOne(DormApplication application, String note) {
        changeApplicationStatus(application, ApplicationStatus.PHASE_ONE_APPROVED, note);
    }
    
    public void declinePhaseOne(DormApplication application, String note) {
        changeApplicationStatus(application, ApplicationStatus.PHASE_ONE_DECLINED, note);
    }
    
    public void requestResubmit(DormApplication application, String note) {
        changeApplicationStatus(application, ApplicationStatus.PHASE_ONE_RESUBMIT, note);
    }
    
    public void approvePhaseTwoApplication(DormApplication application, String note) {
        changeApplicationStatus(application, ApplicationStatus.PHASE_TWO_APPROVED, note);
    }
    
    public void declinePhaseTwoApplication(DormApplication application, String note) {
        changeApplicationStatus(application, ApplicationStatus.PHASE_TWO_DECLINED, note);
    }
    
    public void updateApplication(DormApplication application) {
        applicationRepository.update(application);
    }
    
    public boolean canFillPhaseTwo(Student student) {
        return applicationRepository.findByStudent(student)
                .map(app -> app.getStatus() == ApplicationStatus.PHASE_ONE_APPROVED)
                .orElse(false);
    }
    
    public boolean isReadyForAssignment(DormApplication application) {
        ApplicationStatus status = application.getStatus();
        return status == ApplicationStatus.PHASE_TWO_APPROVED || 
               status == ApplicationStatus.PHASE_TWO_PENDING;
    }
    
    public void assignBuilding(Student student, String buildingName) {
        student.setAssignedBuilding(buildingName);
        studentRepository.update(student);
        
        applicationRepository.findByStudent(student).ifPresent(application -> {
            changeApplicationStatus(application, ApplicationStatus.ASSIGNED, "");
        });
    }
    
    // ========== User Management ==========
    
    public List<User> getUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }
    
    public void addUser(User user) {
        userRepository.save(user);
    }
    
    public void removeUser(User user) {
        userRepository.delete(user);
    }
    
    // ========== Announcements ==========
    
    public void addAnnouncement(String title, String body, String createdBy) {
        Announcement announcement = new Announcement(
            UUID.randomUUID().toString(),
            title,
            body,
            createdBy,
            LocalDateTime.now()
        );
        
        announcementRepository.save(announcement);
    }
    
    public List<Announcement> getAnnouncements() {
        return announcementRepository.findAll();
    }
    
    public void updateAnnouncement(Announcement announcement) {
        announcementRepository.update(announcement);
    }
    
    public void deleteAnnouncement(Announcement announcement) {
        announcementRepository.delete(announcement);
    }
    
    // ========== Messaging ==========
    
    public void sendMessage(String fromUser, String toUser, String content) {
        Message message = new Message(
            UUID.randomUUID().toString(),
            fromUser,
            toUser,
            content,
            LocalDateTime.now()
        );
        
        messageRepository.save(message);
    }
    
    public List<Message> getMessagesForUser(String username) {
        return messageRepository.findByUser(username);
    }
    
    public void markMessageAsRead(Message message, boolean read) {
        message.setRead(read);
        messageRepository.update(message);
    }

    public Optional<Student> findStudentByUsername(String username) {
        for (Student s : studentRepository.findAll()) {
            if (s.getUsername().equals(username)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }
}
