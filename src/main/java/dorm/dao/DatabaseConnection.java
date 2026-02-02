package dorm.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class DatabaseConnection {
    
    private static final String CONFIG_FILE = "/dorm/db.properties";
    
    private static String url = "jdbc:mysql://localhost:3306/dormitory_db";
    private static String username = "root";
    private static String password = "";
    
    private static boolean initialized = false;
    
    static {
        loadConfiguration();
    }
    
    //Load database configuration from properties file

    private static void loadConfiguration() {
        try (InputStream input = DatabaseConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                
                url = props.getProperty("db.url", url);
                username = props.getProperty("db.username", username);
                password = props.getProperty("db.password", password);
            }
        } catch (IOException e) {
            System.err.println("Could not load database configuration, using defaults: " + e.getMessage());
        }
    }
    
    //Get a database connection

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
        
        Connection conn = DriverManager.getConnection(url, username, password);
        
        if (!initialized) {
            initializeDatabase(conn);
            initialized = true;
        }
        
        return conn;
    }
    
    //Initialize database tables if they don't exist

    private static void initializeDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    full_name VARCHAR(100),
                    role ENUM('STUDENT','ADMIN','OWNER','PROCTOR') NOT NULL
                )
            """);
            
            // Create Student table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Student (
                    id VARCHAR(50) PRIMARY KEY,
                    username VARCHAR(50) NOT NULL UNIQUE,
                    password VARCHAR(100) NOT NULL,
                    role ENUM('STUDENT', 'ADMIN', 'PROCTOR', 'OWNER') NOT NULL DEFAULT 'STUDENT',
                    displayName VARCHAR(100),
                    studentId VARCHAR(20) NOT NULL UNIQUE,
                    gender ENUM('MALE', 'FEMALE'),
                    college VARCHAR(50),
                    residency ENUM('ADDIS_ABABA', 'SHEGER_CITY', 'REGIONAL'),
                    city VARCHAR(50),
                    subcity VARCHAR(50),
                    woreda VARCHAR(50),
                    sponsorshipType ENUM('GOVERNMENT', 'SELF_SPONSORED'),
                    disabilityInfo VARCHAR(255),
                    emergencyContactName VARCHAR(100),
                    emergencyContactPhone VARCHAR(20),
                    transactionId VARCHAR(50),
                    assignedBuilding VARCHAR(50) DEFAULT 'unassigned'
                )
            """);
            
            // Create messages table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id VARCHAR(36) PRIMARY KEY,
                    fromUser VARCHAR(100) NOT NULL,
                    toUser VARCHAR(100) NOT NULL,
                    content TEXT NOT NULL,
                    sentAt DATETIME NOT NULL,
                    isRead BOOLEAN DEFAULT FALSE
                )
            """);
            
            // Create dorm_applications table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS dorm_applications (
                    id VARCHAR(36) PRIMARY KEY,
                    studentId VARCHAR(36) NOT NULL,
                    status ENUM(
                        'PHASE_ONE_PENDING',
                        'PHASE_ONE_APPROVED',
                        'PHASE_ONE_DECLINED',
                        'PHASE_ONE_RESUBMIT',
                        'PHASE_TWO_PENDING',
                        'PHASE_TWO_APPROVED',
                        'PHASE_TWO_DECLINED',
                        'ASSIGNED'
                    ) NOT NULL DEFAULT 'PHASE_ONE_PENDING',
                    adminNote TEXT,
                    submittedDate DATE NOT NULL,
                    responseHistory TEXT,
                    FOREIGN KEY (studentId) REFERENCES Student(id) ON DELETE CASCADE
                )
            """);
            
            // Create announcements table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS announcements (
                    id VARCHAR(36) PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    body TEXT NOT NULL,
                    createdBy VARCHAR(100) NOT NULL,
                    createdAt DATETIME NOT NULL
                )
            """);
            
            System.out.println("Database tables initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            // Don't throw - tables might already exist with different structure
        }
    }
    
    //Close a connection safely

    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    
    //Set connection parameters programmatically (for testing)

    public static void setConnectionParams(String dbUrl, String dbUsername, String dbPassword) {
        url = dbUrl;
        username = dbUsername;
        password = dbPassword;
        initialized = false;
    }
}
