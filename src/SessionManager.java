import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;


public class SessionManager {


    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }

    private Database db;

    public SessionManager(Database db) {
        this.db = db;
    }

    // accounts

    public Student register(String name, String email, String password, String program, int yearLevel) throws ValidationException {
        name = Database.clean(name);
        email = Database.clean(email).toLowerCase();
        program = Database.clean(program);

        if (name.isEmpty()) {
            throw new ValidationException("Name is required.");
        }
        if (name.length() > 100 || email.length() > 120 || program.length() > 80) {
            throw new ValidationException("Name, e-mail or program is too long.");
        }
        if (!email.matches("^[a-z0-9._]+@[a-z0-9.]+\\.[a-z]+$")) {
            throw new ValidationException("Enter a valid e-mail address.");
        }
        for (Student s : db.students) {
            if (s.getEmail().equals(email)) {
                throw new ValidationException("That e-mail is already registered.");
            }
        }
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters.");
        }
        if (password.length() > 50) {
            throw new ValidationException("Password must be 50 characters or fewer.");
        }
        if (yearLevel < 1 || yearLevel > 6) {
            throw new ValidationException("Year level must be between 1 and 6.");
        }

        Student s = new Student(db.nextStudentId(), name, email, Database.clean(password), program, yearLevel);
        db.addStudent(s);
        return s;
    }

    public Student login(String email, String password) throws ValidationException {
        email = Database.clean(email).toLowerCase();
        for (Student s : db.students) {
            if (s.getEmail().equals(email) && s.getPassword().equals(password)) {
                return s;
            }
        }
        throw new ValidationException("Incorrect e-mail or password.");
    }

    public void updateProfile(Student s, String name, String program, int yearLevel) throws ValidationException {
        name = Database.clean(name);
        if (name.isEmpty()) {
            throw new ValidationException("Name is required.");
        }
        if (yearLevel < 1 || yearLevel > 6) {
            throw new ValidationException("Year level must be between 1 and 6.");
        }
        if (name.length() > 100 || Database.clean(program).length() > 80) {
            throw new ValidationException("Name or program is too long.");
        }
        s.updateProfile(name, Database.clean(program), yearLevel);
        db.updateStudent(s);
    }

    // posting sessions

    public Session.StudySpotSession postStudySpot(Student creator, String subject, String description, int capacity, String location)
            throws ValidationException {
        checkCommonFields(subject, description, capacity);
        location = Database.clean(location);
        if (location.isEmpty()) {
            throw new ValidationException("Location is required for a Study Spot session.");
        }
        if (location.length() > 120) {
            throw new ValidationException("Location must be 120 characters or fewer.");
        }

        Session.StudySpotSession s = new Session.StudySpotSession(db.nextSessionId(), creator, Database.clean(subject).toUpperCase(),
                Database.clean(description), capacity, location, LocalDateTime.now().withNano(0));
        db.addSession(s);
        return s;
    }

    public Session.ReviewGroupSession postReviewGroup(Student creator, String subject, String description, int maxMembers,
                                              LocalDate date, LocalTime time) throws ValidationException {
        checkCommonFields(subject, description, maxMembers);
        if (date == null || time == null) {
            throw new ValidationException("Scheduled date and time are required.");
        }
        if (!LocalDateTime.of(date, time).isAfter(LocalDateTime.now())) {
            throw new ValidationException("Scheduled date and time must be in the future.");
        }

        Session.ReviewGroupSession s = new Session.ReviewGroupSession(db.nextSessionId(), creator, Database.clean(subject).toUpperCase(),
                Database.clean(description), maxMembers, date, time);
        db.addSession(s);
        return s;
    }

    private void checkCommonFields(String subject, String description, int capacity) throws ValidationException {
        if (Database.clean(subject).isEmpty()) {
            throw new ValidationException("Subject is required (e.g. CSS123P).");
        }
        if (Database.clean(subject).length() > 60) {
            throw new ValidationException("Subject must be 60 characters or fewer.");
        }
        if (Database.clean(description).isEmpty()) {
            throw new ValidationException("Description is required.");
        }
        if (Database.clean(description).length() > 300) {
            throw new ValidationException("Description must be 300 characters or fewer.");
        }
        if (capacity < 1 || capacity > 20) {
            throw new ValidationException("Capacity must be between 1 and 20.");
        }
    }


    public ArrayList<Session> getOpenSessions(String search, Session.Type type) {
        String text = (search == null) ? "" : search.trim().toLowerCase();
        ArrayList<Session> result = new ArrayList<>();

        for (int i = db.sessions.size() - 1; i >= 0; i--) {
            Session s = db.sessions.get(i);
            if (!s.isOpen()) continue;
            if (type != null && s.getType() != type) continue;

            boolean matches = s.getSubject().toLowerCase().contains(text)
                    || s.getDescription().toLowerCase().contains(text)
                    || s.getDetails().toLowerCase().contains(text);
            if (text.isEmpty() || matches) {
                result.add(s);
            }
        }
        return result;
    }

    public Session getSession(int id) {
        return db.findSession(id);
    }

    public ArrayList<Session> getSessionsCreatedBy(Student student) {
        ArrayList<Session> result = new ArrayList<>();
        for (Session s : db.sessions) {
            if (s.isCreator(student)) result.add(s);
        }
        return result;
    }

    // join requests

    public JoinRequest requestToJoin(Student student, int sessionId) throws ValidationException {
        Session s = db.findSession(sessionId);
        if (s == null) {
            throw new ValidationException("Session not found.");
        }
        if (!s.isOpen()) {
            throw new ValidationException("This session is closed.");
        }
        if (s.isCreator(student)) {
            throw new ValidationException("You created this session, so you are already in it.");
        }
        if (s.isMember(student)) {
            throw new ValidationException("You are already a member of this session.");
        }
        if (!s.hasAvailableSeats()) {
            throw new ValidationException("This session is full.");
        }
        JoinRequest old = getMyRequest(student, s);
        if (old != null && old.getStatus() != JoinRequest.Status.DECLINED) {
            throw new ValidationException("You already requested to join this session.");
        }

        JoinRequest r = new JoinRequest(db.nextRequestId(), student, s, JoinRequest.Status.PENDING, LocalDateTime.now().withNano(0));
        db.addRequest(r);
        return r;
    }

   
    public JoinRequest getMyRequest(Student student, Session s) {
        JoinRequest found = null;
        for (JoinRequest r : db.requests) {
            if (r.getSession().getSessionId() == s.getSessionId()
                    && r.getRequester().getStudentId() == student.getStudentId()) {
                found = r;
            }
        }
        return found;
    }

    public ArrayList<JoinRequest> getPendingRequests(int sessionId) {
        ArrayList<JoinRequest> result = new ArrayList<>();
        for (JoinRequest r : db.requests) {
            if (r.getSession().getSessionId() == sessionId && r.isPending()) {
                result.add(r);
            }
        }
        return result;
    }

    public ArrayList<JoinRequest> getRequestsBy(Student student) {
        ArrayList<JoinRequest> result = new ArrayList<>();
        for (JoinRequest r : db.requests) {
            if (r.getRequester().getStudentId() == student.getStudentId()) {
                result.add(r);
            }
        }
        return result;
    }

    public void acceptRequest(int requestId, Student creator) throws ValidationException {
        JoinRequest r = db.findRequest(requestId);
        if (r == null) {
            throw new ValidationException("Request not found.");
        }
        Session s = r.getSession();
        if (!s.isCreator(creator)) {
            throw new ValidationException("Only the session creator can accept requests.");
        }
        if (!r.isPending()) {
            throw new ValidationException("This request was already handled.");
        }

        
        if (!s.hasAvailableSeats()) {
            r.decline();
            db.updateRequestStatus(r);
            throw new ValidationException("Session is full. The request was declined automatically.");
        }

        db.addMember(s, r.getRequester());
        r.accept();
        db.updateRequestStatus(r);
    }

    public void declineRequest(int requestId, Student creator) throws ValidationException {
        JoinRequest r = db.findRequest(requestId);
        if (r == null) {
            throw new ValidationException("Request not found.");
        }
        if (!r.getSession().isCreator(creator)) {
            throw new ValidationException("Only the session creator can decline requests.");
        }
        if (!r.isPending()) {
            throw new ValidationException("This request was already handled.");
        }
        r.decline();
        db.updateRequestStatus(r);
    }

    public void closeSession(int sessionId, Student creator) throws ValidationException {
        Session s = db.findSession(sessionId);
        if (s == null) {
            throw new ValidationException("Session not found.");
        }
        if (!s.isCreator(creator)) {
            throw new ValidationException("Only the session creator can close this session.");
        }
        if (!s.isOpen()) {
            throw new ValidationException("This session is already closed.");
        }

        s.close();
        db.updateSessionStatus(s);
        // Anyone still waiting is declined, because the session is closed now.
        for (JoinRequest r : db.requests) {
            if (r.getSession().getSessionId() == sessionId && r.isPending()) {
                r.decline();
                db.updateRequestStatus(r);
            }
        }
    }

 
    public void addSampleData() {
        try {
            Student althea = register("Althea Pide", "althea@mymail.mapua.edu.ph", "password123", "BS Computer Science", 2);
            Student dane = register("Dane Rolle", "dane@mymail.mapua.edu.ph", "password123", "BS Information Technology", 2);
            Student sudip = register("Sudip Shah", "sudip@mymail.mapua.edu.ph", "password123", "BS Computer Science", 3);
            Student abdur = register("Abdur-Rahman Manala-o", "abdur@mymail.mapua.edu.ph", "password123", "BS Computer Engineering", 2);

            Session s1 = postStudySpot(althea, "CSS123P", "Working through the OOP exercises, inheritance and polymorphism. Bring your laptop.", 4, "Library, 3rd floor, Table 12");
            postStudySpot(sudip, "MATH181", "Quiet review of measures of central tendency. Formula cards welcome.", 3, "Student Lounge, near the windows");
            Session s3 = postReviewGroup(dane, "MATH181", "Midterm review: sampling, data presentation, dispersion. We will solve past exam items.", 5,
                    LocalDate.now().plusDays(3), LocalTime.of(14, 0));
            Session s4 = postReviewGroup(abdur, "CSS123P", "Java project sprint: build the session board UI together.", 6,
                    LocalDate.now().plusDays(5), LocalTime.of(10, 0));
            postStudySpot(dane, "PHY101", "Problem set 4, kinematics. 2 seats free until 5 PM.", 2, "Engineering Bldg, Room 204");

            JoinRequest r1 = requestToJoin(sudip, s1.getSessionId());
            acceptRequest(r1.getRequestId(), althea);
            requestToJoin(abdur, s1.getSessionId());
            requestToJoin(althea, s3.getSessionId());
            requestToJoin(sudip, s4.getSessionId());
        } catch (ValidationException e) {
            System.out.println("Sample data problem: " + e.getMessage());
        }
    }
}
