package dorm.dao;

//Custom exception for data access operations.

public class DataAccessException extends RuntimeException {
    
    private final String operation;
    private final String resource;
    

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
        this.operation = "unknown";
        this.resource = "unknown";
    }

    public DataAccessException(String operation, String resource, Throwable cause) {
        super(buildMessage(operation, resource, cause), cause);
        this.operation = operation;
        this.resource = resource;
    }
    
    private static String buildMessage(String operation, String resource, Throwable cause) {
        return String.format("Failed to %s %s: %s", operation, resource, 
                cause != null ? cause.getMessage() : "unknown error");
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getResource() {
        return resource;
    }

    public String getUserFriendlyMessage() {
        return String.format("Unable to %s data. Please try again or contact support.", operation);
    }
}
