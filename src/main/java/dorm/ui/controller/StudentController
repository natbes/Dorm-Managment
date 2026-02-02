package dorm.ui.controller;

import dorm.model.*;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class StudentController {
    
    @FXML private Label welcomeLabel;
    @FXML private TabPane mainTabPane;
    
    // Profile
    @FXML private Label profileNameLabel;
    @FXML private Label profileStudentIdLabel;
    @FXML private Label profileGenderLabel;
    @FXML private Label profileCollegeLabel;
    @FXML private Label profileSponsorshipLabel;
    @FXML private Label profileBuildingLabel;
    @FXML private Label profileStatusLabel;
    
    // Phase 1
    @FXML private Label phase1StatusLabel;
    @FXML private ComboBox<SponsorshipType> sponsorshipBox;
    @FXML private ComboBox<Residency> residencyBox;
    @FXML private TextField cityField;
    @FXML private ComboBox<String> subcityBox;
    @FXML private ComboBox<String> woredaBox;
    @FXML private TextField disabilityField;
    @FXML private Button submitPhase1Btn;
    @FXML private Label phase1NoteLabel;
    
    // Phase 2
    @FXML private Tab phase2Tab;
    @FXML private Label phase2StatusLabel;
    @FXML private TextField emergencyNameField;
    @FXML private TextField emergencyPhoneField;
    @FXML private TextField transactionIdField;
    @FXML private Button submitPhase2Btn;
    
    // Announcements
    @FXML private ListView<Announcement> announcementList;
    
    // Messages
    @FXML private ComboBox<String> messageRecipientBox;
    @FXML private TextField messageField;
    @FXML private Label charCountLabel;
    @FXML private ListView<String> messageList;
    
    private DatabaseDormService service;
    private Student student;
    
    public void setService(DatabaseDormService service) {
        this.service = service;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public void initialize() {
        if (service == null || student == null) return;
        
        welcomeLabel.setText("Welcome, " + student.getDisplayName() + " (ID: " + student.getStudentId() + ")");
        
        setupPhase1();
        setupPhase2();
        setupAnnouncements();
        setupMessages();
        refreshProfile();
        refresh();
    }
    
    private void setupPhase1() {
        sponsorshipBox.setItems(FXCollections.observableArrayList(SponsorshipType.values()));
        residencyBox.setItems(FXCollections.observableArrayList(Residency.values()));
        
        // Handle residency change
        residencyBox.setOnAction(e -> {
            Residency selected = residencyBox.getValue();
            subcityBox.getItems().clear();
            woredaBox.getItems().clear();
            subcityBox.setValue(null);
            woredaBox.setValue(null);
            subcityBox.getEditor().clear();
            woredaBox.getEditor().clear();
            
            if (selected == Residency.ADDIS_ABABA) {
                cityField.setText("Addis Ababa");
                cityField.setDisable(true);
                
                // For Addis Ababa: must select from dropdown (not editable)
                subcityBox.setEditable(false);
                woredaBox.setEditable(false);
                
                for (AddisSubcity as : AddisSubcity.values()) {
                    subcityBox.getItems().add(as.getDisplayName());
                }
            } else {
                cityField.setText("");
                cityField.setDisable(false);
                
                // For Sheger/Regional: can type subcity, but woreda must be typed as positive integer
                subcityBox.setEditable(true);
                woredaBox.setEditable(true);
            }
        });
        
        // Handle subcity change for Addis
        subcityBox.setOnAction(e -> {
            String selected = subcityBox.getValue();
            
            if (selected != null && residencyBox.getValue() == Residency.ADDIS_ABABA) {
                woredaBox.getItems().clear();
                woredaBox.setValue(null);
                for (AddisSubcity as : AddisSubcity.values()) {
                    if (as.getDisplayName().equals(selected)) {
                        for (int i = 1; i <= as.getWoredaCount(); i++) {
                            woredaBox.getItems().add(String.valueOf(i));
                        }
                        break;
                    }
                }
            }
        });
        
        // Load existing data
        if (student.getSponsorshipType() != null) sponsorshipBox.setValue(student.getSponsorshipType());
        if (student.getResidency() != null) {
            residencyBox.setValue(student.getResidency());
            residencyBox.fireEvent(new javafx.event.ActionEvent());
        }
        if (student.getCity() != null) cityField.setText(student.getCity());
        if (student.getSubcity() != null) subcityBox.setValue(student.getSubcity());
        if (student.getWoreda() != null) woredaBox.setValue(student.getWoreda());
        if (student.getDisabilityInfo() != null) disabilityField.setText(student.getDisabilityInfo());
        
        updatePhase1Status();
    }
    
    private void updatePhase1Status() {
        Optional<DormApplication> app = service.getApplicationForStudent(student);
        if (app.isPresent()) {
            ApplicationStatus status = app.get().getStatus();
            phase1StatusLabel.setText("Status: " + status.name());
            
            String note = app.get().getAdminNote();
            if (note != null && !note.isBlank()) {
                phase1NoteLabel.setText("Admin Note: " + note);
            }
            
            // Disable form if not pending or resubmit
            if (status != ApplicationStatus.PHASE_ONE_PENDING && 
                status != ApplicationStatus.PHASE_ONE_RESUBMIT) {
                disablePhase1Form();
            }
        } else {
            phase1StatusLabel.setText("Status: Not Applied");
        }
    }
    
    private void disablePhase1Form() {
        sponsorshipBox.setDisable(true);
        residencyBox.setDisable(true);
        cityField.setDisable(true);
        subcityBox.setDisable(true);
        woredaBox.setDisable(true);
        disabilityField.setDisable(true);
        submitPhase1Btn.setDisable(true);
    }
    
    private void setupPhase2() {
        // Load existing data
        if (student.getEmergencyContactName() != null) emergencyNameField.setText(student.getEmergencyContactName());
        if (student.getEmergencyContactPhone() != null) emergencyPhoneField.setText(student.getEmergencyContactPhone());
        if (student.getTransactionId() != null) transactionIdField.setText(student.getTransactionId());
        
        updatePhase2Status();
    }
    
    private void updatePhase2Status() {
        boolean canFill = service.canFillPhaseTwo(student);
        phase2Tab.setDisable(!canFill);
        
        if (!canFill) {
            phase2StatusLabel.setText("Complete Phase 1 first and wait for approval");
        } else {
            Optional<DormApplication> app = service.getApplicationForStudent(student);
            if (app.isPresent()) {
                ApplicationStatus status = app.get().getStatus();
                phase2StatusLabel.setText("Status: " + status.name());
                
                if (status == ApplicationStatus.PHASE_TWO_PENDING ||
                    status == ApplicationStatus.PHASE_TWO_APPROVED ||
                    status == ApplicationStatus.PHASE_TWO_DECLINED ||
                    status == ApplicationStatus.ASSIGNED) {
                    emergencyNameField.setDisable(true);
                    emergencyPhoneField.setDisable(true);
                    transactionIdField.setDisable(true);
                    submitPhase2Btn.setDisable(true);
                }
            }
        }
    }
    
    private void setupAnnouncements() {
        announcementList.setCellFactory(lv -> new ListCell<Announcement>() {
            @Override
            protected void updateItem(Announcement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    box.getStyleClass().add("announcement-card");
                    
                    Label title = new Label(item.getTitle());
                    title.getStyleClass().add("announcement-title");
                    
                    Label body = new Label(item.getBody());
                    body.setWrapText(true);
                    
                    Label date = new Label(item.getCreatedAt().toString() + " by " + item.getCreatedBy());
                    date.getStyleClass().add("announcement-date");
                    
                    box.getChildren().addAll(title, body, date);
                    setGraphic(box);
                }
            }
        });
    }
    
    private void setupMessages() {
        // Populate recipients
        messageRecipientBox.getItems().addAll(
            service.getUsersByRole(Role.ADMIN).stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
        );
        messageRecipientBox.getItems().addAll(
            service.getUsersByRole(Role.OWNER).stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
        );
        
        // Character counter
        messageField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 80) {
                messageField.setText(old);
            } else {
                int len = newVal != null ? newVal.length() : 0;
                charCountLabel.setText(len + "/80");
            }
        });
        
        // Enter key to send message
        messageField.setOnAction(e -> onSendMessage());
    }
    
    private void refreshProfile() {
        profileNameLabel.setText(student.getDisplayName());
        profileStudentIdLabel.setText(student.getStudentId());
        profileGenderLabel.setText(student.getGender() != null ? student.getGender().name() : "-");
        profileCollegeLabel.setText(student.getCollege() != null ? student.getCollege().getAcronym() : "-");
        profileSponsorshipLabel.setText(student.getSponsorshipType() != null ? student.getSponsorshipType().name() : "-");
        profileBuildingLabel.setText(student.getAssignedBuilding());
        
        String status = service.getApplicationForStudent(student)
            .map(app -> app.getStatus().name())
            .orElse("Not Applied");
        profileStatusLabel.setText(status);
    }
    
    private void refresh() {
        announcementList.setItems(FXCollections.observableArrayList(service.getAnnouncements()));
        
        messageList.setItems(FXCollections.observableArrayList(
            service.getMessagesForUser(student.getUsername()).stream()
                .map(m -> m.getSentAt().toLocalDate() + " | " + m.getFromUser() + ": " + m.getContent())
                .collect(Collectors.toList())
        ));
    }
    
    @FXML
    private void onRefresh() {
        // Reload student from database
        Optional<Student> reloaded = service.findStudentByStudentId(student.getStudentId());
        if (reloaded.isPresent()) {
            this.student = reloaded.get();
        }
        
        refreshProfile();
        updatePhase1Status();
        updatePhase2Status();
        refresh();
        
        showAlert("Data refreshed", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    private void onLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dorm/ui/login.fxml"));
            Parent root = loader.load();
            
            LoginController ctrl = loader.getController();
            ctrl.setService(service);
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
        } catch (IOException e) {
            showAlert("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void onSubmitPhase1() {
        if (sponsorshipBox.getValue() == null || residencyBox.getValue() == null) {
            showAlert("Sponsorship and Residency are required", Alert.AlertType.WARNING);
            return;
        }
        
        String city = cityField.getText().trim();
        String subcity = subcityBox.getValue();
        String woreda = woredaBox.getValue();
        
        // Get editor values for editable combo boxes (Sheger/Regional)
        if (subcity == null || subcity.isEmpty()) {
            subcity = subcityBox.getEditor().getText().trim();
        }
        if (woreda == null || woreda.isEmpty()) {
            woreda = woredaBox.getEditor().getText().trim();
        }
        
        Residency selectedResidency = residencyBox.getValue();
        
        if (city.isEmpty()) {
            showAlert("City is required", Alert.AlertType.WARNING);
            return;
        }
        
        // Validation based on residency type
        if (selectedResidency == Residency.ADDIS_ABABA) {
            // For Addis Ababa: must select from the dropdown list
            if (subcity == null || subcity.isEmpty()) {
                showAlert("Please select a Subcity from the list", Alert.AlertType.WARNING);
                return;
            }
            // Verify subcity is from the enum
            boolean validSubcity = false;
            for (AddisSubcity as : AddisSubcity.values()) {
                if (as.getDisplayName().equals(subcity)) {
                    validSubcity = true;
                    break;
                }
            }
            if (!validSubcity) {
                showAlert("Please select a valid Subcity from the list", Alert.AlertType.WARNING);
                return;
            }
            if (woreda == null || woreda.isEmpty()) {
                showAlert("Please select a Woreda from the list", Alert.AlertType.WARNING);
                return;
            }
        } else {
            // For Sheger/Regional: subcity can be typed, woreda must be positive integer
            if (subcity == null || subcity.isEmpty()) {
                showAlert("Subcity is required", Alert.AlertType.WARNING);
                return;
            }
            if (woreda == null || woreda.isEmpty()) {
                showAlert("Woreda is required", Alert.AlertType.WARNING);
                return;
            }
            // Validate woreda is a positive integer
            try {
                int woredaNum = Integer.parseInt(woreda);
                if (woredaNum <= 0) {
                    showAlert("Woreda must be a positive integer", Alert.AlertType.WARNING);
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Woreda must be a positive integer", Alert.AlertType.WARNING);
                return;
            }
        }
        
        try {
            service.submitPhaseOneApplication(
                student,
                sponsorshipBox.getValue(),
                selectedResidency,
                city,
                subcity,
                woreda,
                disabilityField.getText().trim()
            );
            
            showAlert("Phase 1 application submitted!", Alert.AlertType.INFORMATION);
            onRefresh();
            
        } catch (Exception e) {
            showAlert("Failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void onSubmitPhase2() {
        String emergencyName = emergencyNameField.getText().trim();
        String emergencyPhone = emergencyPhoneField.getText().trim();
        
        if (emergencyName.isEmpty() || emergencyPhone.isEmpty()) {
            showAlert("Emergency contact name and phone are required", Alert.AlertType.WARNING);
            return;
        }
        
        // Validate phone number: must start with 09 or 07 and have exactly 10 digits
        if (!emergencyPhone.matches("^(09|07)\\d{8}$")) {
            showAlert("Phone number must start with 09 or 07 and have exactly 10 digits", Alert.AlertType.WARNING);
            return;
        }
        
        if (student.getSponsorshipType() == SponsorshipType.SELF_SPONSORED && 
            transactionIdField.getText().trim().isEmpty()) {
            showAlert("Transaction ID is required for self-sponsored students", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            service.submitPhaseTwoApplication(
                student,
                emergencyName,
                emergencyPhone,
                transactionIdField.getText().trim()
            );
            
            showAlert("Phase 2 application submitted!", Alert.AlertType.INFORMATION);
            onRefresh();
            
        } catch (Exception e) {
            showAlert("Failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void onSendMessage() {
        String recipient = messageRecipientBox.getValue();
        String content = messageField.getText().trim();
        
        if (recipient == null || content.isEmpty()) {
            showAlert("Select a recipient and enter a message", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            content = content.replaceAll("[\\r\\n]+", " ");
            service.sendMessage(student.getUsername(), recipient, content);
            messageField.clear();
            refresh();
            showAlert("Message sent!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
