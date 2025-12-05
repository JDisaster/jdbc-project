-- Create and use the database
CREATE DATABASE IF NOT EXISTS studentdb;
USE studentdb;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL, --we are aware storing passwords like this is indeed bad practice
    major VARCHAR(50) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE courses (
    course_id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    department VARCHAR(50) NOT NULL
);

CREATE TABLE enrollments (
    enrollment_id INT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    course_code VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    enrollment_date DATE NOT NULL,
    FOREIGN KEY (student_id) REFERENCES users(student_id),
    FOREIGN KEY (course_code) REFERENCES courses(course_code)
);

CREATE TABLE grades (
    grade_id INT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id INT NOT NULL,
    grade VARCHAR(2) NOT NULL,
    points DECIMAL(3,2) NOT NULL,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

-- Sample data inserts (at least 15 records per table)

-- Insert 20 users
INSERT INTO users (student_id, name, password, major, semester) VALUES
('1001', 'John Doe', 'password123', 'Computer Science', 'Fall 2025'),
('1002', 'Jane Smith', 'password123', 'Business Administration', 'Fall 2025'),
('1003', 'Michael Brown', 'password123', 'Electrical Engineering', 'Fall 2025'),
('1004', 'Sarah Davis', 'password123', 'Biology', 'Fall 2025'),
('1005', 'David Wilson', 'password123', 'Psychology', 'Fall 2025'),
('1006', 'Lisa Anderson', 'password123', 'Mathematics', 'Fall 2025'),
('1007', 'Robert Taylor', 'password123', 'Computer Science', 'Fall 2025'),
('1008', 'Jennifer Lee', 'password123', 'Business Administration', 'Fall 2025'),
('1009', 'William Clark', 'password123', 'Electrical Engineering', 'Fall 2025'),
('1010', 'Maria Martinez', 'password123', 'Biology', 'Fall 2025'),
('1011', 'James Rodriguez', 'password123', 'Psychology', 'Fall 2025'),
('1012', 'Patricia Lewis', 'password123', 'Mathematics', 'Fall 2025'),
('1013', 'Charles Walker', 'password123', 'Computer Science', 'Fall 2025'),
('1014', 'Karen Hall', 'password123', 'Business Administration', 'Fall 2025'),
('1015', 'Daniel Allen', 'password123', 'Electrical Engineering', 'Fall 2025'),
('1016', 'Nancy Young', 'password123', 'Biology', 'Fall 2025'),
('1017', 'Paul King', 'password123', 'Psychology', 'Fall 2025'),
('1018', 'Betty Wright', 'password123', 'Mathematics', 'Fall 2025'),
('1019', 'Mark Scott', 'password123', 'Computer Science', 'Fall 2025'),
('1020', 'Sandra Adams', 'password123', 'Business Administration', 'Fall 2025');

-- Insert 20 courses
INSERT INTO courses (course_code, course_name, credits, department) VALUES
('CS101', 'Introduction to Programming', 3, 'Computer Science'),
('CS201', 'Data Structures', 4, 'Computer Science'),
('CS301', 'Database Systems', 3, 'Computer Science'),
('CS401', 'Software Engineering', 4, 'Computer Science'),
('CS501', 'Artificial Intelligence', 4, 'Computer Science'),
('BUS101', 'Introduction to Business', 3, 'Business Administration'),
('BUS201', 'Marketing Principles', 3, 'Business Administration'),
('BUS301', 'Financial Management', 3, 'Business Administration'),
('BUS401', 'Operations Management', 3, 'Business Administration'),
('BUS501', 'Strategic Management', 3, 'Business Administration'),
('EE101', 'Circuit Analysis', 4, 'Electrical Engineering'),
('EE201', 'Digital Systems', 4, 'Electrical Engineering'),
('EE301', 'Signals and Systems', 4, 'Electrical Engineering'),
('EE401', 'Control Systems', 4, 'Electrical Engineering'),
('EE501', 'Power Systems', 4, 'Electrical Engineering'),
('MATH101', 'Calculus I', 4, 'Mathematics'),
('MATH201', 'Linear Algebra', 3, 'Mathematics'),
('MATH301', 'Differential Equations', 3, 'Mathematics'),
('PHYS101', 'Physics I', 4, 'Physics'),
('CHEM101', 'General Chemistry', 4, 'Chemistry');

-- Insert 30 enrollments (giving each student 1-3 courses)
INSERT INTO enrollments (student_id, course_code, semester, enrollment_date) VALUES
-- John Doe (CS major) takes CS courses
('1001', 'CS101', 'Fall 2025', '2025-08-25'),
('1001', 'CS201', 'Fall 2025', '2025-08-25'),
('1001', 'MATH101', 'Fall 2025', '2025-08-25'),

-- Jane Smith (Business) takes business courses
('1002', 'BUS101', 'Fall 2025', '2025-08-25'),
('1002', 'BUS201', 'Fall 2025', '2025-08-25'),

-- Michael Brown (EE) takes engineering courses
('1003', 'EE101', 'Fall 2025', '2025-08-25'),
('1003', 'MATH101', 'Fall 2025', '2025-08-25'),
('1003', 'PHYS101', 'Fall 2025', '2025-08-25'),

-- Sarah Davis (Biology)
('1004', 'CHEM101', 'Fall 2025', '2025-08-25'),
('1004', 'MATH101', 'Fall 2025', '2025-08-25'),

-- David Wilson (Psychology)
('1005', 'BUS101', 'Fall 2025', '2025-08-25'),

-- Lisa Anderson (Mathematics)
('1006', 'MATH101', 'Fall 2025', '2025-08-25'),
('1006', 'MATH201', 'Fall 2025', '2025-08-25'),
('1006', 'CS101', 'Fall 2025', '2025-08-25'),

-- Robert Taylor (CS)
('1007', 'CS101', 'Fall 2025', '2025-08-25'),
('1007', 'CS201', 'Fall 2025', '2025-08-25'),

-- Jennifer Lee (Business)
('1008', 'BUS101', 'Fall 2025', '2025-08-25'),
('1008', 'BUS201', 'Fall 2025', '2025-08-25'),

-- William Clark (EE)
('1009', 'EE101', 'Fall 2025', '2025-08-25'),
('1009', 'EE201', 'Fall 2025', '2025-08-25'),

-- Maria Martinez (Biology)
('1010', 'CHEM101', 'Fall 2025', '2025-08-25'),

-- James Rodriguez (Psychology)
('1011', 'BUS101', 'Fall 2025', '2025-08-25'),

-- Patricia Lewis (Mathematics)
('1012', 'MATH101', 'Fall 2025', '2025-08-25'),
('1012', 'MATH201', 'Fall 2025', '2025-08-25'),

-- Charles Walker (CS)
('1013', 'CS101', 'Fall 2025', '2025-08-25'),
('1013', 'CS301', 'Fall 2025', '2025-08-25'),

-- Karen Hall (Business)
('1014', 'BUS101', 'Fall 2025', '2025-08-25'),
('1014', 'BUS301', 'Fall 2025', '2025-08-25'),

-- Daniel Allen (EE)
('1015', 'EE101', 'Fall 2025', '2025-08-25'),

-- Nancy Young (Biology)
('1016', 'CHEM101', 'Fall 2025', '2025-08-25'),

-- Paul King (Psychology)
('1017', 'BUS101', 'Fall 2025', '2025-08-25'),

-- Betty Wright (Mathematics)
('1018', 'MATH101', 'Fall 2025', '2025-08-25'),

-- Mark Scott (CS)
('1019', 'CS101', 'Fall 2025', '2025-08-25'),

-- Sandra Adams (Business)
('1020', 'BUS101', 'Fall 2025', '2025-08-25'),
('1020', 'BUS201', 'Fall 2025', '2025-08-25');

-- Insert 25 grades
INSERT INTO grades (enrollment_id, grade, points) VALUES
-- Grades for John Doe
(1, 'A', 4.00),  -- CS101
(2, 'B+', 3.30), -- CS201
(3, 'A-', 3.70), -- MATH101

-- Grades for Jane Smith
(4, 'B', 3.00),  -- BUS101
(5, 'A', 4.00),  -- BUS201

-- Grades for Michael Brown
(6, 'B+', 3.30), -- EE101
(7, 'A-', 3.70), -- MATH101
(8, 'B', 3.00),  -- PHYS101

-- Grades for Sarah Davis
(9, 'C+', 2.30), -- CHEM101
(10, 'B-', 2.70),-- MATH101

-- Grades for David Wilson
(11, 'A', 4.00), -- BUS101

-- Grades for Lisa Anderson
(12, 'A', 4.00), -- MATH101
(13, 'A-', 3.70),-- MATH201
(14, 'B+', 3.30),-- CS101

-- Grades for Robert Taylor
(15, 'C+', 2.30),-- CS101
(16, 'B', 3.00), -- CS201

-- Grades for Jennifer Lee
(17, 'A-', 3.70),-- BUS101
(18, 'B+', 3.30),-- BUS201

-- Grades for William Clark
(19, 'B', 3.00), -- EE101
(20, 'B-', 2.70),-- EE201

-- Grades for Maria Martinez
(21, 'C', 2.00), -- CHEM101

-- Grades for James Rodriguez
(22, 'B+', 3.30),-- BUS101

-- Grades for Patricia Lewis
(23, 'A', 4.00), -- MATH101
(24, 'A-', 3.70);-- MATH201