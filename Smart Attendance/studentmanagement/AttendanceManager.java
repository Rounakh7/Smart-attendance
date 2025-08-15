package studentmanagement;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class AttendanceManager {
    private static final String ATTENDANCE_DIR = "attendance_records";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    public void markAttendance(Scanner sc, StudentManager studentManager) {
        if (studentManager.getStudents().isEmpty()) {
            System.out.println("No students found! Add students first.");
            return;
        }

        System.out.println("\n--- Mark Attendance ---");
        
        // Get date for attendance
        LocalDate date = getDateFromUser(sc);
        if (date == null) return;
        
        // Mark attendance for each student
        for (Student student : studentManager.getStudents()) {
            System.out.printf("\nStudent: %s (ID: %d)", student.getName(), student.getId());
            System.out.print("\nPresent? (y/n, default=y): ");
            String input = sc.nextLine().trim().toLowerCase();
            boolean isPresent = input.isEmpty() || input.equals("y");
            
            student.markAttendance(date, isPresent);
            System.out.printf("Marked %s as %s for %s%n", 
                student.getName(), 
                isPresent ? "PRESENT" : "ABSENT", 
                date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        }
        
        // Save attendance to file
        saveAttendanceToFile(date, studentManager.getStudents());
        System.out.println("\nAttendance marked successfully for " + date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
    
    private LocalDate getDateFromUser(Scanner sc) {
        while (true) {
            try {
                System.out.print("\nEnter date (YYYY-MM-DD, or press Enter for today): ");
                String dateInput = sc.nextLine().trim();
                
                if (dateInput.isEmpty()) {
                    return LocalDate.now();
                }
                
                return LocalDate.parse(dateInput, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD format or press Enter for today.");
            }
        }
    }
    
    private void saveAttendanceToFile(LocalDate date, List<Student> students) {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get(ATTENDANCE_DIR));
            
            String filename = String.format("%s/attendance_%s.txt", 
                ATTENDANCE_DIR, 
                date.format(DateTimeFormatter.BASIC_ISO_DATE));
                
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("Date: " + date.format(DateTimeFormatter.ISO_LOCAL_DATE));
                writer.println(String.format("%-5s %-30s %-10s", "ID", "Name", "Status"));
                writer.println("-".repeat(50));
                
                for (Student student : students) {
                    String status = student.isPresentOnDate(date) ? "PRESENT" : "ABSENT";
                    writer.println(String.format("%-5d %-30s %-10s", 
                        student.getId(), 
                        student.getName(), 
                        status));
                }
                
                // Add summary
                long presentCount = students.stream()
                    .filter(s -> s.isPresentOnDate(date))
                    .count();
                double percentage = (students.size() > 0) ? 
                    (presentCount * 100.0) / students.size() : 0;
                    
                writer.println("\n--- Summary ---");
                writer.printf("Total Students: %d%n", students.size());
                writer.printf("Present: %d%n", presentCount);
                writer.printf("Absent: %d%n", students.size() - presentCount);
                writer.printf("Attendance: %.2f%%%n", percentage);
            }
            
        } catch (IOException e) {
            System.err.println("Error saving attendance to file: " + e.getMessage());
        }
    }
    
    public void viewAttendanceReport(Scanner sc, StudentManager studentManager) {
        System.out.println("\n--- Attendance Report ---");
        
        // Get list of all attendance files
        File dir = new File(ATTENDANCE_DIR);
        File[] files = dir.listFiles((d, name) -> name.startsWith("attendance_") && name.endsWith(".txt"));
        
        if (files == null || files.length == 0) {
            System.out.println("No attendance records found.");
            return;
        }
        
        // Sort files by date (newest first)
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        // List available dates
        System.out.println("\nAvailable dates:");
        for (int i = 0; i < files.length; i++) {
            String dateStr = files[i].getName()
                .replace("attendance_", "")
                .replace(".txt", "");
            try {
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.BASIC_ISO_DATE);
                System.out.printf("%d. %s%n", i+1, date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (DateTimeParseException e) {
                // Skip invalid filenames
            }
        }
        
        // Let user select a date
        System.out.print("\nSelect a date (number) or 0 to go back: ");
        try {
            int choice = Integer.parseInt(sc.nextLine().trim());
            if (choice < 1 || choice > files.length) {
                return;
            }
            
            // Display the selected attendance file
            displayAttendanceFile(files[choice-1]);
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid selection.");
        }
    }
    
    /**
     * Updates all attendance text files in ATTENDANCE_DIR so that student names
     * reflect the current names stored in students.dat. Past files are static snapshots
     * by design, so this utility lets you sync them after a rename.
     */
    public void syncNamesInAttendanceFiles(StudentManager studentManager) {
        Map<Integer, String> idToName = new HashMap<>();
        for (Student s : studentManager.getStudents()) {
            idToName.put(s.getId(), s.getName());
        }

        File dir = new File(ATTENDANCE_DIR);
        File[] files = dir.listFiles((d, name) -> name.startsWith("attendance_") && name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            System.out.println("No attendance records found to sync.");
            return;
        }

        int updatedFiles = 0;
        for (File file : files) {
            if (updateNamesInFile(file, idToName)) {
                updatedFiles++;
            }
        }
        System.out.printf("Synced names in %d/%d attendance files.%n", updatedFiles, files.length);
    }

    private boolean updateNamesInFile(File file, Map<Integer, String> idToName) {
        List<String> lines = new ArrayList<>();
        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                lines.add(fileScanner.nextLine());
            }
        } catch (IOException e) {
            System.err.println("Error reading file for sync: " + file.getPath());
            return false;
        }

        boolean inRowsSection = false;
        boolean changed = false;
        List<String> newLines = new ArrayList<>(lines.size());
        for (String line : lines) {
            if (!inRowsSection) {
                newLines.add(line);
                // Detect start of rows section: it comes right after the separator of dashes
                if (line.equals("-".repeat(50))) {
                    inRowsSection = true;
                }
                continue;
            }

            // Rows continue until a blank line or a section header like "--- Summary ---"
            if (line.trim().isEmpty() || line.startsWith("---")) {
                newLines.add(line);
                inRowsSection = false; // Stop processing rows after this marker
                continue;
            }

            // Parse the fixed-width columns used when writing:
            // "%-5d %-30s %-10s"
            String idPart = safeSubstring(line, 0, 5).trim();
            String namePart = safeSubstring(line, 6, 36).trim(); // starts after ID and a space, width 30
            String statusPart;
            if (line.length() >= 47) {
                statusPart = line.substring(36).trim();
            } else {
                // Fallback: take last token
                String[] tokens = line.trim().split("\\s+");
                statusPart = tokens.length > 0 ? tokens[tokens.length - 1] : "";
            }

            try {
                int id = Integer.parseInt(idPart);
                String currentName = idToName.getOrDefault(id, namePart);
                String rebuilt = String.format("%-5d %-30s %-10s", id, currentName, statusPart);
                if (!rebuilt.equals(line)) {
                    changed = true;
                }
                newLines.add(rebuilt);
            } catch (NumberFormatException e) {
                // Not a data row; keep as is
                newLines.add(line);
            }
        }

        if (!changed) {
            return false;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (String l : newLines) {
                writer.println(l);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing file during sync: " + file.getPath());
            return false;
        }
    }

    private static String safeSubstring(String s, int start, int end) {
        if (start >= s.length()) return "";
        return s.substring(start, Math.min(end, s.length()));
    }
    private void displayAttendanceFile(File file) {
        try {
            System.out.println("\n" + "-".repeat(50));
            System.out.println("ATTENDANCE REPORT");
            System.out.println("-".repeat(50));
            
            try (Scanner fileScanner = new Scanner(file)) {
                while (fileScanner.hasNextLine()) {
                    System.out.println(fileScanner.nextLine());
                }
            }
            
            System.out.println("-".repeat(50));
            System.out.println("File: " + file.getPath());
            
        } catch (IOException e) {
            System.out.println("Error reading attendance file: " + e.getMessage());
        }
    }
}

