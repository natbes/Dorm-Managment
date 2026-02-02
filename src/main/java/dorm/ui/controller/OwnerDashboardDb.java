
package dorm.ui;

import dorm.model.*;
import dorm.service.DatabaseDormService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OwnerDashboardDb {
    private final DatabaseDormService service;
    private final User owner;
    private final Stage stage;
    private final BorderPane root;
    private final TableView<DormApplication> applicationTable;
    private final TableView<User> staffTable;
    private final ListView<Announcement> announcementListView;
    private final Map<String, SimpleBooleanProperty> selectionMap = new HashMap<>();

    public OwnerDashboardDb(DatabaseDormService service, User owner, Stage stage) {
        this.service = service;
        this.owner = owner;
        this.stage = stage;
        this.root = new BorderPane();
        this.applicationTable = new TableView<>();
        this.staffTable = new TableView<>();
        this.announcementListView = new ListView<>();
        build();
        refresh();
    }

    public Parent getRoot() {
        return root;
    }

    private void build() {
        Label headerLabel = new Label("Owner: " + owner.getDisplayName());
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refresh());
        
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(event -> logout());
        
        HBox header = new HBox(20, headerLabel, refreshButton, logoutButton);
        header.setPadding(new Insets(10));
        root.setTop(header);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(createApplicationsTab());
        tabs.getTabs().add(createStaffTab());
        tabs.getTabs().add(createAnnouncementsTab());
        tabs.getTabs().add(createMessagesTab());
        tabs.getTabs().add(createSearchTab());

        root.setCenter(tabs);
    }

    // Filter controls
    private ComboBox<String> filterGender;
    private ComboBox<String> filterResidency;
    private ComboBox<String> filterSubcity;
    private ComboBox<String> filterWoreda;
    private ComboBox<String> filterCollege;
    private ComboBox<String> filterSponsorship;
    private ComboBox<String> filterStatus;
    
    private Tab createApplicationsTab() {
        Tab tab = new Tab("Applications");
        tab.setClosable(false);

        //Filter Controls
        filterGender = new ComboBox<>();
        filterGender.getItems().add("All Genders");
        for (Gender g : Gender.values()) filterGender.getItems().add(g.name());
        filterGender.setValue("All Genders");
        
        filterResidency = new ComboBox<>();
        filterResidency.getItems().add("All Residency");
        for (Residency r : Residency.values()) filterResidency.getItems().add(r.name());
        filterResidency.setValue("All Residency");
        
        filterSubcity = new ComboBox<>();
        filterSubcity.getItems().add("All Subcities");
        filterSubcity.setValue("All Subcities");
        filterSubcity.setEditable(true);
        
        filterWoreda = new ComboBox<>();
        filterWoreda.getItems().add("All Woredas");
        filterWoreda.setValue("All Woredas");
        filterWoreda.setEditable(true);
        
        filterCollege = new ComboBox<>();
        filterCollege.getItems().add("All Colleges");
        for (College c : College.values()) filterCollege.getItems().add(c.getAcronym());
        filterCollege.setValue("All Colleges");
        
        filterSponsorship = new ComboBox<>();
        filterSponsorship.getItems().add("All Sponsorship");
        for (SponsorshipType s : SponsorshipType.values()) filterSponsorship.getItems().add(s.name());
        filterSponsorship.setValue("All Sponsorship");
        
        filterStatus = new ComboBox<>();
        filterStatus.getItems().add("All Status");
        for (ApplicationStatus s : ApplicationStatus.values()) filterStatus.getItems().add(s.name());
        filterStatus.setValue("All Status");
        
        Button applyFilterBtn = new Button("Apply Filter");
        Button clearFilterBtn = new Button("Clear");
        
        applyFilterBtn.setOnAction(e -> applyFilters());
        clearFilterBtn.setOnAction(e -> {
            filterGender.setValue("All Genders");
            filterResidency.setValue("All Residency");
            filterSubcity.setValue("All Subcities");
            filterWoreda.setValue("All Woredas");
            filterCollege.setValue("All Colleges");
            filterSponsorship.setValue("All Sponsorship");
            filterStatus.setValue("All Status");
            applyFilters();
        });
        
        // Update subcity options when residency changes
        filterResidency.setOnAction(e -> {
            filterSubcity.getItems().clear();
            filterSubcity.getItems().add("All Subcities");
            if ("ADDIS_ABABA".equals(filterResidency.getValue())) {
                for (AddisSubcity as : AddisSubcity.values()) {
                    filterSubcity.getItems().add(as.getDisplayName());
                }
            }
            filterSubcity.setValue("All Subcities");
        });
        
        // Update woreda options when subcity changes (for Addis)
        filterSubcity.setOnAction(e -> {
            filterWoreda.getItems().clear();
            filterWoreda.getItems().add("All Woredas");
            String subcity = filterSubcity.getValue();
            if (subcity != null && !"All Subcities".equals(subcity)) {
                for (AddisSubcity as : AddisSubcity.values()) {
                    if (as.getDisplayName().equals(subcity)) {
                        for (int i = 1; i <= as.getWoredaCount(); i++) {
                            filterWoreda.getItems().add(String.valueOf(i));
                        }
                        break;
                    }
                }
            }
            filterWoreda.setValue("All Woredas");
        });
        
        HBox filterRow1 = new HBox(8, 
            new Label("Gender:"), filterGender,
            new Label("Residency:"), filterResidency,
            new Label("Subcity:"), filterSubcity,
            new Label("Woreda:"), filterWoreda);
        filterRow1.setPadding(new Insets(5));
        
        HBox filterRow2 = new HBox(8,
            new Label("College:"), filterCollege,
            new Label("Sponsorship:"), filterSponsorship,
            new Label("Status:"), filterStatus,
            applyFilterBtn, clearFilterBtn);
        filterRow2.setPadding(new Insets(5));
        
        VBox filterBox = new VBox(5, filterRow1, filterRow2);
        filterBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 5;");

        // ===== Table Columns =====
        TableColumn<DormApplication, Boolean> selectCol = new TableColumn<>("Select");
        selectCol.setCellValueFactory(cell -> {
            String id = cell.getValue().getId();
            selectionMap.putIfAbsent(id, new SimpleBooleanProperty(false));
            return selectionMap.get(id);
        });
        selectCol.setCellFactory(col -> new CheckBoxTableCell<>());
        selectCol.setEditable(true);
        selectCol.setPrefWidth(50);

        TableColumn<DormApplication, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getDisplayName()));
        nameCol.setPrefWidth(100);

        TableColumn<DormApplication, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getStudentId()));

        TableColumn<DormApplication, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(cell -> {
            Gender g = cell.getValue().getStudent().getGender();
            return new javafx.beans.property.SimpleStringProperty(g != null ? g.name() : "-");
        });

        TableColumn<DormApplication, String> collegeCol = new TableColumn<>("College");
        collegeCol.setCellValueFactory(cell -> {
            College c = cell.getValue().getStudent().getCollege();
            return new javafx.beans.property.SimpleStringProperty(c != null ? c.getAcronym() : "-");
        });

        TableColumn<DormApplication, String> residencyCol = new TableColumn<>("Residency");
        residencyCol.setCellValueFactory(cell -> {
            Residency r = cell.getValue().getStudent().getResidency();
            return new javafx.beans.property.SimpleStringProperty(r != null ? r.name() : "-");
        });

        TableColumn<DormApplication, String> subcityCol = new TableColumn<>("Subcity");
        subcityCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            safe(cell.getValue().getStudent().getSubcity())));

        TableColumn<DormApplication, String> woredaCol = new TableColumn<>("Woreda");
        woredaCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            safe(cell.getValue().getStudent().getWoreda())));

        TableColumn<DormApplication, String> transactionCol = new TableColumn<>("Transaction ID");
        transactionCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            safe(cell.getValue().getStudent().getTransactionId())));

        TableColumn<DormApplication, String> sponsorCol = new TableColumn<>("Sponsorship");
        sponsorCol.setCellValueFactory(cell -> {
            SponsorshipType type = cell.getValue().getStudent().getSponsorshipType();
            return new javafx.beans.property.SimpleStringProperty(type != null ? type.name() : "-");
        });

        TableColumn<DormApplication, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStatus().name()));

        TableColumn<DormApplication, String> submittedCol = new TableColumn<>("Submitted");
        submittedCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            safe(cell.getValue().getSubmittedDate())));

        TableColumn<DormApplication, String> buildingCol = new TableColumn<>("Building");
        buildingCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
            cell.getValue().getStudent().getAssignedBuilding()));

        applicationTable.getColumns().addAll(selectCol, nameCol, idCol, genderCol, collegeCol, residencyCol, subcityCol, woredaCol, sponsorCol, statusCol, transactionCol, submittedCol, buildingCol);
        applicationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        applicationTable.setEditable(true);

        CheckBox selectAllBox = new CheckBox("Select All");
        selectAllBox.setOnAction(event -> {
            boolean selected = selectAllBox.isSelected();
            for (DormApplication app : applicationTable.getItems()) {
                selectionMap.putIfAbsent(app.getId(), new SimpleBooleanProperty(false));
                selectionMap.get(app.getId()).set(selected);
            }
            applicationTable.refresh();
        });

        Button approveBtn = new Button("Approve");
        Button declineBtn = new Button("Decline");
        Button resubmitBtn = new Button("Resubmit");
        Button exportBtn = new Button("Export CSV");
        
        TextField buildingField = new TextField();
        buildingField.setPromptText("Building");
        buildingField.setPrefWidth(80);
        Button assignBtn = new Button("Assign");

        approveBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            if (selected.isEmpty()) {
                showAlert("Select applications first");
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
            refresh();
            showAlert("Approved " + selected.size() + " applications");
        });

        declineBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            if (selected.isEmpty()) {
                showAlert("Select applications first");
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
            refresh();
            showAlert("Declined " + selected.size() + " applications");
        });

        resubmitBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            if (selected.isEmpty()) {
                showAlert("Select applications first");
                return;
            }
            
            // Prompt for reason
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Resubmit Request");
            dialog.setHeaderText("Request students to resubmit their application");
            dialog.setContentText("Reason/Note:");
            
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty() || result.get().isBlank()) {
                showAlert("Reason is required for resubmit request");
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
                    
                    // Send message to student (sanitize and limit to 80 chars)
                    String message = "Resubmit required: " + reason;
                    message = message.replaceAll("[\\r\\n]+", " ");
                    if (message.length() > 80) {
                        message = message.substring(0, 77) + "...";
                    }
                    service.sendMessage(owner.getUsername(), app.getStudent().getUsername(), message);
                    count++;
                }
            }
            refresh();
            showAlert("Requested " + count + " student(s) to resubmit. Messages sent.");
        });

        assignBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            String building = buildingField.getText().trim();
            if (selected.isEmpty() || building.isEmpty()) {
                showAlert("Select applications and enter building");
                return;
            }
            int count = 0;
            List<String> notReady = new java.util.ArrayList<>();
            
            for (DormApplication app : selected) {
                if (service.isReadyForAssignment(app)) {
                    service.assignBuilding(app.getStudent(), building);
                    count++;
                } else {
                    // Collect names of students who haven't completed Phase 2
                    String studentName = app.getStudent().getDisplayName();
                    String status = app.getStatus().name();
                    notReady.add(studentName + " (" + status + ")");
                }
            }
            refresh();
            
            // Build appropriate message
            if (count > 0 && notReady.isEmpty()) {
                showAlert("Assigned " + count + " student(s) to " + building);
            } else if (count > 0 && !notReady.isEmpty()) {
                showAlert("Assigned " + count + " student(s) to " + building + 
                         "\n\nCould not assign " + notReady.size() + " student(s) - Phase 2 not completed:\n" +
                         String.join("\n", notReady));
            } else {
                // count == 0
                showAlert("No students were assigned.\n\nSelected students have not completed Phase 2:\n" +
                         String.join("\n", notReady));
            }
        });

        exportBtn.setOnAction(event -> {
            List<DormApplication> selected = getSelectedApplications();
            if (selected.isEmpty()) {
                showAlert("Select applications first");
                return;
            }
            exportToCsv(selected);
        });

        HBox actionRow1 = new HBox(10, selectAllBox, approveBtn, declineBtn, resubmitBtn);
        actionRow1.setPadding(new Insets(5));
        
        HBox actionRow2 = new HBox(10, buildingField, assignBtn, exportBtn);
        actionRow2.setPadding(new Insets(5));

        VBox wrapper = new VBox(10, filterBox, applicationTable, actionRow1, actionRow2);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }
    
    /**
     * Apply filters and sort applications
     */
    private void applyFilters() {
        List<DormApplication> all = service.getApplications();
        
        List<DormApplication> filtered = all.stream()
            .filter(app -> {
                Student s = app.getStudent();
                
                String genderFilter = filterGender.getValue();
                if (genderFilter != null && !"All Genders".equals(genderFilter)) {
                    if (s.getGender() == null || !s.getGender().name().equals(genderFilter)) {
                        return false;
                    }
                }
                
                String residencyFilter = filterResidency.getValue();
                if (residencyFilter != null && !"All Residency".equals(residencyFilter)) {
                    if (s.getResidency() == null || !s.getResidency().name().equals(residencyFilter)) {
                        return false;
                    }
                }
                
                String subcityFilter = filterSubcity.getValue();
                if (subcityFilter != null && !"All Subcities".equals(subcityFilter) && !subcityFilter.isEmpty()) {
                    if (s.getSubcity() == null || !s.getSubcity().equalsIgnoreCase(subcityFilter)) {
                        return false;
                    }
                }
                
                String woredaFilter = filterWoreda.getValue();
                if (woredaFilter != null && !"All Woredas".equals(woredaFilter) && !woredaFilter.isEmpty()) {
                    if (s.getWoreda() == null || !s.getWoreda().equals(woredaFilter)) {
                        return false;
                    }
                }
                
                String collegeFilter = filterCollege.getValue();
                if (collegeFilter != null && !"All Colleges".equals(collegeFilter)) {
                    if (s.getCollege() == null || !s.getCollege().getAcronym().equals(collegeFilter)) {
                        return false;
                    }
                }
                
                String sponsorFilter = filterSponsorship.getValue();
                if (sponsorFilter != null && !"All Sponsorship".equals(sponsorFilter)) {
                    if (s.getSponsorshipType() == null || !s.getSponsorshipType().name().equals(sponsorFilter)) {
                        return false;
                    }
                }
                
                String statusFilter = filterStatus.getValue();
                if (statusFilter != null && !"All Status".equals(statusFilter)) {
                    if (!app.getStatus().name().equals(statusFilter)) {
                        return false;
                    }
                }
                
                return true;
            })
            .sorted((a1, a2) -> {
                // ASSIGNED status goes to end
                boolean a1Assigned = a1.getStatus() == ApplicationStatus.ASSIGNED;
                boolean a2Assigned = a2.getStatus() == ApplicationStatus.ASSIGNED;
                
                if (a1Assigned && !a2Assigned) return 1;
                if (!a1Assigned && a2Assigned) return -1;
                
                // Sort by submission date (earliest first)
                String date1 = a1.getSubmittedDate();
                String date2 = a2.getSubmittedDate();
                
                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return 1;
                if (date2 == null) return -1;
                
                return date1.compareTo(date2);
            })
            .collect(Collectors.toList());
        
        applicationTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private List<DormApplication> getSelectedApplications() {
        List<DormApplication> selected = new ArrayList<>();
        for (DormApplication app : applicationTable.getItems()) {
            SimpleBooleanProperty prop = selectionMap.get(app.getId());
            if (prop != null && prop.get()) {
                selected.add(app);
            }
        }
        return selected;
    }

    private void exportToCsv(List<DormApplication> applications) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Name,Student ID,Gender,Sponsorship,Residency,City,Subcity,Woreda,Status,Submitted,Last Response,Building,Transaction ID\n");
            for (DormApplication app : applications) {
                Student s = app.getStudent();
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        s.getDisplayName(),
                        s.getStudentId(),
                        s.getGender() != null ? s.getGender().name() : "-",
                        s.getSponsorshipType() != null ? s.getSponsorshipType().name() : "-",
                        s.getResidency() != null ? s.getResidency().name() : "-",
                        safe(s.getCity()),
                        safe(s.getSubcity()),
                        safe(s.getWoreda()),
                        app.getStatus().name(),
                        safe(app.getSubmittedDate()),
                        safe(app.getLatestResponse()),
                        safe(s.getAssignedBuilding()),
                        safe(s.getTransactionId())));
            }
            showAlert("Exported to " + file.getName());
        } catch (IOException e) {
            showAlert("Export failed: " + e.getMessage());
        }
    }

    private Tab createStaffTab() {
        Tab tab = new Tab("Staff");
        tab.setClosable(false);

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDisplayName()));
        nameCol.setPrefWidth(150);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getUsername()));
        usernameCol.setPrefWidth(120);

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getRole().name()));
        roleCol.setPrefWidth(100);

        staffTable.getColumns().addAll(nameCol, usernameCol, roleCol);
        staffTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button addButton = new Button("Add Admin");
        Button removeButton = new Button("Remove");

        addButton.setOnAction(event -> {
            if (nameField.getText().isBlank() || usernameField.getText().isBlank() || passwordField.getText().isBlank()) {
                showAlert("All fields required");
                return;
            }
            User newAdmin = new User(
                UUID.randomUUID().toString(),
                usernameField.getText().trim(),
                passwordField.getText().trim(),
                Role.ADMIN,
                nameField.getText().trim()
            );
            service.addUser(newAdmin);
            nameField.clear();
            usernameField.clear();
            passwordField.clear();
            refresh();
        });

        removeButton.setOnAction(event -> {
            User selected = staffTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a user first");
                return;
            }
            if (selected.getRole() == Role.OWNER) {
                showAlert("Cannot remove owner");
                return;
            }
            service.removeUser(selected);
            refresh();
        });

        HBox form = new HBox(10, nameField, usernameField, passwordField, addButton, removeButton);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, staffTable, form);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
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
                    bodyLabel.setMaxWidth(500);
                    
                    Label dateLabel = new Label(item.getCreatedAt().toString() + " by " + item.getCreatedBy());
                    dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                    
                    box.getChildren().addAll(titleLabel, bodyLabel, dateLabel);
                    box.setPadding(new Insets(5));
                    setGraphic(box);
                }
            }
        });
        announcementListView.setPrefHeight(300);

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        
        TextArea bodyArea = new TextArea();
        bodyArea.setPrefRowCount(4);
        bodyArea.setPromptText("Content (supports multiple lines)");
        bodyArea.setWrapText(true);
        
        Button postButton = new Button("Post");
        Button editButton = new Button("Edit Selected");
        Button deleteButton = new Button("Delete Selected");

        final Announcement[] editingAnnouncement = {null};

        postButton.setOnAction(event -> {
            if (titleField.getText().isBlank() || bodyArea.getText().isBlank()) {
                showAlert("Title and content required");
                return;
            }
            
            if (editingAnnouncement[0] != null) {
                editingAnnouncement[0].setTitle(titleField.getText().trim());
                editingAnnouncement[0].setBody(bodyArea.getText().trim());
                service.updateAnnouncement(editingAnnouncement[0]);
                editingAnnouncement[0] = null;
                postButton.setText("Post");
            } else {
                service.addAnnouncement(titleField.getText().trim(), bodyArea.getText().trim(), owner.getDisplayName());
            }
            
            titleField.clear();
            bodyArea.clear();
            refresh();
        });

        editButton.setOnAction(event -> {
            Announcement selected = announcementListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select an announcement first");
                return;
            }
            editingAnnouncement[0] = selected;
            titleField.setText(selected.getTitle());
            bodyArea.setText(selected.getBody());
            postButton.setText("Save");
        });

        deleteButton.setOnAction(event -> {
            Announcement selected = announcementListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select an announcement first");
                return;
            }
            service.deleteAnnouncement(selected);
            refresh();
        });

        HBox buttons = new HBox(10, postButton, editButton, deleteButton);
        VBox form = new VBox(10, titleField, bodyArea, buttons);
        form.setPadding(new Insets(10));

        VBox wrapper = new VBox(10, announcementListView, form);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private Tab createMessagesTab() {
        Tab tab = new Tab("Messages");
        tab.setClosable(false);

        // Reply form
        TextField studentIdField = new TextField();
        studentIdField.setPromptText("Student ID");
        
        Label studentNameLabel = new Label();
        studentNameLabel.setStyle("-fx-text-fill: #666;");
        
        // Show student name when ID is entered
        studentIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                Optional<Student> student = service.findStudentByStudentId(newVal.trim());
                if (student.isPresent()) {
                    studentNameLabel.setText("Student: " + student.get().getDisplayName());
                    studentNameLabel.setStyle("-fx-text-fill: green;");
                } else {
                    studentNameLabel.setText("Student not found");
                    studentNameLabel.setStyle("-fx-text-fill: red;");
                }
            } else {
                studentNameLabel.setText("");
            }
        });
        
        TextField messageField = new TextField();
        messageField.setPromptText("Type your reply (max 80 characters)");
        messageField.setPrefWidth(400);
        
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
        
        Button sendButton = new Button("Send Reply");

        sendButton.setOnAction(event -> {
            String studentId = studentIdField.getText().trim();
            if (studentId.isEmpty() || messageField.getText().isBlank()) {
                showAlert("Enter student ID and message");
                return;
            }
            
            Optional<Student> student = service.findStudentByStudentId(studentId);
            if (student.isEmpty()) {
                showAlert("Student not found");
                return;
            }
            
            // Sanitize message: remove line breaks and trim
            String message = messageField.getText().trim().replaceAll("[\\r\\n]+", " ");
            service.sendMessage(owner.getUsername(), student.get().getUsername(), message);
            messageField.clear();
            studentIdField.clear();
            refreshMessages();
            showAlert("Message sent to " + student.get().getDisplayName());
        });

        HBox idRow = new HBox(10, studentIdField, studentNameLabel);
        HBox messageRow = new HBox(10, messageField, charCountLabel);
        VBox form = new VBox(10, new Label("Reply to Student:"), idRow, messageRow, sendButton);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");

        // Messages table with read checkbox
        TableView<Message> messagesTable = new TableView<>();
        messagesTable.setEditable(true);
        
        TableColumn<Message, Boolean> readCol = new TableColumn<>("Read");
        readCol.setCellValueFactory(cell -> {
            SimpleBooleanProperty prop = new SimpleBooleanProperty(cell.getValue().isRead());
            prop.addListener((obs, oldVal, newVal) -> {
                service.markMessageAsRead(cell.getValue(), newVal);
            });
            return prop;
        });
        readCol.setCellFactory(col -> new CheckBoxTableCell<>());
        readCol.setEditable(true);
        readCol.setPrefWidth(50);
        
        TableColumn<Message, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(cell -> {
            String fromUsername = cell.getValue().getFromUser();
            Optional<Student> student = service.findStudentByUsername(fromUsername);
            if (student.isPresent()) {
                Student s = student.get();
                return new javafx.beans.property.SimpleStringProperty(
                    s.getDisplayName() + " (" + s.getStudentId() + ")");
            }
            return new javafx.beans.property.SimpleStringProperty(fromUsername);
        });
        fromCol.setPrefWidth(200);
        
        TableColumn<Message, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(cell.getValue().getContent()));
        messageCol.setPrefWidth(350);
        
        TableColumn<Message, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getSentAt().toLocalDate().toString() + " " + 
                cell.getValue().getSentAt().toLocalTime().withNano(0).toString()));
        dateCol.setPrefWidth(150);
        
        messagesTable.getColumns().addAll(readCol, fromCol, messageCol, dateCol);
        messagesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Double-click to auto-fill student ID for reply
        messagesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Message selected = messagesTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Optional<Student> student = service.findStudentByUsername(selected.getFromUser());
                    if (student.isPresent()) {
                        studentIdField.setText(student.get().getStudentId());
                    }
                }
            }
        });
        
        // Store reference for refresh
        this.messagesTable = messagesTable;

        VBox wrapper = new VBox(10, 
            new Label("Received Messages (double-click to reply):"), 
            messagesTable, 
            new Separator(),
            form);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }
    
    private TableView<Message> messagesTable;
    
    private void refreshMessages() {
        if (messagesTable != null) {
            // Only show messages received by owner (from students)
            List<Message> received = service.getMessagesForUser(owner.getUsername()).stream()
                .filter(m -> m.getToUser().equals(owner.getUsername()))
                .collect(Collectors.toList());
            messagesTable.setItems(FXCollections.observableArrayList(received));
        }
    }

    private Tab createSearchTab() {
        Tab tab = new Tab("Search");
        tab.setClosable(false);

        GridPane searchGrid = new GridPane();
        searchGrid.setPadding(new Insets(10));
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);

        TextField studentIdField = new TextField();
        Button searchButton = new Button("Search");

        searchGrid.addRow(0, new Label("Student ID"), studentIdField, searchButton);

        GridPane editGrid = new GridPane();
        editGrid.setPadding(new Insets(10));
        editGrid.setHgap(10);
        editGrid.setVgap(10);

        TextField nameField = new TextField();
        nameField.setDisable(true);
        ComboBox<Gender> genderBox = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        genderBox.setDisable(true);
        ComboBox<Residency> residencyBox = new ComboBox<>(FXCollections.observableArrayList(Residency.values()));
        residencyBox.setDisable(true);
        TextField cityField = new TextField();
        cityField.setDisable(true);
        TextField subcityField = new TextField();
        subcityField.setDisable(true);
        TextField woredaField = new TextField();
        woredaField.setDisable(true);
        TextField buildingField = new TextField();
        buildingField.setDisable(true);
        Label statusLabel = new Label();
        Label collegeLabel = new Label();
        Label responseHistoryLabel = new Label();
        responseHistoryLabel.setWrapText(true);
        Button saveButton = new Button("Save Changes");
        saveButton.setDisable(true);

        editGrid.addRow(0, new Label("Name"), nameField);
        editGrid.addRow(1, new Label("Gender"), genderBox);
        editGrid.addRow(2, new Label("College"), collegeLabel);
        editGrid.addRow(3, new Label("Residency"), residencyBox);
        editGrid.addRow(4, new Label("City"), cityField);
        editGrid.addRow(5, new Label("Subcity"), subcityField);
        editGrid.addRow(6, new Label("Woreda"), woredaField);
        editGrid.addRow(7, new Label("Building"), buildingField);
        editGrid.addRow(8, new Label("Status"), statusLabel);
        editGrid.addRow(9, new Label("Response History"), responseHistoryLabel);
        editGrid.add(saveButton, 1, 10);

        final Student[] foundStudent = {null};

        searchButton.setOnAction(event -> {
            String id = studentIdField.getText().trim();
            if (id.isBlank()) {
                showAlert("Enter student ID");
                return;
            }
            
            Optional<Student> result = service.findStudentByStudentId(id);
            if (result.isEmpty()) {
                showAlert("Student not found");
                foundStudent[0] = null;
                nameField.clear();
                genderBox.setValue(null);
                residencyBox.setValue(null);
                cityField.clear();
                subcityField.clear();
                woredaField.clear();
                buildingField.clear();
                statusLabel.setText("");
                saveButton.setDisable(true);
                return;
            }
            
            Student s = result.get();
            foundStudent[0] = s;
            
            nameField.setText(s.getDisplayName());
            nameField.setDisable(false);
            genderBox.setValue(s.getGender());
            genderBox.setDisable(false);
            collegeLabel.setText(s.getCollege() != null ? s.getCollege().getFullName() : "-");
            residencyBox.setValue(s.getResidency());
            residencyBox.setDisable(false);
            cityField.setText(safe(s.getCity()));
            cityField.setDisable(false);
            subcityField.setText(safe(s.getSubcity()));
            subcityField.setDisable(false);
            woredaField.setText(safe(s.getWoreda()));
            woredaField.setDisable(false);
            buildingField.setText(s.getAssignedBuilding());
            buildingField.setDisable(false);
            
            Optional<DormApplication> app = service.getApplicationForStudent(s);
            if (app.isPresent()) {
                statusLabel.setText(app.get().getStatus().name());
                String history = app.get().getResponseHistory();
                responseHistoryLabel.setText(history != null && !history.isBlank() ? history.replace(";", "\n") : "-");
            } else {
                statusLabel.setText("No application");
                responseHistoryLabel.setText("-");
            }
            saveButton.setDisable(false);
        });

        saveButton.setOnAction(event -> {
            if (foundStudent[0] == null) return;
            
            Student s = foundStudent[0];
            s.setGender(genderBox.getValue());
            s.setResidency(residencyBox.getValue());
            s.setCity(cityField.getText().trim());
            s.setSubcity(subcityField.getText().trim());
            s.setWoreda(woredaField.getText().trim());
            s.setAssignedBuilding(buildingField.getText().trim());
            
            service.updateStudent(s);
            showAlert("Student updated");
            refresh();
        });

        VBox wrapper = new VBox(10, searchGrid, new Separator(), editGrid);
        wrapper.setPadding(new Insets(10));
        tab.setContent(wrapper);
        return tab;
    }

    private void refresh() {
        applyFilters(); // Apply current filters when refreshing
        staffTable.setItems(FXCollections.observableArrayList(
            service.getUsers().stream()
                .filter(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.OWNER)
                .collect(Collectors.toList())
        ));
        announcementListView.setItems(FXCollections.observableArrayList(service.getAnnouncements()));
        refreshMessages();
    }

    private void logout() {
        LoginViewDb loginView = new LoginViewDb(service, stage);
        Scene scene = new Scene(loginView.getRoot(), 1200, 700);
        stage.setScene(scene);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

