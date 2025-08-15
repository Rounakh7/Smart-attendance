package studentmanagement;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StudentManager studentManager = new StudentManager();
        AttendanceManager attendanceManager = new AttendanceManager();
        ResultManager resultManager = new ResultManager();

        while (true) {
            System.out.println("\n===== Student Management System =====");
            System.out.println("1. Manage Students");
            System.out.println("2. Attendance");
            System.out.println("3. Results");
            System.out.println("4. Sync Attendance Names");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");

            try {
                int choice = Integer.parseInt(sc.nextLine().trim());

                switch (choice) {
                    case 1:
                        studentManager.manageStudents(sc);
                        break;
                    case 2:
                        attendanceMenu(sc, studentManager, attendanceManager);
                        break;
                    case 3:
                        resultManager.viewResults(sc, studentManager);
                        break;
                    case 4:
                        attendanceManager.syncNamesInAttendanceFiles(studentManager);
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid choice, try again!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }
    
    private static void attendanceMenu(Scanner sc, StudentManager studentManager, AttendanceManager attendanceManager) {
        while (true) {
            System.out.println("\n--- Attendance Management ---");
            System.out.println("1. Mark Attendance");
            System.out.println("2. View Attendance Report");
            System.out.println("3. Back to Main Menu");
            System.out.print("Enter choice: ");
            
            try {
                int choice = Integer.parseInt(sc.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        attendanceManager.markAttendance(sc, studentManager);
                        break;
                    case 2:
                        attendanceManager.viewAttendanceReport(sc, studentManager);
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
}
