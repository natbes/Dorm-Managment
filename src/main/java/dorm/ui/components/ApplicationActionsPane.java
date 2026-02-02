package dorm.ui.components;

import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;
import dorm.service.DatabaseDormService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class ApplicationActionsPane extends VBox {
    
    private final DatabaseDormService service;
    private final String currentUsername;
    private final TableView<DormApplication> applicationTable;
    private final Map<String, SimpleBooleanProperty> selectionMap;
    private final CheckBox selectAllBox;
    
    private Consumer<String> alertCallback;
    private Runnable onActionCompleted;
    private Supplier<Window> windowSupplier;
    
    public ApplicationActionsPane(DatabaseDormService service, 
                                  String currentUsername,
                                  TableView<DormApplication> applicationTable,
                                  Map<String, SimpleBooleanProperty> selectionMap) {
        this.service = service;
        this.currentUsername = currentUsername;
        this.applicationTable = applicationTable;
        this.selectionMap = selectionMap;
        this.selectAllBox = new CheckBox("Select All");
        
        buildUI();
    }
    
    private void buildUI() {
        // Select all checkbox
        selectAllBox.setOnAction(event -> {
            boolean selected = selectAllBox.isSelected();
            for (DormApplication app : applicationTable.getItems()) {
                selectionMap.putIfAbsent(app.getId(), new SimpleBooleanProperty(false));
                selectionMap.get(app.getId()).set(selected);
            }
            applicationTable.refresh();
        });
        
        // Action buttons
        Button approveBtn = new Button("Approve");
        Button declineBtn = new Button("Decline");
        Button resubmitBtn = new Button("Resubmit");
        Button exportBtn = new Button("Export CSV");
        
        TextField buildingField = new TextField();
        buildingField.setPromptText("Building");
        buildingField.setPrefWidth(80);
        Button assignBtn = new Button("Assign");
        
        // Wire up actions
        approveBtn.setOnAction(e -> handleApprove());
        declineBtn.setOnAction(e -> handleDecline());
        resubmitBtn.setOnAction(e -> handleResubmit());
        assignBtn.setOnAction(e -> handleAssign(buildingField.getText().trim()));
        exportBtn.setOnAction(e -> handleExport());
        
        HBox actionRow1 = new HBox(10, selectAllBox, approveBtn, declineBtn, resubmitBtn);
        actionRow1.setPadding(new Insets(5));
        
        HBox actionRow2 = new HBox(10, buildingField, assignBtn, exportBtn);
        actionRow2.setPadding(new Insets(5));
        
        this.setSpacing(5);
        this.getChildren().addAll(actionRow1, actionRow2);
    }
    
        public void setAlertCallback(Consumer<String> callback) {
        this.alertCallback = callback;
    }
    
    
    public void setOnActionCompleted(Runnable callback) {
        this.onActionCompleted = callback;
    }
    
    
    public void setWindowSupplier(Supplier<Window> supplier) {
        this.windowSupplier = supplier;
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
    
    private void handleApprove() {
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
        
        notifyActionCompleted();
        showAlert("Approved " + selected.size() + " applications");
    }
    
    private void handleDecline() {
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
        
        notifyActionCompleted();
        showAlert("Declined " + selected.size() + " applications");
    }
    
    private void handleResubmit() {
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
                service.sendMessage(currentUsername, app.getStudent().getUsername(), message);
                count++;
            }
        }
        
        notifyActionCompleted();
        showAlert("Requested " + count + " student(s) to resubmit. Messages sent.");
    }
    
    private void handleAssign(String building) {
        List<DormApplication> selected = getSelectedApplications();
        if (selected.isEmpty() || building.isEmpty()) {
            showAlert("Select applications and enter building");
            return;
        }
        
        int count = 0;
        List<String> notReady = new ArrayList<>();
        
        for (DormApplication app : selected) {
            if (service.isReadyForAssignment(app)) {
                service.assignBuilding(app.getStudent(), building);
                count++;
            } else {
                String studentName = app.getStudent().getDisplayName();
                String status = app.getStatus().name();
                notReady.add(studentName + " (" + status + ")");
            }
        }
        
        notifyActionCompleted();
        
        // Build appropriate message
        if (count > 0 && notReady.isEmpty()) {
            showAlert("Assigned " + count + " student(s) to " + building);
        } else if (count > 0 && !notReady.isEmpty()) {
            showAlert("Assigned " + count + " student(s) to " + building + 
                     "\n\nCould not assign " + notReady.size() + " student(s) - Phase 2 not completed:\n" +
                     String.join("\n", notReady));
        } else {
            showAlert("No students were assigned.\n\nSelected students have not completed Phase 2:\n" +
                     String.join("\n", notReady));
        }
    }
    
    private void handleExport() {
        List<DormApplication> selected = getSelectedApplications();
        if (selected.isEmpty()) {
            showAlert("Select applications first");
            return;
        }
        
        Window window = windowSupplier != null ? windowSupplier.get() : null;
        ExportUtil.exportApplicationsToCsv(selected, window);
    }
    
    private void showAlert(String message) {
        if (alertCallback != null) {
            alertCallback.accept(message);
        }
    }
    
    private void notifyActionCompleted() {
        if (onActionCompleted != null) {
            onActionCompleted.run();
        }
    }
}
