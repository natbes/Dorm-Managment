package dorm.ui.components;

import dorm.model.Announcement;
import dorm.service.DatabaseDormService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;


public class AnnouncementPane extends VBox {
    
    private final DatabaseDormService service;
    private final String createdBy;
    private final ListView<Announcement> announcementListView;
    private final TextField titleField;
    private final TextArea bodyArea;
    private final Button postButton;
    private Announcement editingAnnouncement;
    private Consumer<String> alertCallback;
    
    /**
     * Create announcement pane
     * @param service The database service
     * @param createdBy Name to use as author when creating announcements
     * @param editable Whether this pane allows creating/editing (false for read-only view)
     */
    public AnnouncementPane(DatabaseDormService service, String createdBy, boolean editable) {
        this.service = service;
        this.createdBy = createdBy;
        this.announcementListView = new ListView<>();
        this.titleField = new TextField();
        this.bodyArea = new TextArea();
        this.postButton = new Button("Post");
        
        buildUI(editable);
    }
    
    private void buildUI(boolean editable) {
        // Configure list view with custom cell factory
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
        
        this.getChildren().add(announcementListView);
        
        if (editable) {
            titleField.setPromptText("Title");
            
            bodyArea.setPrefRowCount(4);
            bodyArea.setPromptText("Content (supports multiple lines)");
            bodyArea.setWrapText(true);
            
            Button editButton = new Button("Edit Selected");
            Button deleteButton = new Button("Delete Selected");
            
            postButton.setOnAction(event -> handlePost());
            editButton.setOnAction(event -> handleEdit());
            deleteButton.setOnAction(event -> handleDelete());
            
            HBox buttons = new HBox(10, postButton, editButton, deleteButton);
            VBox form = new VBox(10, titleField, bodyArea, buttons);
            form.setPadding(new Insets(10));
            
            this.getChildren().add(form);
        }
        
        this.setSpacing(10);
        this.setPadding(new Insets(10));
    }
    
    /**
     * Set callback for showing alerts
     */
    public void setAlertCallback(Consumer<String> callback) {
        this.alertCallback = callback;
    }
    
    /**
     * Refresh the announcement list
     */
    public void refresh() {
        announcementListView.setItems(FXCollections.observableArrayList(service.getAnnouncements()));
    }
    
    private void handlePost() {
        if (titleField.getText().isBlank() || bodyArea.getText().isBlank()) {
            showAlert("Title and content required");
            return;
        }
        
        if (editingAnnouncement != null) {
            // Saving edit
            editingAnnouncement.setTitle(titleField.getText().trim());
            editingAnnouncement.setBody(bodyArea.getText().trim());
            service.updateAnnouncement(editingAnnouncement);
            editingAnnouncement = null;
            postButton.setText("Post");
        } else {
            // New post
            service.addAnnouncement(titleField.getText().trim(), bodyArea.getText().trim(), createdBy);
        }
        
        titleField.clear();
        bodyArea.clear();
        refresh();
    }
    
    private void handleEdit() {
        Announcement selected = announcementListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select an announcement first");
            return;
        }
        editingAnnouncement = selected;
        titleField.setText(selected.getTitle());
        bodyArea.setText(selected.getBody());
        postButton.setText("Save");
    }
    
    private void handleDelete() {
        Announcement selected = announcementListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select an announcement first");
            return;
        }
        service.deleteAnnouncement(selected);
        refresh();
    }
    
    private void showAlert(String message) {
        if (alertCallback != null) {
            alertCallback.accept(message);
        }
    }
}
