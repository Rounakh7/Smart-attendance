package studentmanagement;

import java.util.List;
import java.util.Scanner;

public class ResultManager {

    public void viewResults(Scanner sc, StudentManager studentManager) {
        List<Student> students = studentManager.getStudents();
        if (students.isEmpty()) {
            System.out.println("No students found! Add students first.");
            return;
        }

        while (true) {
            System.out.println("\n--- Student Results ---");
            System.out.println("1. View All Results");
            System.out.println("2. View Attendance Summary");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter choice: ");

            try {
                int choice = Integer.parseInt(sc.nextLine().trim());

                switch (choice) {
                    case 1:
                        displayAllResults(students);
                        break;
                    case 2:
                        displayAttendanceSummary(students);
                        break;
                    case 3:
                        return;
                    default:
                        System.out.println("Invalid choice, try again!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }

    private void displayAllResults(List<Student> students) {
        System.out.println("\n--- All Student Results ---");
        System.out.printf("%-5s %-20s %-10s %-10s%n",
                "ID", "Name", "Present", "Marks");
        System.out.println("-".repeat(50));

        for (Student student : students) {
            System.out.printf("%-5d %-20s %-10s %-10.2f%n",
                    student.getId(),
                    student.getName(),
                    student.getTotalDaysPresent() > 0 ? "Yes" : "No",
                    student.getMarks());
        }
    }

    private void displayAttendanceSummary(List<Student> students) {
        System.out.println("\n--- Attendance Summary ---");
        System.out.printf("%-5s %-25s %13s %12s %15s%n",
                "ID", "Name", "Present Days", "Total Days", "Percentage");
        System.out.println("-".repeat(75));

        for (Student student : students) {
            System.out.printf("%-5d %-25s %13d %12d %14.1f%%%n",
                student.getId(),
                student.getName(),
                student.getTotalDaysPresent(),
                student.getTotalDays(),
                student.getAttendancePercentage());
        }
    }
}
        