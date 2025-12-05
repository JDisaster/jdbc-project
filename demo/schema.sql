CREATE DATABASE IF NOT EXISTS studentdb;
USE studentdb;

CREATE TABLE IF NOT EXISTS students(
    studentID INT AUTO_INCREMENT PRIMARY KEY,
    studentName VARCHAR(200) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    major VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS departments(
    departmentID INT AUTO_INCREMENT PRIMARY KEY,
    departmentName VARCHAR(50) UNIQUE NOT NULL,
    departmentCode VARCHAR(25) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS courses(
    courseID INT AUTO_INCREMENT PRIMARY KEY,
    courseName VARCHAR(100) NOT NULL,
    departmentID INT NOT NULL,
    courseCode VARCHAR(10) UNIQUE NOT NULL,

    FOREIGN KEY (departmentID) REFERENCES departments(departmentID)
);

CREATE TABLE IF NOT EXISTS registrations(
    registrationID INT AUTO_INCREMENT PRIMARY KEY,
    studentID INT NOT NULL,
    courseID INT NOT NULL,

    FOREIGN KEY (studentID) REFERENCES students(studentID),
    FOREIGN KEY (courseID) REFERENCES courses(courseID),

    UNIQUE(studentID, courseID)
);