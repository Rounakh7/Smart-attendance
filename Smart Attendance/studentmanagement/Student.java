package studentmanagement;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private Map<LocalDate, Boolean> attendance;
    private double marks;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
        this.attendance = new HashMap<>();
        this.marks = 0.0;
    }

    // --- Getters / setters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getMarks() { return marks; }
    public void setMarks(double marks) { this.marks = marks; }

    // --- Attendance methods ---
    public void markAttendance(LocalDate date, boolean isPresent) {
        attendance.put(date, isPresent);
    }

    public boolean isPresentOnDate(LocalDate date) {
        return attendance.getOrDefault(date, false);
    }

    /** Returns a shallow copy of all attendance records */
    public Map<LocalDate, Boolean> getAttendance() {
        return new HashMap<>(attendance);
    }

    public int getTotalDaysPresent() {
        return (int) attendance.values().stream().filter(present -> present).count();
    }

    public int getTotalDays() {
        return attendance.size();
    }

    public double getAttendancePercentage() {
        if (getTotalDays() == 0) return 0.0;
        return (getTotalDaysPresent() * 100.0) / getTotalDays();
    }

    /**
     * Returns whether the student was present in their most recent marked session.
     * This is just for compatibility with old code that used isPresent().
     */
    public boolean isPresent() {
        if (attendance.isEmpty()) return false;
        LocalDate latestDate = attendance.keySet().stream().max(LocalDate::compareTo).orElse(null);
        return latestDate != null && attendance.get(latestDate);
    }
}
