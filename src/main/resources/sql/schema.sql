-- Dormitory Management System - MySQL Schema
-- Run this script to initialize the database

-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS dormitory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE dormitory_db;

-- Drop tables if they exist (for fresh install)
DROP TABLE IF EXISTS dorm_applications;
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS Student;
DROP TABLE IF EXISTS users;

-- Create users table (for admins, owners, proctors)
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role ENUM('STUDENT','ADMIN','OWNER','PROCTOR') NOT NULL
);

-- Create Student table
CREATE TABLE Student (
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
);

-- Create messages table
CREATE TABLE messages (
    id VARCHAR(36) PRIMARY KEY,
    fromUser VARCHAR(100) NOT NULL,
    toUser VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    sentAt DATETIME NOT NULL,
    isRead BOOLEAN DEFAULT FALSE
);

-- Create dorm_applications table
CREATE TABLE dorm_applications (
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
);

-- Create announcements table
CREATE TABLE announcements (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    createdBy VARCHAR(100) NOT NULL,
    createdAt DATETIME NOT NULL
);

-- Create indexes for performance
CREATE INDEX idx_student_studentId ON Student(studentId);
CREATE INDEX idx_student_building ON Student(assignedBuilding);
CREATE INDEX idx_applications_studentId ON dorm_applications(studentId);
CREATE INDEX idx_applications_status ON dorm_applications(status);
CREATE INDEX idx_messages_fromUser ON messages(fromUser);
CREATE INDEX idx_messages_toUser ON messages(toUser);
CREATE INDEX idx_announcements_createdAt ON announcements(createdAt);
CREATE INDEX idx_users_role ON users(role);

-- Insert default admin user
INSERT INTO users (id, username, password, full_name, role) 
VALUES ('admin-001', 'admin', 'admin123', 'System Administrator', 'ADMIN')
ON DUPLICATE KEY UPDATE id=id;

-- Insert default owner user
INSERT INTO users (id, username, password, full_name, role) 
VALUES ('owner-001', 'owner', 'owner123', 'System Owner', 'OWNER')
ON DUPLICATE KEY UPDATE id=id;

SELECT 'Database schema created successfully!' AS status;

