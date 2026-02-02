package dorm;

import dorm.dao.DaoFactory;
import dorm.service.DatabaseDormService;
import dorm.ui.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application {
    
    private DatabaseDormService service;
    
    @Override
    public void init() {
        // Initialize the service with MySQL repositories
        service = new DatabaseDormService(
            DaoFactory.createUserRepository(),
            DaoFactory.createStudentRepository(),
            DaoFactory.createApplicationRepository(),
            DaoFactory.createAnnouncementRepository(),
            DaoFactory.createMessageRepository()
        );
    }
    
    @Override
    public void start(Stage stage) {
        try {
            // Load login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dorm/ui/login.fxml"));
            Parent root = loader.load();
            
            // Set up the controller with the service
            LoginController controller = loader.getController();
            controller.setService(service);
            
            // Create and show the scene
            Scene scene = new Scene(root, 1200, 700);
            stage.setTitle("Dormitory Management System");
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading application: " + e.getMessage());
            e.printStackTrace();
            
            // Show error dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText("Error: " + e.getMessage() + "\n\nPlease check:\n" +
                "1. MySQL server is running\n" +
                "2. Database credentials in db.properties are correct\n" +
                "3. The dormitory_db database exists");
            alert.showAndWait();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

