
public class Student {
    private int studentId;
    private String name;
    private String email;
    private String password;
    private String program;
    private int yearLevel;

    public Student(int studentId, String name, String email, String password, String program, int yearLevel) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.program = program;
        this.yearLevel = yearLevel;
    }

    public int getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getProgram() { return program; }
    public int getYearLevel() { return yearLevel; }

    public void updateProfile(String name, String program, int yearLevel) {
        this.name = name;
        this.program = program;
        this.yearLevel = yearLevel;
    }

    @Override
    public String toString() { return name; }
}
