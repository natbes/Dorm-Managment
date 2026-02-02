package dorm.ui.components;

import dorm.model.*;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.function.Consumer;


public class StudentSearchPane extends VBox {
    
    private final DatabaseDormService service;
    private final TextField studentIdField;
    private final TextField nameField;
    private final ComboBox<Gender> genderBox;
    private final ComboBox<Residency> residencyBox;
    private final Label collegeLabel;
    private final TextField cityField;
    private final TextField subcityField;
    private final TextField woredaField;
    private final TextField buildingField;
    private final Label statusLabel;
    private final Label responseHistoryLabel;
    private final Button saveButton;
    
    private Student foundStudent;
    private Consumer<String> alertCallback;
    private Runnable onSaveCallback;
    
    public StudentSearchPane(DatabaseDormService service) {
        this.service = service;
        
        // Initialize controls
        this.studentIdField = new TextField();
        this.nameField = new TextField();
        this.genderBox = new ComboBox<>(FXCollections.observableArrayList(Gender.values()));
        this.residencyBox = new ComboBox<>(FXCollections.observableArrayList(Residency.values()));
        this.collegeLabel = new Label();
        this.cityField = new TextField();
        this.subcityField = new TextField();
        this.woredaField = new TextField();
        this.buildingField = new TextField();
        this.statusLabel = new Label();
        this.responseHistoryLabel = new Label();
        this.saveButton = new Button("Save Changes");
        
        buildUI();
    }
    
    private void buildUI() {
        // Search section
        GridPane searchGrid = new GridPane();
        searchGrid.setPadding(new Insets(10));
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);
        
        Button searchButton = new Button("Search");
        searchGrid.addRow(0, new Label("Student ID"), studentIdField, searchButton);
        
        // Edit section
        GridPane editGrid = new GridPane();
        editGrid.setPadding(new Insets(10));
        editGrid.setHgap(10);
        editGrid.setVgap(10);
        
        // Initially disable edit fields
        setFieldsEnabled(false);
        
        responseHistoryLabel.setWrapText(true);
        
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
        
        // Wire up actions
        searchButton.setOnAction(e -> handleSearch());
        saveButton.setOnAction(e -> handleSave());
        
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.getChildren().addAll(searchGrid, new Separator(), editGrid);
    }
    
    private void setFieldsEnabled(boolean enabled) {
        nameField.setDisable(!enabled);
        genderBox.setDisable(!enabled);
        residencyBox.setDisable(!enabled);
        cityField.setDisable(!enabled);
        subcityField.setDisable(!enabled);
        woredaField.setDisable(!enabled);
        buildingField.setDisable(!enabled);
        saveButton.setDisable(!enabled);
    }
    
    private void clearFields() {
        nameField.clear();
        genderBox.setValue(null);
        collegeLabel.setText("");
        residencyBox.setValue(null);
        cityField.clear();
        subcityField.clear();
        woredaField.clear();
        buildingField.clear();
        statusLabel.setText("");
        responseHistoryLabel.setText("");
    }
    
    private void handleSearch() {
        String id = studentIdField.getText().trim();
        if (id.isBlank()) {
            showAlert("Enter student ID");
            return;
        }
        
        Optional<Student> result = service.findStudentByStudentId(id);
        if (result.isEmpty()) {
            showAlert("Student not found");
            foundStudent = null;
            clearFields();
            setFieldsEnabled(false);
            return;
        }
        
        Student s = result.get();
        foundStudent = s;
        
        // Populate fields
        nameField.setText(s.getDisplayName());
        genderBox.setValue(s.getGender());
        collegeLabel.setText(s.getCollege() != null ? s.getCollege().getFullName() : "-");
        residencyBox.setValue(s.getResidency());
        cityField.setText(safe(s.getCity()));
        subcityField.setText(safe(s.getSubcity()));
        woredaField.setText(safe(s.getWoreda()));
        buildingField.setText(s.getAssignedBuilding() != null ? s.getAssignedBuilding() : "");
        
        Optional<DormApplication> app = service.getApplicationForStudent(s);
        if (app.isPresent()) {
            statusLabel.setText(app.get().getStatus().name());
            String history = app.get().getResponseHistory();
            responseHistoryLabel.setText(history != null && !history.isBlank() ? history.replace(";", "\n") : "-");
        } else {
            statusLabel.setText("No application");
            responseHistoryLabel.setText("-");
        }
        
        setFieldsEnabled(true);
    }
    
    private void handleSave() {
        if (foundStudent == null) return;
        
        foundStudent.setGender(genderBox.getValue());
        foundStudent.setResidency(residencyBox.getValue());
        foundStudent.setCity(cityField.getText().trim());
        foundStudent.setSubcity(subcityField.getText().trim());
        foundStudent.setWoreda(woredaField.getText().trim());
        foundStudent.setAssignedBuilding(buildingField.getText().trim());
        
        service.updateStudent(foundStudent);
        showAlert("Student updated");
        
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
    }
    
    /**
     * Set callback for showing alerts
     */
    public void setAlertCallback(Consumer<String> callback) {
        this.alertCallback = callback;
    }
    
    /**
     * Set callback for when save is completed
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
    
    private void showAlert(String message) {
        if (alertCallback != null) {
            alertCallback.accept(message);
        }
    }
    
    private String safe(String value) {
        return value == null || value.isBlank() ? "" : value;
    }
}
