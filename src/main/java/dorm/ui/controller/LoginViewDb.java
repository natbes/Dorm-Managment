
package dorm.ui;

import dorm.model.College;
import dorm.model.Gender;
import dorm.model.Role;
import dorm.model.Student;
import dorm.model.User;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Optional;

public class LoginViewDb {
    private final DatabaseDormService service;
    private final Stage stage;
    private final TabPane root;

    public LoginViewDb(DatabaseDormService service, Stage stage) {
        this.service = service;
        this.stage = stage;
        this.root = new TabPane();
        this.root.getTabs().add(createLoginTab());
        this.root.getTabs().add(createRegisterTab());
    }

    public Parent getRoot() {
        return root;
    }

    private Tab createLoginTab() {
        Tab tab = new Tab("Login");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Student ID (e.g., UGR/1234/16)");
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");

        Label usernameLabel = new Label("Username / Student ID");
        form.addRow(0, usernameLabel, usernameField);
        form.addRow(1, new Label("Password"), passwordField);
        form.add(loginButton, 1, 2);

        loginButton.setOnAction(event -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Username and password required");
                return;
            }
            
            Optional<Object> authResult = service.authenticate(username, password);
            if (authResult.isEmpty()) {
                showAlert("Invalid credentials");
                return;
            }
            
            switchToDashboard(authResult.get());
        });

        VBox wrapper = new VBox(form);
        wrapper.setAlignment(Pos.CENTER);
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createRegisterTab() {
        Tab tab = new Tab("Register");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        TextField fullNameField = new TextField();
        TextField studentIdField = new TextField();
        ComboBox<Gender> genderBox = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        
        // College dropdown - shows full name
        ComboBox<College> collegeBox = new ComboBox<>(FXCollections.observableArrayList(College.values()));
        collegeBox.setConverter(new StringConverter<College>() {
            @Override
            public String toString(College college) {
                return college != null ? college.getFullName() : "";
            }
            @Override
            public College fromString(String string) {
                return null;
            }
        });
        
        PasswordField passwordField = new PasswordField();
        Button registerButton = new Button("Create Account");

        studentIdField.setPromptText("UGR/XXXX/YY");
        
        // Note: Student ID will be used as username for login
        Label idNote = new Label("(This will be your login username)");
        idNote.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
        
        form.addRow(0, new Label("Full Name"), fullNameField);
        form.addRow(1, new Label("Student ID (UGR/XXXX/YY)"), studentIdField);
        form.add(idNote, 1, 2);
        form.addRow(3, new Label("Gender"), genderBox);
        form.addRow(4, new Label("College"), collegeBox);
        form.addRow(5, new Label("Password (min 8 chars)"), passwordField);
        form.add(registerButton, 1, 6);

        registerButton.setOnAction(event -> {
            if (fullNameField.getText().isBlank() || studentIdField.getText().isBlank() || 
                genderBox.getValue() == null || collegeBox.getValue() == null ||
                passwordField.getText().isBlank()) {
                showAlert("All fields required");
                return;
            }
            
            String studentId = studentIdField.getText().trim().toUpperCase();
            
            // Validate student ID format
            String idError = service.validateStudentIdFormat(studentId);
            if (idError != null) {
                showAlert(idError);
                return;
            }
            
            // Check if student ID is already registered (also serves as username check)
            if (!service.isStudentIdAvailable(studentId)) {
                showAlert("This Student ID is already registered");
                return;
            }
            
            // Password validation - minimum 8 characters
            if (passwordField.getText().length() < 8) {
                showAlert("Password must be at least 8 characters");
                return;
            }
            
            try {
                // Use student ID as username for simpler login
                Student student = service.registerStudent(
                        studentId,  // Use student ID as username
                        passwordField.getText().trim(),
                        fullNameField.getText().trim(),
                        studentId,
                        genderBox.getValue(),
                        collegeBox.getValue()
                );
                switchToDashboard(student);
            } catch (Exception e) {
                showAlert("Registration failed: " + e.getMessage());
            }
        });

        VBox wrapper = new VBox(form);
        wrapper.setAlignment(Pos.CENTER);
        tab.setContent(wrapper);
        return tab;
    }

    private void switchToDashboard(Object authenticated) {
        Scene scene;
        
        if (authenticated instanceof Student) {
            Student student = (Student) authenticated;
            scene = new Scene(new StudentDashboardDb(service, student, stage).getRoot(), 1200, 700);
        } else if (authenticated instanceof User) {
            User user = (User) authenticated;
            if (user.getRole() == Role.ADMIN) {
                scene = new Scene(new AdminDashboardDb(service, user, stage).getRoot(), 1200, 700);
            } else if (user.getRole() == Role.OWNER) {
                scene = new Scene(new OwnerDashboardDb(service, user, stage).getRoot(), 1200, 700);
            } else {
                showAlert("Unknown role");
                return;
            }
        } else {
            showAlert("Unknown account type");
            return;
        }
        
        stage.setScene(scene);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
