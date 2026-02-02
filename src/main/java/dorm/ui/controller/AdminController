package dorm.ui.controller;

import dorm.model.*;
import dorm.service.DatabaseDormService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class AdminController {
    
    @FXML protected Label welcomeLabel;
    @FXML protected TabPane mainTabPane;
    
    // Filters
    @FXML protected ComboBox<String> filterGender;
    @FXML protected ComboBox<String> filterResidency;
    @FXML protected ComboBox<String> filterSubcity;
    @FXML protected ComboBox<String> filterWoreda;
    @FXML protected ComboBox<String> filterCollege;
    @FXML protected ComboBox<String> filterSponsorship;
    @FXML protected ComboBox<String> filterStatus;
    
    // Applications table
    @FXML protected TableView<DormApplication> applicationTable;
    @FXML protected TableColumn<DormApplication, Boolean> selectColumn;
    @FXML protected TableColumn<DormApplication, String> nameColumn;
    @FXML protected TableColumn<DormApplication, String> studentIdColumn;
    @FXML protected TableColumn<DormApplication, String> genderColumn;
    @FXML protected TableColumn<DormApplication, String> collegeColumn;
    @FXML protected TableColumn<DormApplication, String> residencyColumn;
    @FXML protected TableColumn<DormApplication, String> subcityColumn;
    @FXML protected TableColumn<DormApplication, String> woredaColumn;
    @FXML protected TableColumn<DormApplication, String> sponsorshipColumn;
    @FXML protected TableColumn<DormApplication, String> transactionIdColumn;
    @FXML protected TableColumn<DormApplication, String> statusColumn;
    @FXML protected TableColumn<DormApplication, String> buildingColumn;
    
    @FXML protected CheckBox selectAllCheckbox;
    @FXML protected TextField buildingField;
    
    // Announcements
    @FXML protected ListView<Announcement> announcementList;
    @FXML protected Label announcementFormLabel;
    @FXML protected TextField announcementTitleField;
    @FXML protected TextArea announcementBodyArea;
    @FXML protected Button postAnnouncementBtn;
    @FXML protected Button cancelEditBtn;
    
    // Messages
    @FXML protected TableView<Message> messageTable;
    @FXML protected TableColumn<Message, Boolean> msgReadColumn;
    @FXML protected TableColumn<Message, String> msgFromColumn;
    @FXML protected TableColumn<Message, String> msgContentColumn;
    @FXML protected TableColumn<Message, String> msgDateColumn;
    @FXML protected TextField replyStudentIdField;
    @FXML protected Label replyStudentNameLabel;
    @FXML protected TextField replyMessageField;
    @FXML protected Label replyCharCountLabel;
    
    // Search
    @FXML protected TextField searchStudentIdField;
    @FXML protected VBox searchResultPane;
    @FXML protected TextField searchNameField;
    @FXML protected ComboBox<Gender> searchGenderBox;
    @FXML protected Label searchCollegeLabel;
    @FXML protected ComboBox<Residency> searchResidencyBox;
    @FXML protected TextField searchCityField;
    @FXML protected TextField searchSubcityField;
    @FXML protected TextField searchWoredaField;
    @FXML protected TextField searchBuildingField;
    @FXML protected Label searchStatusLabel;
    
    protected DatabaseDormService service;
    protected User user;
    protected Map<String, SimpleBooleanProperty> selectionMap = new HashMap<>();
    protected Announcement editingAnnouncement = null;
    protected Student foundStudent = null;
    
    public void setService(DatabaseDormService service) {
        this.service = service;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void initialize() {
        if (service == null || user == null) return;
        
        welcomeLabel.setText("Admin: " + user.getDisplayName());
        
        setupFilters();
        setupApplicationTable();
        setupAnnouncements();
        setupMessages();
        setupSearch();
        refresh();
    }
    
    protected void setupFilters() {
        filterGender.getItems().add("All Genders");
        for (Gender g : Gender.values()) filterGender.getItems().add(g.name());
        filterGender.setValue("All Genders");
        
        filterResidency.getItems().add("All Residency");
        for (Residency r : Residency.values()) filterResidency.getItems().add(r.name());
        filterResidency.setValue("All Residency");
        
        filterSubcity.getItems().add("All Subcities");
        filterSubcity.setValue("All Subcities");
        
        if (filterWoreda != null) {
            filterWoreda.getItems().add("All Woredas");
            filterWoreda.setValue("All Woredas");
        }
        
        filterCollege.getItems().add("All Colleges");
        for (College c : College.values()) filterCollege.getItems().add(c.getAcronym());
        filterCollege.setValue("All Colleges");
        
        filterSponsorship.getItems().add("All Sponsorship");
        for (SponsorshipType s : SponsorshipType.values()) filterSponsorship.getItems().add(s.name());
        filterSponsorship.setValue("All Sponsorship");
        
        filterStatus.getItems().add("All Status");
        for (ApplicationStatus s : ApplicationStatus.values()) filterStatus.getItems().add(s.name());
        filterStatus.setValue("All Status");
        
        // Update subcity and woreda options when residency changes
        filterResidency.setOnAction(e -> {
            filterSubcity.getItems().clear();
            filterSubcity.getItems().add("All Subcities");
            if (filterWoreda != null) {
                filterWoreda.getItems().clear();
                filterWoreda.getItems().add("All Woredas");
                filterWoreda.setValue("All Woredas");
            }
            if ("ADDIS_ABABA".equals(filterResidency.getValue())) {
                for (AddisSubcity as : AddisSubcity.values()) {
                    filterSubcity.getItems().add(as.getDisplayName());
                }
            }
            filterSubcity.setValue("All Subcities");
        });
        
        // Update woreda options when subcity changes (for Addis Ababa)
        filterSubcity.setOnAction(e -> {
            if (filterWoreda != null && "ADDIS_ABABA".equals(filterResidency.getValue())) {
                filterWoreda.getItems().clear();
                filterWoreda.getItems().add("All Woredas");
                String selectedSubcity = filterSubcity.getValue();
                if (selectedSubcity != null && !"All Subcities".equals(selectedSubcity)) {
                    for (AddisSubcity as : AddisSubcity.values()) {
                        if (as.getDisplayName().equals(selectedSubcity)) {
                            for (int i = 1; i <= as.getWoredaCount(); i++) {
                                filterWoreda.getItems().add(String.valueOf(i));
                            }
                            break;
                        }
                    }
                }
                filterWoreda.setValue("All Woredas");
            }
        });
    }
    
    protected void setupApplicationTable() {
        applicationTable.setEditable(true);
        applicationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        selectColumn.setCellValueFactory(cell -> {
            String id = cell.getValue().getId();
            selectionMap.putIfAbsent(id, new SimpleBooleanProperty(false));
            return selectionMap.get(id);
        });
        selectColumn.setCellFactory(col -> new CheckBoxTableCell<>());
        selectColumn.setEditable(true);
        
        nameColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getStudent().getDisplayName()));
        centerColumnText(nameColumn);
        
        studentIdColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getStudent().getStudentId()));
        centerColumnText(studentIdColumn);
        
        genderColumn.setCellValueFactory(cell -> {
            Gender g = cell.getValue().getStudent().getGender();
            return new SimpleStringProperty(g != null ? g.name() : "-");
        });
        centerColumnText(genderColumn);
        
        collegeColumn.setCellValueFactory(cell -> {
            College c = cell.getValue().getStudent().getCollege();
            return new SimpleStringProperty(c != null ? c.getAcronym() : "-");
        });
        centerColumnText(collegeColumn);
        
        residencyColumn.setCellValueFactory(cell -> {
            Residency r = cell.getValue().getStudent().getResidency();
            return new SimpleStringProperty(r != null ? r.name() : "-");
        });
        centerColumnText(residencyColumn);
        
        if (subcityColumn != null) {
            subcityColumn.setCellValueFactory(cell -> {
                String subcity = cell.getValue().getStudent().getSubcity();
                return new SimpleStringProperty(subcity != null ? subcity : "-");
            });
            centerColumnText(subcityColumn);
        }
        
        if (woredaColumn != null) {
            woredaColumn.setCellValueFactory(cell -> {
                String woreda = cell.getValue().getStudent().getWoreda();
                return new SimpleStringProperty(woreda != null ? woreda : "-");
            });
            centerColumnText(woredaColumn);
        }
        
        sponsorshipColumn.setCellValueFactory(cell -> {
            SponsorshipType s = cell.getValue().getStudent().getSponsorshipType();
            return new SimpleStringProperty(s != null ? s.name() : "-");
        });
        centerColumnText(sponsorshipColumn);
        
        if (transactionIdColumn != null) {
            transactionIdColumn.setCellValueFactory(cell -> {
                String transId = cell.getValue().getStudent().getTransactionId();
                return new SimpleStringProperty(transId != null && !transId.isEmpty() ? transId : "-");
            });
            centerColumnText(transactionIdColumn);
        }
        
        statusColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getStatus().name()));
        centerColumnText(statusColumn);
        
        buildingColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getStudent().getAssignedBuilding()));
        centerColumnText(buildingColumn);
    }
    
    protected <T> void centerColumnText(TableColumn<T, String> column) {
        column.setCellFactory(tc -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }
    
    protected void setupAnnouncements() {
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
                    body.setMaxWidth(500);
                    
                    Label date = new Label(item.getCreatedAt().toString() + " by " + item.getCreatedBy());
                    date.getStyleClass().add("announcement-date");
                    
                    box.getChildren().addAll(title, body, date);
                    setGraphic(box);
                }
            }
        });
    }
    
    protected void setupMessages() {
        messageTable.setEditable(true);
        
        msgReadColumn.setCellValueFactory(cell -> {
            SimpleBooleanProperty prop = new SimpleBooleanProperty(cell.getValue().isRead());
            prop.addListener((obs, old, newVal) -> service.markMessageAsRead(cell.getValue(), newVal));
            return prop;
        });
        msgReadColumn.setCellFactory(col -> new CheckBoxTableCell<>());
        msgReadColumn.setEditable(true);
        
        msgFromColumn.setCellValueFactory(cell -> {
            String from = cell.getValue().getFromUser();
            Optional<Student> s = service.findStudentByUsername(from);
            if (s.isPresent()) {
                return new SimpleStringProperty(s.get().getDisplayName() + " (" + s.get().getStudentId() + ")");
            }
            return new SimpleStringProperty(from);
        });
        
        msgContentColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getContent()));
        
        msgDateColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getSentAt().toLocalDate() + " " + 
                cell.getValue().getSentAt().toLocalTime().withNano(0)));
        
        // Double-click to reply
        messageTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Message msg = messageTable.getSelectionModel().getSelectedItem();
                if (msg != null) {
                    Optional<Student> s = service.findStudentByUsername(msg.getFromUser());
                    if (s.isPresent()) {
                        replyStudentIdField.setText(s.get().getStudentId());
                    }
                }
            }
        });
        
        // Student name lookup
        replyStudentIdField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                Optional<Student> s = service.findStudentByStudentId(newVal.trim());
                if (s.isPresent()) {
                    replyStudentNameLabel.setText("Student: " + s.get().getDisplayName());
                    replyStudentNameLabel.setStyle("-fx-text-fill: green;");
                } else {
                    replyStudentNameLabel.setText("Student not found");
                    replyStudentNameLabel.setStyle("-fx-text-fill: red;");
                }
            } else {
                replyStudentNameLabel.setText("");
            }
        });
        
        // Character counter
        replyMessageField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 80) {
                replyMessageField.setText(old);
            } else {
                int len = newVal != null ? newVal.length() : 0;
                replyCharCountLabel.setText(len + "/80");
            }
        });
        
        // Enter key to send reply
        replyMessageField.setOnAction(e -> onSendReply());
    }
    
    protected void setupSearch() {
        searchGenderBox.setItems(FXCollections.observableArrayList(Gender.values()));
        searchResidencyBox.setItems(FXCollections.observableArrayList(Residency.values()));
    }
    
    protected void refresh() {
        applyFilters();
        announcementList.setItems(FXCollections.observableArrayList(service.getAnnouncements()));
        refreshMessages();
    }
    
    protected void refreshMessages() {
        List<Message> received = service.getMessagesForUser(user.getUsername()).stream()
            .filter(m -> m.getToUser().equals(user.getUsername()))
            .collect(Collectors.toList());
        messageTable.setItems(FXCollections.observableArrayList(received));
    }
    
    protected void applyFilters() {
        List<DormApplication> all = service.getApplications();
        
        List<DormApplication> filtered = all.stream()
            .filter(app -> {
                Student s = app.getStudent();
                
                if (!matchesFilter(filterGender.getValue(), "All Genders", 
                    s.getGender() != null ? s.getGender().name() : null)) return false;
                    
                if (!matchesFilter(filterResidency.getValue(), "All Residency",
                    s.getResidency() != null ? s.getResidency().name() : null)) return false;
                    
                if (!matchesFilter(filterSubcity.getValue(), "All Subcities", s.getSubcity())) return false;
                
                if (filterWoreda != null && !matchesFilter(filterWoreda.getValue(), "All Woredas", s.getWoreda())) return false;
                
                if (!matchesFilter(filterCollege.getValue(), "All Colleges",
                    s.getCollege() != null ? s.getCollege().getAcronym() : null)) return false;
                    
                if (!matchesFilter(filterSponsorship.getValue(), "All Sponsorship",
                    s.getSponsorshipType() != null ? s.getSponsorshipType().name() : null)) return false;
                    
                if (!matchesFilter(filterStatus.getValue(), "All Status", app.getStatus().name())) return false;
                
                return true;
            })
            .sorted((a1, a2) -> {
                boolean a1Assigned = a1.getStatus() == ApplicationStatus.ASSIGNED;
                boolean a2Assigned = a2.getStatus() == ApplicationStatus.ASSIGNED;
                if (a1Assigned != a2Assigned) return a1Assigned ? 1 : -1;
                
                String d1 = a1.getSubmittedDate();
                String d2 = a2.getSubmittedDate();
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                return d1.compareTo(d2);
            })
            .collect(Collectors.toList());
        
        applicationTable.setItems(FXCollections.observableArrayList(filtered));
    }
    
    protected boolean matchesFilter(String filter, String allValue, String actual) {
        if (filter == null || filter.equals(allValue) || filter.isEmpty()) return true;
        return filter.equalsIgnoreCase(actual);
    }
    
    protected List<DormApplication> getSelectedApplications() {
        List<DormApplication> selected = new ArrayList<>();
        for (DormApplication app : applicationTable.getItems()) {
            SimpleBooleanProperty prop = selectionMap.get(app.getId());
            if (prop != null && prop.get()) {
                selected.add(app);
            }
        }
        return selected;
    }
    
    protected void clearSelections() {
        for (SimpleBooleanProperty prop : selectionMap.values()) {
            prop.set(false);
        }
        selectAllCheckbox.setSelected(false);
        applicationTable.refresh();
    }
    
    @FXML
    protected void onRefresh() {
        refresh();
        showAlert("Data refreshed", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    protected void onLogout() {
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
    protected void onApplyFilter() {
        applyFilters();
    }
    
    @FXML
    protected void onClearFilter() {
        filterGender.setValue("All Genders");
        filterResidency.setValue("All Residency");
        filterSubcity.setValue("All Subcities");
        if (filterWoreda != null) filterWoreda.setValue("All Woredas");
        filterCollege.setValue("All Colleges");
        filterSponsorship.setValue("All Sponsorship");
        filterStatus.setValue("All Status");
        applyFilters();
    }
    
    @FXML
    protected void onSelectAll() {
        boolean selected = selectAllCheckbox.isSelected();
        for (DormApplication app : applicationTable.getItems()) {
            selectionMap.putIfAbsent(app.getId(), new SimpleBooleanProperty(false));
            selectionMap.get(app.getId()).set(selected);
        }
        applicationTable.refresh();
    }
    
    @FXML
    protected void onApprove() {
        List<DormApplication> selected = getSelectedApplications();
        if (selected.isEmpty()) {
            showAlert("Select applications first", Alert.AlertType.WARNING);
            return;
        }
        
        for (DormApplication app : selected) {
            ApplicationStatus status = app.getStatus();
            if (status == ApplicationStatus.PHASE_ONE_PENDING || 
                status == ApplicationStatus.PHASE_ONE_DECLINED ||
                status == ApplicationStatus.PHASE_ONE_RESUBMIT) {
                service.approvePhaseOne(app, "");
            } else if (status == ApplicationStatus.PHASE_TWO_PENDING ||
                       status == ApplicationStatus.PHASE_TWO_DECLINED) {
                service.approvePhaseTwoApplication(app, "");
            }
        }
        
        clearSelections();
        refresh();
        showAlert("Approved " + selected.size() + " applications", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    protected void onDecline() {
        List<DormApplication> selected = getSelectedApplications();
        if (selected.isEmpty()) {
            showAlert("Select applications first", Alert.AlertType.WARNING);
            return;
        }
        
        for (DormApplication app : selected) {
            ApplicationStatus status = app.getStatus();
            if (status == ApplicationStatus.PHASE_ONE_PENDING ||
                status == ApplicationStatus.PHASE_ONE_APPROVED ||
                status == ApplicationStatus.PHASE_ONE_RESUBMIT) {
                service.declinePhaseOne(app, "");
            } else if (status == ApplicationStatus.PHASE_TWO_PENDING ||
                       status == ApplicationStatus.PHASE_TWO_APPROVED) {
                service.declinePhaseTwoApplication(app, "");
            }
        }
        
        clearSelections();
        refresh();
        showAlert("Declined " + selected.size() + " applications", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    protected void onResubmit() {
        List<DormApplication> selected = getSelectedApplications();
        if (selected.isEmpty()) {
            showAlert("Select applications first", Alert.AlertType.WARNING);
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Resubmit Request");
        dialog.setHeaderText("Request students to resubmit");
        dialog.setContentText("Reason:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) {
            showAlert("Reason is required", Alert.AlertType.WARNING);
            return;
        }
        
        String reason = result.get().trim();
        int count = 0;
        
        for (DormApplication app : selected) {
            ApplicationStatus status = app.getStatus();
            if (status == ApplicationStatus.PHASE_ONE_PENDING ||
                status == ApplicationStatus.PHASE_ONE_DECLINED ||
                status == ApplicationStatus.PHASE_ONE_APPROVED) {
                
                service.requestResubmit(app, reason);
                
                String message = "Resubmit required: " + reason;
                if (message.length() > 80) message = message.substring(0, 77) + "...";
                service.sendMessage(user.getUsername(), app.getStudent().getUsername(), message);
                count++;
            }
        }
        
        clearSelections();
        refresh();
        showAlert("Requested " + count + " resubmissions", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    protected void onAssign() {
        List<DormApplication> selected = getSelectedApplications();
        String building = buildingField.getText().trim();
        
        if (selected.isEmpty() || building.isEmpty()) {
            showAlert("Select applications and enter building", Alert.AlertType.WARNING);
            return;
        }
        
        int count = 0;
        for (DormApplication app : selected) {
            if (service.isReadyForAssignment(app)) {
                service.assignBuilding(app.getStudent(), building);
                count++;
            }
        }
        
        clearSelections();
        refresh();
        showAlert("Assigned " + count + " students to " + building, Alert.AlertType.INFORMATION);
    }
    
    @FXML
    protected void onExport() {
        List<DormApplication> selected = getSelectedApplications();
        if (selected.isEmpty()) {
            showAlert("Select applications first", Alert.AlertType.WARNING);
            return;
        }
        
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(applicationTable.getScene().getWindow());
        if (file == null) return;
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Name,Student ID,Gender,Sponsorship,Residency,Subcity,Woreda,Transaction ID,Status,Building\n");
            for (DormApplication app : selected) {
                Student s = app.getStudent();
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    escapeCsv(s.getDisplayName()),
                    escapeCsv(s.getStudentId()),
                    s.getGender() != null ? s.getGender().name() : "-",
                    s.getSponsorshipType() != null ? s.getSponsorshipType().name() : "-",
                    s.getResidency() != null ? s.getResidency().name() : "-",
                    escapeCsv(s.getSubcity() != null ? s.getSubcity() : "-"),
                    escapeCsv(s.getWoreda() != null ? s.getWoreda() : "-"),
                    escapeCsv(s.getTransactionId() != null && !s.getTransactionId().isEmpty() ? s.getTransactionId() : "-"),
                    app.getStatus().name(),
                    escapeCsv(s.getAssignedBuilding())
                ));
            }
            clearSelections();
            showAlert("Exported to " + file.getName(), Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showAlert("Export failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    protected String escapeCsv(String value) {
        if (value == null) return "-";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    @FXML
    protected void onPostAnnouncement() {
        String title = announcementTitleField.getText().trim();
        String body = announcementBodyArea.getText().trim();
        
        if (title.isEmpty() || body.isEmpty()) {
            showAlert("Title and content are required", Alert.AlertType.WARNING);
            return;
        }
        
        if (editingAnnouncement != null) {
            editingAnnouncement.setTitle(title);
            editingAnnouncement.setBody(body);
            service.updateAnnouncement(editingAnnouncement);
            editingAnnouncement = null;
            postAnnouncementBtn.setText("Post");
            cancelEditBtn.setVisible(false);
        } else {
            service.addAnnouncement(title, body, user.getDisplayName());
        }
        
        announcementTitleField.clear();
        announcementBodyArea.clear();
        refresh();
    }
    
    @FXML
    protected void onEditAnnouncement() {
        Announcement selected = announcementList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select an announcement first", Alert.AlertType.WARNING);
            return;
        }
        
        editingAnnouncement = selected;
        announcementTitleField.setText(selected.getTitle());
        announcementBodyArea.setText(selected.getBody());
        postAnnouncementBtn.setText("Save");
        cancelEditBtn.setVisible(true);
    }
    
    @FXML
    protected void onDeleteAnnouncement() {
        Announcement selected = announcementList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select an announcement first", Alert.AlertType.WARNING);
            return;
        }
        
        service.deleteAnnouncement(selected);
        refresh();
    }
    
    @FXML
    protected void onCancelEdit() {
        editingAnnouncement = null;
        announcementTitleField.clear();
        announcementBodyArea.clear();
        postAnnouncementBtn.setText("Post");
        cancelEditBtn.setVisible(false);
    }
    
    @FXML
    protected void onSendReply() {
        String studentId = replyStudentIdField.getText().trim();
        String message = replyMessageField.getText().trim();
        
        if (studentId.isEmpty() || message.isEmpty()) {
            showAlert("Enter student ID and message", Alert.AlertType.WARNING);
            return;
        }
        
        Optional<Student> student = service.findStudentByStudentId(studentId);
        if (student.isEmpty()) {
            showAlert("Student not found", Alert.AlertType.WARNING);
            return;
        }
        
        service.sendMessage(user.getUsername(), student.get().getUsername(), message);
        replyMessageField.clear();
        replyStudentIdField.clear();
        refreshMessages();
        showAlert("Message sent!", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    protected void onSearch() {
        String studentId = searchStudentIdField.getText().trim();
        if (studentId.isEmpty()) {
            showAlert("Enter student ID", Alert.AlertType.WARNING);
            return;
        }
        
        Optional<Student> result = service.findStudentByStudentId(studentId);
        if (result.isEmpty()) {
            showAlert("Student not found", Alert.AlertType.WARNING);
            searchResultPane.setVisible(false);
            foundStudent = null;
            return;
        }
        
        foundStudent = result.get();
        searchResultPane.setVisible(true);
        
        searchNameField.setText(foundStudent.getDisplayName());
        searchGenderBox.setValue(foundStudent.getGender());
        searchCollegeLabel.setText(foundStudent.getCollege() != null ? foundStudent.getCollege().getFullName() : "-");
        searchResidencyBox.setValue(foundStudent.getResidency());
        searchCityField.setText(foundStudent.getCity() != null ? foundStudent.getCity() : "");
        searchSubcityField.setText(foundStudent.getSubcity() != null ? foundStudent.getSubcity() : "");
        searchWoredaField.setText(foundStudent.getWoreda() != null ? foundStudent.getWoreda() : "");
        searchBuildingField.setText(foundStudent.getAssignedBuilding());
        
        Optional<DormApplication> app = service.getApplicationForStudent(foundStudent);
        searchStatusLabel.setText(app.map(a -> a.getStatus().name()).orElse("No application"));
    }
    
    @FXML
    protected void onSaveStudent() {
        if (foundStudent == null) return;
        
        foundStudent.setGender(searchGenderBox.getValue());
        foundStudent.setResidency(searchResidencyBox.getValue());
        foundStudent.setCity(searchCityField.getText().trim());
        foundStudent.setSubcity(searchSubcityField.getText().trim());
        foundStudent.setWoreda(searchWoredaField.getText().trim());
        foundStudent.setAssignedBuilding(searchBuildingField.getText().trim());
        
        service.updateStudent(foundStudent);
        refresh();
        showAlert("Student updated", Alert.AlertType.INFORMATION);
    }
    
    protected void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
