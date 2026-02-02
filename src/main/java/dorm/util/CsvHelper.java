package dorm.util;

import dorm.dao.DataAccessException;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


//for CSV file operations
public final class CsvHelper {
    
    private static final String DATA_DIR = "data";
    private static final Logger LOGGER = Logger.getLogger(CsvHelper.class.getName());
    
    private CsvHelper() {}

    public static Path getDataDirectory() throws DataAccessException {
        Path dataPath = Paths.get(DATA_DIR);
        if (!Files.exists(dataPath)) {
            try {
                Files.createDirectories(dataPath);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to create data directory", e);
                throw new DataAccessException("create", "data directory", e);
            }
        }
        return dataPath;
    }

    public static Path getDataDirectorySafe() {
        Path dataPath = Paths.get(DATA_DIR);
        if (!Files.exists(dataPath)) {
            try {
                Files.createDirectories(dataPath);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to create data directory", e);
                return null;
            }
        }
        return dataPath;
    }

    public static Path getCsvPath(String filename) throws DataAccessException {
        return getDataDirectory().resolve(filename);
    }

    public static Path getCsvPathSafe(String filename) {
        Path dataDir = getDataDirectorySafe();
        return dataDir != null ? dataDir.resolve(filename) : null;
    }
    

    public static List<String[]> readAll(String filename, boolean hasHeader) {
        List<String[]> records = new ArrayList<>();
        Path path = getCsvPathSafe(filename);
        
        if (path == null || !Files.exists(path)) {
            return records;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirst = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirst && hasHeader) {
                    isFirst = false;
                    continue;
                }
                isFirst = false;
                
                if (!line.trim().isEmpty()) {
                    records.add(parseCsvLine(line));
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading CSV file: " + filename, e);
            // Return empty list instead of crashing - UI can show "no data" message
        }
        
        return records;
    }
    

    public static List<String[]> readAllChecked(String filename, boolean hasHeader) throws DataAccessException {
        List<String[]> records = new ArrayList<>();
        Path path;
        
        try {
            path = getCsvPath(filename);
        } catch (DataAccessException e) {
            throw e;
        }
        
        if (!Files.exists(path)) {
            return records;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirst = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirst && hasHeader) {
                    isFirst = false;
                    continue;
                }
                isFirst = false;
                
                if (!line.trim().isEmpty()) {
                    records.add(parseCsvLine(line));
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading CSV file: " + filename, e);
            throw new DataAccessException("read", filename, e);
        }
        
        return records;
    }

    public static boolean writeAll(String filename, String header, List<String[]> records) {
        Path path = getCsvPathSafe(filename);
        
        if (path == null) {
            LOGGER.warning("Cannot write to " + filename + ": data directory unavailable");
            return false;
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            if (header != null) {
                writer.write(header);
                writer.newLine();
            }
            
            for (String[] record : records) {
                writer.write(toCsvLine(record));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing CSV file: " + filename, e);
            return false;
        }
    }
    

    public static void writeAllChecked(String filename, String header, List<String[]> records) throws DataAccessException {
        Path path;
        try {
            path = getCsvPath(filename);
        } catch (DataAccessException e) {
            throw e;
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            if (header != null) {
                writer.write(header);
                writer.newLine();
            }
            
            for (String[] record : records) {
                writer.write(toCsvLine(record));
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing CSV file: " + filename, e);
            throw new DataAccessException("write", filename, e);
        }
    }

    public static boolean append(String filename, String header, String[] record) {
        Path path = getCsvPathSafe(filename);
        
        if (path == null) {
            LOGGER.warning("Cannot append to " + filename + ": data directory unavailable");
            return false;
        }
        
        try {
            boolean fileExists = Files.exists(path);
            
            try (BufferedWriter writer = Files.newBufferedWriter(path, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                
                if (!fileExists && header != null) {
                    writer.write(header);
                    writer.newLine();
                }
                
                writer.write(toCsvLine(record));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error appending to CSV file: " + filename, e);
            return false;
        }
    }
    


    public static void appendChecked(String filename, String header, String[] record) throws DataAccessException {
        Path path;
        try {
            path = getCsvPath(filename);
        } catch (DataAccessException e) {
            throw e;
        }
        
        try {
            boolean fileExists = Files.exists(path);
            
            try (BufferedWriter writer = Files.newBufferedWriter(path, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                
                if (!fileExists && header != null) {
                    writer.write(header);
                    writer.newLine();
                }
                
                writer.write(toCsvLine(record));
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error appending to CSV file: " + filename, e);
            throw new DataAccessException("append", filename, e);
        }
    }
    

    public static String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (inQuotes) {
                if (c == '"') {
                    // Check for escaped quote
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    values.add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        values.add(current.toString());
        
        return values.toArray(new String[0]);
    }
    
    /**
     * Convert an array of values to a CSV line
     * Properly escapes quotes and wraps values containing commas or quotes
     */
    public static String toCsvLine(String[] values) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escapeCsvValue(values[i]));
        }
        
        return sb.toString();
    }
    
    /**
     * Escape a single CSV value
     */
    public static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // If value contains comma, quote, or newline, wrap in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Null-safe string value
     */
    public static String nullSafe(String value) {
        return value == null ? "" : value;
    }
    
    /**
     * Convert empty string to null
     */
    public static String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}
