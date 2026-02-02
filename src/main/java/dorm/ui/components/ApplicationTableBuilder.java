package dorm.ui.components;

import dorm.model.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.util.Map;


public class ApplicationTableBuilder {
    
    private final TableView<DormApplication> table;
    private final Map<String, SimpleBooleanProperty> selectionMap;
    
    public ApplicationTableBuilder(TableView<DormApplication> table, 
                                   Map<String, SimpleBooleanProperty> selectionMap) {
        this.table = table;
        this.selectionMap = selectionMap;
    }
    
    
    public void buildAllColumns() {
        table.getColumns().clear();
        
        table.getColumns().add(createSelectColumn());
        table.getColumns().add(createNameColumn());
        table.getColumns().add(createStudentIdColumn());
        table.getColumns().add(createGenderColumn());
        table.getColumns().add(createCollegeColumn());
        table.getColumns().add(createResidencyColumn());
        table.getColumns().add(createSubcityColumn());
        table.getColumns().add(createWoredaColumn());
        table.getColumns().add(createSponsorshipColumn());
        table.getColumns().add(createStatusColumn());
        table.getColumns().add(createTransactionColumn());
        table.getColumns().add(createSubmittedColumn());
        table.getColumns().add(createBuildingColumn());
        
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setEditable(true);
    }
    
    private TableColumn<DormApplication, Boolean> createSelectColumn() {
        TableColumn<DormApplication, Boolean> col = new TableColumn<>("Select");
        col.setCellValueFactory(cell -> {
            String id = cell.getValue().getId();
            selectionMap.putIfAbsent(id, new SimpleBooleanProperty(false));
            return selectionMap.get(id);
        });
        col.setCellFactory(c -> new CheckBoxTableCell<>());
        col.setEditable(true);
        col.setPrefWidth(50);
        return col;
    }
    
    private TableColumn<DormApplication, String> createNameColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Name");
        col.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getStudent().getDisplayName()));
        col.setPrefWidth(100);
        return col;
    }
    
    private TableColumn<DormApplication, String> createStudentIdColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Student ID");
        col.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getStudent().getStudentId()));
        return col;
    }
    
    private TableColumn<DormApplication, String> createGenderColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Gender");
        col.setCellValueFactory(cell -> {
            Gender g = cell.getValue().getStudent().getGender();
            return new SimpleStringProperty(g != null ? g.name() : "-");
        });
        return col;
    }
    
    private TableColumn<DormApplication, String> createCollegeColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("College");
        col.setCellValueFactory(cell -> {
            College c = cell.getValue().getStudent().getCollege();
            return new SimpleStringProperty(c != null ? c.getAcronym() : "-");
        });
        return col;
    }
    
    private TableColumn<DormApplication, String> createResidencyColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Residency");
        col.setCellValueFactory(cell -> {
            Residency r = cell.getValue().getStudent().getResidency();
            return new SimpleStringProperty(r != null ? r.name() : "-");
        });
        return col;
    }
    
    private TableColumn<DormApplication, String> createSubcityColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Subcity");
        col.setCellValueFactory(cell -> new SimpleStringProperty(
            safe(cell.getValue().getStudent().getSubcity())));
        return col;
    }
    
    private TableColumn<DormApplication, String> createWoredaColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Woreda");
        col.setCellValueFactory(cell -> new SimpleStringProperty(
            safe(cell.getValue().getStudent().getWoreda())));
        return col;
    }
    
    private TableColumn<DormApplication, String> createSponsorshipColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Sponsorship");
        col.setCellValueFactory(cell -> {
            SponsorshipType type = cell.getValue().getStudent().getSponsorshipType();
            return new SimpleStringProperty(type != null ? type.name() : "-");
        });
        return col;
    }
    
    private TableColumn<DormApplication, String> createStatusColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Status");
        col.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getStatus().name()));
        return col;
    }
    
    private TableColumn<DormApplication, String> createTransactionColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Transaction ID");
        col.setCellValueFactory(cell -> new SimpleStringProperty(
            safe(cell.getValue().getStudent().getTransactionId())));
        return col;
    }
    
    private TableColumn<DormApplication, String> createSubmittedColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Submitted");
        col.setCellValueFactory(cell -> new SimpleStringProperty(
            safe(cell.getValue().getSubmittedDate())));
        return col;
    }
    
    private TableColumn<DormApplication, String> createBuildingColumn() {
        TableColumn<DormApplication, String> col = new TableColumn<>("Building");
        col.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getStudent().getAssignedBuilding()));
        return col;
    }
    
    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
