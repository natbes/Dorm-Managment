package dorm.ui.components;

import dorm.model.DormApplication;
import dorm.model.Student;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public final class ExportUtil {
    
    private ExportUtil() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Export applications to CSV file
     * @param applications List of applications to export
     * @param parentWindow Parent window for file chooser dialog
     * @return true if export was successful
     */
    public static boolean exportApplicationsToCsv(List<DormApplication> applications, Window parentWindow) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Applications");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("applications_export.csv");
        
        File file = chooser.showSaveDialog(parentWindow);
        if (file == null) {
            return false; // User cancelled
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("Name,Student ID,Gender,Sponsorship,Residency,City,Subcity,Woreda,");
            writer.write("Status,Submitted,Last Response,Building,Transaction ID\n");
            
            // Write data
            for (DormApplication app : applications) {
                Student s = app.getStudent();
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    escapeCsvValue(s.getDisplayName()),
                    escapeCsvValue(s.getStudentId()),
                    s.getGender() != null ? s.getGender().name() : "-",
                    s.getSponsorshipType() != null ? s.getSponsorshipType().name() : "-",
                    s.getResidency() != null ? s.getResidency().name() : "-",
                    escapeCsvValue(safe(s.getCity())),
                    escapeCsvValue(safe(s.getSubcity())),
                    escapeCsvValue(safe(s.getWoreda())),
                    app.getStatus().name(),
                    escapeCsvValue(safe(app.getSubmittedDate())),
                    escapeCsvValue(safe(app.getLatestResponse())),
                    escapeCsvValue(safe(s.getAssignedBuilding())),
                    escapeCsvValue(safe(s.getTransactionId()))
                ));
            }
            
            showInfo("Exported " + applications.size() + " applications to " + file.getName());
            return true;
            
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Escape a value for CSV output
     */
    private static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if needed
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private static String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
    
    private static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Export Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
