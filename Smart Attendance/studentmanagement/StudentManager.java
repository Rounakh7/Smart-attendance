package studentmanagement;

import java.io.*;
import java.util.*;

public class StudentManager {
    private static final String STUDENTS_FILE = "students.dat";
    private List<Student> students;
    private int nextId = 1;
    
    public StudentManager() {
        this.students = new ArrayList<>();
        loadStudents();
        updateNextId();
    }

    public void manageStudents(Scanner sc) {
        while (true) {
            System.out.println("\n--- Manage Students ---");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Update Student");
            System.out.println("4. Delete Student");
			System.out.println("5. Go Back");
			System.out.println("6. Set/Update Marks");
            System.out.print("Enter choice: ");

            try {
                int choice = Integer.parseInt(sc.nextLine().trim());

					switch (choice) {
                    case 1:
                        addStudent(sc);
                        break;
                    case 2:
                        viewAllStudents();
                        break;
                    case 3:
                        updateStudent(sc);
                        break;
                    case 4:
                        deleteStudent(sc);
                        break;
                    case 5:
                        return;
						case 6:
							setStudentMarks(sc);
							break;
                    default:
                        System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }
    
    private void addStudent(Scanner sc) {
        System.out.print("Enter student name: ");
        String name = sc.nextLine().trim();
        
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty!");
            return;
        }
        
        Student student = new Student(nextId++, name);
        students.add(student);
        saveStudents();
        System.out.printf("Student '%s' (ID: %d) added successfully!%n", name, student.getId());
    }
    
    private void viewAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students found!");
            return;
        }
        
        System.out.println("\n--- All Students ---");
        System.out.printf("%-5s %-25s %15s%n", "ID", "Name", "Attendance %");
        System.out.println("-".repeat(50));
        
        for (Student student : students) {
            System.out.printf("%-5d %-25s %10.1f%%%n",
                student.getId(),
                student.getName(),
                student.getAttendancePercentage());
        }
    }
    
    private void updateStudent(Scanner sc) {
        viewAllStudents();
        if (students.isEmpty()) return;
        
        System.out.print("\nEnter student ID to update: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            Student student = findStudentById(id);
            
            if (student == null) {
                System.out.println("Student not found with ID: " + id);
                return;
            }
            
            System.out.print("Enter new name (or press Enter to keep current): ");
            String newName = sc.nextLine().trim();
            
            if (!newName.isEmpty()) {
                String oldName = student.getName();
                student.setName(newName);
                saveStudents();
                System.out.printf("Student name updated from '%s' to '%s'%n", oldName, newName);
            } else {
                System.out.println("No changes made.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid student ID!");
        }
    }
    
    private void deleteStudent(Scanner sc) {
        viewAllStudents();
        if (students.isEmpty()) return;
        
        System.out.print("\nEnter student ID to delete: ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            Student student = findStudentById(id);
            
            if (student == null) {
                System.out.println("Student not found with ID: " + id);
                return;
            }
            
            System.out.print("Are you sure you want to delete " + student.getName() + "? (y/n): ");
            String confirm = sc.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                students.remove(student);
                saveStudents();
                System.out.println("Student deleted successfully!");
            } else {
                System.out.println("Deletion cancelled.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid student ID!");
        }
    }

	private void setStudentMarks(Scanner sc) {
		viewAllStudents();
		if (students.isEmpty()) return;

		System.out.print("\nEnter student ID to set marks: ");
		try {
			int id = Integer.parseInt(sc.nextLine().trim());
			Student student = findStudentById(id);

			if (student == null) {
				System.out.println("Student not found with ID: " + id);
				return;
			}

			System.out.print("Enter marks (0-100): ");
			String marksInput = sc.nextLine().trim();
			double marks = Double.parseDouble(marksInput);
			if (marks < 0 || marks > 100) {
				System.out.println("Marks must be between 0 and 100.");
				return;
			}

			student.setMarks(marks);
			saveStudents();
			System.out.printf("Marks for '%s' (ID: %d) set to %.2f%n", student.getName(), student.getId(), student.getMarks());
		} catch (NumberFormatException e) {
			System.out.println("Please enter valid numeric values!");
		}
	}
    
    private Student findStudentById(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }
    
    private void updateNextId() {
        nextId = students.stream()
            .mapToInt(Student::getId)
            .max()
            .orElse(0) + 1;
    }
    
    @SuppressWarnings("unchecked")
    private void loadStudents() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(STUDENTS_FILE))) {
            students = (List<Student>) ois.readObject();
            System.out.println("Student data loaded successfully!");
        } catch (FileNotFoundException e) {
            // First run, file doesn't exist yet
            students = new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading student data: " + e.getMessage());
            students = new ArrayList<>();
        }
    }
    
    private void saveStudents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(STUDENTS_FILE))) {
            oos.writeObject(students);
        } catch (IOException e) {
            System.err.println("Error saving student data: " + e.getMessage());
        }
    }

    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }
}
