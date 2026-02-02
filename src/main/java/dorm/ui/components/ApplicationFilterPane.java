package dorm.ui.components;

import dorm.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class ApplicationFilterPane extends VBox {
    
    private final ComboBox<String> filterGender;
    private final ComboBox<String> filterResidency;
    private final ComboBox<String> filterSubcity;
    private final ComboBox<String> filterWoreda;
    private final ComboBox<String> filterCollege;
    private final ComboBox<String> filterSponsorship;
    private final ComboBox<String> filterStatus;
    
    private Consumer<Void> onFilterApplied;
    
    public ApplicationFilterPane() {
        // Initialize filter controls
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
        filterSubcity.setPromptText("Type or select");
        
        filterWoreda = new ComboBox<>();
        filterWoreda.getItems().add("All Woredas");
        filterWoreda.setValue("All Woredas");
        filterWoreda.setEditable(true);
        filterWoreda.setPromptText("Type or select");
        
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
        
        applyFilterBtn.setOnAction(e -> notifyFilterApplied());
        clearFilterBtn.setOnAction(e -> clearFilters());
        
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
        
        // Build layout
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
        
        this.setSpacing(5);
        this.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 5;");
        this.getChildren().addAll(filterRow1, filterRow2);
    }
    
    public void setOnFilterApplied(Consumer<Void> callback) {
        this.onFilterApplied = callback;
    }
    
    private void notifyFilterApplied() {
        if (onFilterApplied != null) {
            onFilterApplied.accept(null);
        }
    }
    
    private void clearFilters() {
        filterGender.setValue("All Genders");
        filterResidency.setValue("All Residency");
        filterSubcity.setValue("All Subcities");
        filterWoreda.setValue("All Woredas");
        filterCollege.setValue("All Colleges");
        filterSponsorship.setValue("All Sponsorship");
        filterStatus.setValue("All Status");
        notifyFilterApplied();
    }
    
    public List<DormApplication> applyFilters(List<DormApplication> applications) {
        return applications.stream()
            .filter(this::matchesFilters)
            .sorted(this::compareApplications)
            .collect(Collectors.toList());
    }
    
    private boolean matchesFilters(DormApplication app) {
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
    }
    
    private int compareApplications(DormApplication a1, DormApplication a2) {
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
    }
}
