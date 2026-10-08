import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Database {


    private static final String URL = "jdbc:mysql://localhost:3306/campus_board";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private Connection conn;

    public ArrayList<Student> students = new ArrayList<>();
    public ArrayList<Session> sessions = new ArrayList<>();
    public ArrayList<JoinRequest> requests = new ArrayList<>();

    // connect

    public void connect() throws SQLException {
        conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("[Database] Connected to MySQL: " + URL);
    }


    public void load() throws SQLException {
        students.clear();
        sessions.clear();
        requests.clear();
        Statement st = conn.createStatement();

        // 1. students
        ResultSet rs = st.executeQuery("SELECT * FROM student ORDER BY student_id");
        while (rs.next()) {
            students.add(new Student(rs.getInt("student_id"), rs.getString("name"), rs.getString("email"),
                    rs.getString("password"), rs.getString("program"), rs.getInt("year_level")));
        }

        // 2. sessions 
        rs = st.executeQuery(
                "SELECT s.*, sp.location, rg.scheduled_date, rg.scheduled_time " +
                "FROM session s " +
                "LEFT JOIN study_spot_session sp ON s.session_id = sp.session_id " +
                "LEFT JOIN review_group_session rg ON s.session_id = rg.session_id " +
                "ORDER BY s.session_id");
        while (rs.next()) {
            int id = rs.getInt("session_id");
            Student creator = findStudent(rs.getInt("creator_id"));
            String subject = rs.getString("subject");
            String description = rs.getString("description");
            int capacity = rs.getInt("capacity");

            Session s;
            if (rs.getString("session_type").equals("STUDY_SPOT")) {
                s = new Session.StudySpotSession(id, creator, subject, description, capacity,
                        rs.getString("location"), rs.getTimestamp("created_at").toLocalDateTime());
            } else {
                s = new Session.ReviewGroupSession(id, creator, subject, description, capacity,
                        rs.getDate("scheduled_date").toLocalDate(), rs.getTime("scheduled_time").toLocalTime());
            }
            if (rs.getString("status").equals("CLOSED")) {
                s.close();
            }
            sessions.add(s);
        }

        // 3. members of each session
        rs = st.executeQuery("SELECT * FROM session_member ORDER BY joined_at");
        while (rs.next()) {
            Session s = findSession(rs.getInt("session_id"));
            Student member = findStudent(rs.getInt("student_id"));
            if (s != null && member != null) {
                s.addMember(member);
            }
        }

        // 4. join requests
        rs = st.executeQuery("SELECT * FROM join_request ORDER BY request_id");
        while (rs.next()) {
            requests.add(new JoinRequest(rs.getInt("request_id"),
                    findStudent(rs.getInt("requester_id")),
                    findSession(rs.getInt("session_id")),
                    JoinRequest.Status.valueOf(rs.getString("status")),
                    rs.getTimestamp("requested_at").toLocalDateTime()));
        }

        st.close();
        System.out.println("[Database] Loaded " + students.size() + " students, " + sessions.size()
                + " sessions, " + requests.size() + " requests.");
    }

    // update

    public void addStudent(Student s) {
        students.add(s);
        run("INSERT INTO student VALUES (?, ?, ?, ?, ?, ?)",
                s.getStudentId(), s.getName(), s.getEmail(), s.getPassword(), s.getProgram(), s.getYearLevel());
    }

    public void updateStudent(Student s) {
        run("UPDATE student SET name = ?, program = ?, year_level = ? WHERE student_id = ?",
                s.getName(), s.getProgram(), s.getYearLevel(), s.getStudentId());
    }


    public void addSession(Session s) {
        sessions.add(s);
        LocalDateTime createdAt = LocalDateTime.now().withNano(0);
        if (s instanceof Session.StudySpotSession) {
            createdAt = ((Session.StudySpotSession) s).getTimeCreated();
        }
        run("INSERT INTO session VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                s.getSessionId(), s.getCreator().getStudentId(), s.getType().toString(), s.getSubject(),
                s.getDescription(), s.getCapacity(), s.getStatus().toString(), createdAt);

        if (s instanceof Session.StudySpotSession) {
            Session.StudySpotSession spot = (Session.StudySpotSession) s;
            run("INSERT INTO study_spot_session VALUES (?, ?, ?)",
                    spot.getSessionId(), spot.getLocation(), spot.getAvailableSeats());
        } else {
            Session.ReviewGroupSession review = (Session.ReviewGroupSession) s;
            run("INSERT INTO review_group_session VALUES (?, ?, ?, ?)",
                    review.getSessionId(), review.getScheduledDate(), review.getScheduledTime(), review.getMaxMembers());
        }
    }

    public void updateSessionStatus(Session s) {
        run("UPDATE session SET status = ? WHERE session_id = ?", s.getStatus().toString(), s.getSessionId());
    }

    // adds a member, and for study spots also updates the available seats column.
    public void addMember(Session s, Student member) {
        s.addMember(member);
        run("INSERT INTO session_member VALUES (?, ?, ?)",
                s.getSessionId(), member.getStudentId(), LocalDateTime.now().withNano(0));

        if (s instanceof Session.StudySpotSession) {
            run("UPDATE study_spot_session SET available_seats = ? WHERE session_id = ?",
                    s.getSeatsLeft(), s.getSessionId());
        }
    }

    public void addRequest(JoinRequest r) {
        requests.add(r);
        run("INSERT INTO join_request (request_id, session_id, requester_id, status, requested_at) VALUES (?, ?, ?, ?, ?)",
                r.getRequestId(), r.getSession().getSessionId(), r.getRequester().getStudentId(),
                r.getStatus().toString(), r.getRequestedAt());
    }

    public void updateRequestStatus(JoinRequest r) {
        run("UPDATE join_request SET status = ?, responded_at = ? WHERE request_id = ?",
                r.getStatus().toString(), LocalDateTime.now().withNano(0), r.getRequestId());
    }

 
    private void run(String sql, Object... values) {
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0; i < values.length; i++) {
                ps.setObject(i + 1, values[i]);
            }
            ps.executeUpdate();
            ps.close();
            System.out.println("[Database] " + sql + "   values: " + java.util.Arrays.toString(values));
        } catch (SQLException e) {
            System.out.println("[Database] ERROR: " + e.getMessage());
        }
    }

 

    public Student findStudent(int id) {
        for (Student s : students) {
            if (s.getStudentId() == id) return s;
        }
        return null;
    }

    public Session findSession(int id) {
        for (Session s : sessions) {
            if (s.getSessionId() == id) return s;
        }
        return null;
    }

    public JoinRequest findRequest(int id) {
        for (JoinRequest r : requests) {
            if (r.getRequestId() == id) return r;
        }
        return null;
    }

   
    public int nextStudentId() {
        int max = 0;
        for (Student s : students) max = Math.max(max, s.getStudentId());
        return max + 1;
    }

    public int nextSessionId() {
        int max = 0;
        for (Session s : sessions) max = Math.max(max, s.getSessionId());
        return max + 1;
    }

    public int nextRequestId() {
        int max = 0;
        for (JoinRequest r : requests) max = Math.max(max, r.getRequestId());
        return max + 1;
    }

 
    public static String clean(String text) {
        if (text == null) return "";
        return text.replace("\n", " ").trim();
    }
}
