package dorm.dao;
 
public class DaoFactory {
    
    private static UserRepository userRepository;
    private static StudentRepository studentRepository;
    private static ApplicationRepository applicationRepository;
    private static AnnouncementRepository announcementRepository;
    private static MessageRepository messageRepository;
    
    public static UserRepository createUserRepository() {
        if (userRepository == null) {
            userRepository = new MySqlUserRepository();
        }
        return userRepository;
    }
    
    public static StudentRepository createStudentRepository() {
        if (studentRepository == null) {
            studentRepository = new MySqlStudentRepository();
        }
        return studentRepository;
    }
    
    public static ApplicationRepository createApplicationRepository() {
        if (applicationRepository == null) {
            applicationRepository = new MySqlApplicationRepository(createStudentRepository());
        }
        return applicationRepository;
    }
    
    public static AnnouncementRepository createAnnouncementRepository() {
        if (announcementRepository == null) {
            announcementRepository = new MySqlAnnouncementRepository();
        }
        return announcementRepository;
    }
    
    public static MessageRepository createMessageRepository() {
        if (messageRepository == null) {
            messageRepository = new MySqlMessageRepository();
        }
        return messageRepository;
    }
    
    //Reset all repositories (useful for testing)

    public static void reset() {
        userRepository = null;
        studentRepository = null;
        applicationRepository = null;
        announcementRepository = null;
        messageRepository = null;
    }
}
