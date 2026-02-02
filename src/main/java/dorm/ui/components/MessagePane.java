package dorm.ui.components;

import dorm.model.Message;
import dorm.model.Student;
import dorm.service.DatabaseDormService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class MessagePane extends VBox {
    
    private final DatabaseDormService service;
    private final String currentUsername;
    private final TableView<Message> messagesTable;
    private final TextField studentIdField;
    private final TextField messageField;
    private final Label charCountLabel;
    private Consumer<String> alertCallback;
    
    /**
     * Create message pane for admin/owner
     * @param service The database service
     * @param currentUsername Username of the current admin/owner
     */
    public MessagePane(DatabaseDormService service, String currentUsername) {
        this.service = service;
        this.currentUsername = currentUsername;
        this.messagesTable = new TableView<>();
        this.studentIdField = new TextField();
        this.messageField = new TextField();
        this.charCountLabel = new Label("0/80");
        
        buildUI();
    }
    
    private void buildUI() {
        // Reply form
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
        
        messageField.setPromptText("Type your reply (max 80 characters)");
        messageField.setPrefWidth(400);
        
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
        
        sendButton.setOnAction(event -> handleSend(studentNameLabel));
        
        HBox idRow = new HBox(10, studentIdField, studentNameLabel);
        HBox messageRow = new HBox(10, messageField, charCountLabel);
        VBox form = new VBox(10, new Label("Reply to Student:"), idRow, messageRow, sendButton);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
        
        // Build messages table
        buildMessagesTable();
        
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
        
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.getChildren().addAll(
            new Label("Received Messages (double-click to reply):"),
            messagesTable,
            new Separator(),
            form
        );
    }
    
    private void buildMessagesTable() {
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
                return new SimpleStringProperty(s.getDisplayName() + " (" + s.getStudentId() + ")");
            }
            return new SimpleStringProperty(fromUsername);
        });
        fromCol.setPrefWidth(200);
        
        TableColumn<Message, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getContent()));
        messageCol.setPrefWidth(350);
        
        TableColumn<Message, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> 
            new SimpleStringProperty(
                cell.getValue().getSentAt().toLocalDate().toString() + " " + 
                cell.getValue().getSentAt().toLocalTime().withNano(0).toString()));
        dateCol.setPrefWidth(150);
        
        messagesTable.getColumns().addAll(readCol, fromCol, messageCol, dateCol);
        messagesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    /**
     * Set callback for showing alerts
     */
    public void setAlertCallback(Consumer<String> callback) {
        this.alertCallback = callback;
    }
    
    /**
     * Refresh the messages list
     */
    public void refresh() {
        List<Message> received = service.getMessagesForUser(currentUsername).stream()
            .filter(m -> m.getToUser().equals(currentUsername))
            .collect(Collectors.toList());
        messagesTable.setItems(FXCollections.observableArrayList(received));
    }
    
    private void handleSend(Label studentNameLabel) {
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
        service.sendMessage(currentUsername, student.get().getUsername(), message);
        messageField.clear();
        studentIdField.clear();
        studentNameLabel.setText("");
        refresh();
        showAlert("Message sent to " + student.get().getDisplayName());
    }
    
    private void showAlert(String message) {
        if (alertCallback != null) {
            alertCallback.accept(message);
        }
    }
}
