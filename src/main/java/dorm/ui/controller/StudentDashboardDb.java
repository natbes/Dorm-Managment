

package dorm.ui;

import dorm.model.*;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.stream.Collectors;

public class StudentDashboardDb {
    private final DatabaseDormService service;
    private Student student;  // Not final - can be refreshed
    private final Stage stage;
    private final BorderPane root;
    private final ListView<Announcement> announcementListView;
    private final ListView<String> messageList;
    private TabPane tabs;
    private Label statusLabel;

    public StudentDashboardDb(DatabaseDormService service, Student student, Stage stage) {
        this.service = service;
        this.student = student;
        this.stage = stage;
        this.root = new BorderPane();
        this.announcementListView = new ListView<>();
        this.messageList = new ListView<>();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        Label headerLabel = new Label("Welcome, " + student.getDisplayName() + " (ID: " + student.getStudentId() + ")");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshAll());
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout());
        
        HBox header = new HBox(20, headerLabel, refreshButton, logoutButton);
        header.setPadding(new Insets(10));
        root.setTop(header);

        tabs = new TabPane();
        tabs.getTabs().add(createProfileTab());
        tabs.getTabs().add(createPhaseOneTab());
        tabs.getTabs().add(createPhaseTwoTab());
        tabs.getTabs().add(createAnnouncementsTab());
        tabs.getTabs().add(createMessagesTab());
        
        root.setCenter(tabs);
    }

    private Tab createProfileTab() {
        Tab tab = new Tab("Profile");
        tab.setClosable(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Name:"), new Label(student.getDisplayName()));
        grid.addRow(1, new Label("Student ID:"), new Label(student.getStudentId()));
        grid.addRow(2, new Label("Gender:"), new Label(student.getGender() != null ? student.getGender().name() : "-"));
        grid.addRow(3, new Label("College:"), new Label(student.getCollege() != null ? student.getCollege().getAcronym() : "-"));
        grid.addRow(4, new Label("Sponsorship:"), new Label(student.getSponsorshipType() != null ? student.getSponsorshipType().name() : "-"));
        grid.addRow(5, new Label("Building:"), new Label(student.getAssignedBuilding()));
        
        statusLabel = new Label();
        updateStatusLabel();
        grid.addRow(6, new Label("Status:"), statusLabel);

        tab.setContent(grid);
        return tab;
    }

    private void updateStatusLabel() {
        String status = service.getApplicationForStudent(student)
                .map(app -> app.getStatus().name())
                .orElse("Not Applied");
        statusLabel.setText(status);
    }

    private Tab createPhaseOneTab() {
        Tab tab = new Tab("Phase 1");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        ComboBox<SponsorshipType> sponsorshipBox = new ComboBox<>(FXCollections.observableArrayList(SponsorshipType.values()));
        ComboBox<Residency> residencyBox = new ComboBox<>(FXCollections.observableArrayList(Residency.values()));
        
        // City field (auto-filled for Addis Ababa)
        TextField cityField = new TextField();
        
        // For Addis Ababa: combo boxes
        ComboBox<AddisSubcity> addisSubcityBox = new ComboBox<>(FXCollections.observableArrayList(AddisSubcity.values()));
        ComboBox<Integer> addisWoredaBox = new ComboBox<>();
        
        // For Sheger/Regional: text fields
        TextField subcityField = new TextField();
        subcityField.setPromptText("Enter subcity");
        TextField woredaField = new TextField();
        woredaField.setPromptText("Enter woreda number");
        
        // Container for subcity input (will swap between combo and text)
        HBox subcityContainer = new HBox(10);
        HBox woredaContainer = new HBox(10);
        
        // Update woreda options when Addis subcity changes
        addisSubcityBox.setOnAction(e -> {
            AddisSubcity selected = addisSubcityBox.getValue();
            if (selected != null) {
                addisWoredaBox.getItems().clear();
                for (int i = 1; i <= selected.getWoredaCount(); i++) {
                    addisWoredaBox.getItems().add(i);
                }
            }
        });
        
        // Switch between Addis combo boxes and text fields based on residency
        residencyBox.setOnAction(e -> {
            Residency selected = residencyBox.getValue();
            subcityContainer.getChildren().clear();
            woredaContainer.getChildren().clear();
            
            if (selected == Residency.ADDIS_ABABA) {
                cityField.setText("Addis Ababa");
                cityField.setDisable(true);
                subcityContainer.getChildren().add(addisSubcityBox);
                woredaContainer.getChildren().add(addisWoredaBox);
            } else {
                cityField.setText("");
                cityField.setDisable(false);
                subcityContainer.getChildren().add(subcityField);
                woredaContainer.getChildren().add(woredaField);
            }
        });
        
        TextField disabilityField = new TextField();
        Button submitButton = new Button("Submit Phase 1");
        Label phaseStatusLabel = new Label();

        // Load existing data
        if (student.getSponsorshipType() != null) sponsorshipBox.setValue(student.getSponsorshipType());
        if (student.getResidency() != null) {
            residencyBox.setValue(student.getResidency());
            // Trigger the switch logic
            residencyBox.fireEvent(new javafx.event.ActionEvent());
        }
        if (student.getCity() != null) cityField.setText(student.getCity());
        if (student.getSubcity() != null) {
            subcityField.setText(student.getSubcity());
            // Try to find matching Addis subcity
            for (AddisSubcity as : AddisSubcity.values()) {
                if (as.getDisplayName().equals(student.getSubcity())) {
                    addisSubcityBox.setValue(as);
                    addisSubcityBox.fireEvent(new javafx.event.ActionEvent());
                    break;
                }
            }
        }
        if (student.getWoreda() != null) {
            woredaField.setText(student.getWoreda());
            try {
                int w = Integer.parseInt(student.getWoreda());
                addisWoredaBox.setValue(w);
            } catch (NumberFormatException ignored) {}
        }
        if (student.getDisabilityInfo() != null) disabilityField.setText(student.getDisabilityInfo());
        
        // Initialize containers if residency not set yet
        if (residencyBox.getValue() == null) {
            subcityContainer.getChildren().add(subcityField);
            woredaContainer.getChildren().add(woredaField);
        }

        form.addRow(0, new Label("Sponsorship Type"), sponsorshipBox);
        form.addRow(1, new Label("Residency"), residencyBox);
        form.addRow(2, new Label("City"), cityField);
        form.addRow(3, new Label("Subcity"), subcityContainer);
        form.addRow(4, new Label("Woreda"), woredaContainer);
        form.addRow(5, new Label("Disability (optional)"), disabilityField);
        form.add(submitButton, 1, 6);
        form.add(phaseStatusLabel, 1, 7);

        Optional<DormApplication> existingApp = service.getApplicationForStudent(student);
        if (existingApp.isPresent()) {
            ApplicationStatus appStatus = existingApp.get().getStatus();
            phaseStatusLabel.setText("Status: " + appStatus.name());
            
            if (appStatus != ApplicationStatus.PHASE_ONE_PENDING && 
                appStatus != ApplicationStatus.PHASE_ONE_RESUBMIT) {
                submitButton.setDisable(true);
                sponsorshipBox.setDisable(true);
                residencyBox.setDisable(true);
                cityField.setDisable(true);
                subcityField.setDisable(true);
                woredaField.setDisable(true);
                addisSubcityBox.setDisable(true);
                addisWoredaBox.setDisable(true);
                disabilityField.setDisable(true);
            }
            
            String note = existingApp.get().getAdminNote();
            if (note != null && !note.isBlank()) {
                phaseStatusLabel.setText("Status: " + appStatus.name() + " | Note: " + note);
            }
        }

        submitButton.setOnAction(event -> {
            if (sponsorshipBox.getValue() == null || residencyBox.getValue() == null) {
                showAlert("Sponsorship and Residency are required");
                return;
            }
            
            String subcityValue;
            String woredaValue;
            String cityValue = cityField.getText().trim();
            
            if (residencyBox.getValue() == Residency.ADDIS_ABABA) {
                if (addisSubcityBox.getValue() == null || addisWoredaBox.getValue() == null) {
                    showAlert("Please select subcity and woreda");
                    return;
                }
                subcityValue = addisSubcityBox.getValue().getDisplayName();
                woredaValue = String.valueOf(addisWoredaBox.getValue());
                cityValue = "Addis Ababa";
            } else {
                if (cityField.getText().isBlank() || subcityField.getText().isBlank() || 
                    woredaField.getText().isBlank()) {
                    showAlert("City, Subcity and Woreda are required");
                    return;
                }
                subcityValue = subcityField.getText().trim();
                woredaValue = woredaField.getText().trim();
                
                // Validate woreda is positive integer for non-Addis
                try {
                    int woredaNum = Integer.parseInt(woredaValue);
                    if (woredaNum <= 0) {
                        showAlert("Woreda must be a positive number");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Woreda must be a valid positive number");
                    return;
                }
            }
            
            try {
                service.submitPhaseOneApplication(
                    student,
                    sponsorshipBox.getValue(),
                    residencyBox.getValue(),
                    cityValue,
                    subcityValue,
                    woredaValue,
                    disabilityField.getText().trim()
                );
                phaseStatusLabel.setText("Status: PHASE_ONE_PENDING");
                showAlert("Phase 1 submitted");
                refreshAll();
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        tab.setContent(form);
        return tab;
    }

    private Tab createPhaseTwoTab() {
        Tab tab = new Tab("Phase 2");
        tab.setClosable(false);

        GridPane form = new GridPane();
        form.setPadding(new Insets(20));
        form.setHgap(10);
        form.setVgap(10);

        TextField emergencyNameField = new TextField();
        TextField emergencyPhoneField = new TextField();
        TextField transactionIdField = new TextField();
        Button submitButton = new Button("Submit Phase 2");
        Label phaseStatusLabel = new Label();

        if (student.getEmergencyContactName() != null) emergencyNameField.setText(student.getEmergencyContactName());
        if (student.getEmergencyContactPhone() != null) emergencyPhoneField.setText(student.getEmergencyContactPhone());
        if (student.getTransactionId() != null) transactionIdField.setText(student.getTransactionId());

        form.addRow(0, new Label("Emergency Contact Name"), emergencyNameField);
        form.addRow(1, new Label("Emergency Contact Phone"), emergencyPhoneField);
        
        Label transactionLabel = new Label("Transaction ID (self-sponsored)");
        form.addRow(2, transactionLabel, transactionIdField);
        
        form.add(submitButton, 1, 3);
        form.add(phaseStatusLabel, 1, 4);

        boolean canFillPhaseTwo = service.canFillPhaseTwo(student);
        
        if (!canFillPhaseTwo) {
            phaseStatusLabel.setText("Complete Phase 1 first and wait for approval");
            emergencyNameField.setDisable(true);
            emergencyPhoneField.setDisable(true);
            transactionIdField.setDisable(true);
            submitButton.setDisable(true);
            tab.setDisable(true);
        } else {
            Optional<DormApplication> existingApp = service.getApplicationForStudent(student);
            if (existingApp.isPresent()) {
                ApplicationStatus appStatus = existingApp.get().getStatus();
                if (appStatus == ApplicationStatus.PHASE_TWO_PENDING ||
                    appStatus == ApplicationStatus.PHASE_TWO_APPROVED ||
                    appStatus == ApplicationStatus.PHASE_TWO_DECLINED ||
                    appStatus == ApplicationStatus.ASSIGNED) {
                    phaseStatusLabel.setText("Status: " + appStatus.name());
                    submitButton.setDisable(true);
                    emergencyNameField.setDisable(true);
                    emergencyPhoneField.setDisable(true);
                    transactionIdField.setDisable(true);
                }
            }
        }

        submitButton.setOnAction(event -> {
            if (emergencyNameField.getText().isBlank() || emergencyPhoneField.getText().isBlank()) {
                showAlert("Emergency contact name and phone are required");
                return;
            }
            
            if (student.getSponsorshipType() == SponsorshipType.SELF_SPONSORED && 
                transactionIdField.getText().isBlank()) {
                showAlert("Transaction ID is required for self-sponsored students");
                return;
            }
            
            try {
                service.submitPhaseTwoApplication(
                    student,
                    emergencyNameField.getText().trim(),
                    emergencyPhoneField.getText().trim(),
                    transactionIdField.getText().trim()
                );
                phaseStatusLabel.setText("Status: PHASE_TWO_PENDING");
                showAlert("Phase 2 submitted");
                refreshAll();
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        tab.setContent(form);
        return tab;
    }

    private Tab createAnnouncementsTab() {
        Tab tab = new Tab("Announcements");
        tab.setClosable(false);

        announcementListView.setCellFactory(listView -> new ListCell<Announcement>() {
            @Override
            protected void updateItem(Announcement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label titleLabel = new Label(item.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    Label bodyLabel = new Label(item.getBody());
                    bodyLabel.setWrapText(true);
                    bodyLabel.setMaxWidth(600);
                    
                    Label dateLabel = new Label(item.getCreatedAt().toString() + " by " + item.getCreatedBy());
                    dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                    
                    box.getChildren().addAll(titleLabel, bodyLabel, dateLabel);
                    box.setPadding(new Insets(5));
                    setGraphic(box);
                }
            }
        });

        VBox wrapper = new VBox(10, announcementListView);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createMessagesTab() {
        Tab tab = new Tab("Messages");
        tab.setClosable(false);

        ComboBox<String> adminBox = new ComboBox<>();
        adminBox.setPromptText("Select Admin");
        adminBox.setItems(FXCollections.observableArrayList(
            service.getUsersByRole(Role.ADMIN).stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
        ));
        adminBox.getItems().addAll(
            service.getUsersByRole(Role.OWNER).stream()
                .map(User::getUsername)
                .collect(Collectors.toList())
        );

        TextField messageField = new TextField();
        messageField.setPromptText("Type your message (max 80 characters)");
        
        Label charCountLabel = new Label("0/80");
        charCountLabel.setStyle("-fx-text-fill: gray;");
        
        // Limit to 80 characters and update counter
        messageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 80) {
                messageField.setText(oldVal);
            } else {
                int len = newVal != null ? newVal.length() : 0;
                charCountLabel.setText(len + "/80");
                if (len >= 70) {
                    charCountLabel.setStyle("-fx-text-fill: orange;");
                } else {
                    charCountLabel.setStyle("-fx-text-fill: gray;");
                }
            }
        });
        
        Button sendButton = new Button("Send");

        sendButton.setOnAction(event -> {
            if (adminBox.getValue() == null || messageField.getText().isBlank()) {
                showAlert("Select admin and enter message");
                return;
            }
            try {
                // Sanitize message: remove line breaks and trim
                String message = messageField.getText().trim().replaceAll("[\\r\\n]+", " ");
                service.sendMessage(student.getUsername(), adminBox.getValue(), message);
                messageField.clear();
                refresh();
                showAlert("Message sent");
            } catch (Exception e) {
                showAlert("Failed: " + e.getMessage());
            }
        });

        HBox messageRow = new HBox(10, messageField, charCountLabel);
        messageField.setPrefWidth(400);
        VBox form = new VBox(10, adminBox, messageRow, sendButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, form, new Label("Received Messages:"), messageList);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private void refresh() {
        try {
            announcementListView.setItems(FXCollections.observableArrayList(service.getAnnouncements()));
            messageList.setItems(FXCollections.observableArrayList(
                    service.getMessagesForUser(student.getUsername()).stream()
                            .map(m -> m.getSentAt() + " | " + m.getFromUser() + ": " + m.getContent())
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            showAlert("Refresh failed: " + e.getMessage());
        }
    }

    /**
     * Full refresh - reloads student data and rebuilds UI
     */
    private void refreshAll() {
        try {
            // Reload student from database
            Optional<Student> reloaded = service.findStudentByStudentId(student.getStudentId());
            if (reloaded.isPresent()) {
                this.student = reloaded.get();
            }
            
            // Update status label
            if (statusLabel != null) {
                updateStatusLabel();
            }
            
            // Refresh lists
            refresh();
            
            // Update phase two tab accessibility
            boolean canFillPhaseTwo = service.canFillPhaseTwo(student);
            if (tabs.getTabs().size() > 2) {
                tabs.getTabs().get(2).setDisable(!canFillPhaseTwo);
            }
            
            showAlert("Data refreshed");
        } catch (Exception e) {
            showAlert("Refresh failed: " + e.getMessage());
        }
    }

    private void logout() {
        LoginViewDb loginView = new LoginViewDb(service, stage);
        Scene scene = new Scene(loginView.getRoot(), 1200, 700);
        stage.setScene(scene);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
