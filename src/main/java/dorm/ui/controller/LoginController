package dorm.ui.controller;

import dorm.model.*;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    
    @FXML private TabPane tabPane;
    
    // Login fields
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginStatusLabel;
    
    // Register fields
    @FXML private TextField registerNameField;
    @FXML private TextField registerStudentIdField;
    @FXML private ComboBox<Gender> registerGenderBox;
    @FXML private ComboBox<College> registerCollegeBox;
    @FXML private PasswordField registerPasswordField;
    @FXML private PasswordField registerConfirmPasswordField;
    @FXML private Label registerStatusLabel;
    
    private DatabaseDormService service;
    
    @FXML
    public void initialize() {
        // Initialize gender combo box
        registerGenderBox.setItems(FXCollections.observableArrayList(Gender.values()));
        
        // Initialize college combo box with custom display
        registerCollegeBox.setItems(FXCollections.observableArrayList(College.values()));
        registerCollegeBox.setConverter(new StringConverter<College>() {
            @Override
            public String toString(College college) {
                return college != null ? college.getFullName() : "";
            }
            
            @Override
            public College fromString(String string) {
                return null;
            }
        });
        
        // Add Enter key functionality for login fields
        loginUsernameField.setOnAction(e -> onLogin());
        loginPasswordField.setOnAction(e -> onLogin());
        
        // Add Enter key functionality for register fields
        registerConfirmPasswordField.setOnAction(e -> onRegister());
    }
    
    public void setService(DatabaseDormService service) {
        this.service = service;
    }
    
    @FXML
    private void onLogin() {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter username and password");
            return;
        }
        
        Optional<Object> result = service.authenticate(username, password);
        
        if (result.isEmpty()) {
            showAlert("Invalid username or password");
            return;
        }
        
        loginStatusLabel.setText("");
        navigateToDashboard(result.get());
    }
    
    @FXML
    private void onRegister() {
        String fullName = registerNameField.getText().trim();
        String studentId = registerStudentIdField.getText().trim().toUpperCase();
        Gender gender = registerGenderBox.getValue();
        College college = registerCollegeBox.getValue();
        String password = registerPasswordField.getText();
        String confirmPassword = registerConfirmPasswordField.getText();
        
        // Validation
        if (fullName.isEmpty() || studentId.isEmpty() || gender == null || 
            college == null || password.isEmpty()) {
            showAlert("All fields are required");
            return;
        }
        
        // Validate student ID format
        String idError = service.validateStudentIdFormat(studentId);
        if (idError != null) {
            showAlert(idError);
            return;
        }
        
        // Check if student ID is available
        if (!service.isStudentIdAvailable(studentId)) {
            showAlert("This Student ID is already registered");
            return;
        }
        
        // Password validation
        if (password.length() < 8) {
            showAlert("Password must be at least 8 characters");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showAlert("Passwords do not match");
            return;
        }
        
        try {
            Student student = service.registerStudent(
                studentId, // Use student ID as username
                password,
                fullName,
                studentId,
                gender,
                college
            );
            
            navigateToDashboard(student);
            
        } catch (Exception e) {
            showAlert("Registration failed: " + e.getMessage());
        }
    }
    
    private void navigateToDashboard(Object user) {
        try {
            String fxmlPath;
            Object controller;
            
            if (user instanceof Student) {
                fxmlPath = "/dorm/ui/student_dashboard.fxml";
            } else if (user instanceof User) {
                User u = (User) user;
                if (u.getRole() == Role.OWNER) {
                    fxmlPath = "/dorm/ui/owner_dashboard.fxml";
                } else {
                    fxmlPath = "/dorm/ui/admin_dashboard.fxml";
                }
            } else {
                showAlert("Unknown user type");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            // Set up the controller
            if (user instanceof Student) {
                StudentController ctrl = loader.getController();
                ctrl.setService(service);
                ctrl.setStudent((Student) user);
                ctrl.initialize();
            } else if (user instanceof User) {
                User u = (User) user;
                if (u.getRole() == Role.OWNER) {
                    OwnerController ctrl = loader.getController();
                    ctrl.setService(service);
                    ctrl.setUser(u);
                    ctrl.initialize();
                } else {
                    AdminController ctrl = loader.getController();
                    ctrl.setService(service);
                    ctrl.setUser(u);
                    ctrl.initialize();
                }
            }
            
            Stage stage = (Stage) tabPane.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            
        } catch (IOException e) {
            showAlert("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
