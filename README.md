# Project overview

This is a Student Registration System that uses MySQL to handle data, including student, course, enrollment, and grade information.
Supported actions include adding courses, dropping courses, filtering courses on specific criteria, and logging in or signing up.
User input is validated and MySQL statements are properly committed or rolled back if necessary to preserve the state of the database.

# To run the project

Install MySQL and create a user where both username and password are set to "admin" (or set these values in DatabaseConnection.java).
Ensure the MySQL server process is running by going into command prompt (or terminal) and running "net start MySQL80" or "net start MySQL".
Run the contents of StudentDB.sql, which can be done by pasting the contents of this file into the MySQL server command line and pressing enter.
Finally, open a shell (or terminal) inside the demo folder and run "mvn javafx:run". This will run the application and display the initial GUI.

# Dependencies
Dependencies include JDK (to run Java) and MySQL Server. Maven will automatically load all other dependencies, of which only includes JavaFX for the GUI.

# Connecting to the database
To connect, the user you create when installing MySQL must match the details in DatabaseConnection.java. Otherwise, you will need to create
a new user in the MySQL server command line and grant it all privileges, which can be done by running these commands:
CREATE USER 'put_new_username_here'@'localhost' IDENTIFIED BY 'put_new_password_here'; (if this does not work, remove the @'localhost')
GRANT ALL PRIVILEGES ON *.* TO 'put_new_username_here'@'localhost' WITH GRANT OPTION; (again, if this does not work, remove @'localhost')
FLUSH PRIVILEGES;

This will create a new user with all privileges. Match the username and password of this new user to the credentials in DatabaseConnection.java
to finish setup.