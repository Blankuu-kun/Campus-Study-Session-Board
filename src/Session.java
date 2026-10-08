import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public abstract class Session {

    // The two kinds of sessions, and the two possible statuses.
    public enum Type { STUDY_SPOT, REVIEW_GROUP }
    public enum Status { OPEN, CLOSED }

    private int sessionId;
    private Student creator;
    private String subject;
    private String description;
    private int capacity;
    private Status status;
    private ArrayList<Student> members = new ArrayList<>();

    public Session(int sessionId, Student creator, String subject, String description, int capacity) {
        this.sessionId = sessionId;
        this.creator = creator;
        this.subject = subject;
        this.description = description;
        this.capacity = capacity;
        this.status = Status.OPEN;
    }

    public int getSessionId() { return sessionId; }
    public Student getCreator() { return creator; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public int getCapacity() { return capacity; }
    public Status getStatus() { return status; }
    public ArrayList<Student> getMembers() { return members; }

    public boolean isOpen() {
        return status == Status.OPEN;
    }

    public void close() {
        status = Status.CLOSED;
    }

    public boolean isCreator(Student s) {
        return creator.getStudentId() == s.getStudentId();
    }

    public boolean isMember(Student s) {
        for (Student m : members) {
            if (m.getStudentId() == s.getStudentId()) {
                return true;
            }
        }
        return false;
    }

    public int getSeatsLeft() {
        return capacity - members.size();
    }

    // Members can only be added through this method, and never past the capacity.
    public void addMember(Student s) {
        if (members.size() < capacity) {
            members.add(s);
        }
    }

    // Each child class answers these differently 
    public abstract boolean hasAvailableSeats();
    public abstract String getDetails();
    public abstract Type getType();


    // child class 1
    public static class StudySpotSession extends Session {
        private String location;
        private LocalDateTime timeCreated;

        public StudySpotSession(int sessionId, Student creator, String subject, String description,
                                int capacity, String location, LocalDateTime timeCreated) {
            super(sessionId, creator, subject, description, capacity);
            this.location = location;
            this.timeCreated = timeCreated;
        }

        public String getLocation() { return location; }
        public LocalDateTime getTimeCreated() { return timeCreated; }

      
        public int getAvailableSeats() {
            return getSeatsLeft();
        }

        @Override
        public boolean hasAvailableSeats() {
            return isOpen() && getAvailableSeats() > 0;
        }

        @Override
        public String getDetails() {
            int seats = getAvailableSeats();
            String word = (seats == 1) ? " seat" : " seats";
            return location + "  ·  " + seats + word + " open now";
        }

        @Override
        public Type getType() {
            return Type.STUDY_SPOT;
        }
    }


    // child class 2
    public static class ReviewGroupSession extends Session {
        private LocalDate scheduledDate;
        private LocalTime scheduledTime;

        public ReviewGroupSession(int sessionId, Student creator, String subject, String description,
                                  int maxMembers, LocalDate scheduledDate, LocalTime scheduledTime) {
            super(sessionId, creator, subject, description, maxMembers);
            this.scheduledDate = scheduledDate;
            this.scheduledTime = scheduledTime;
        }

        public LocalDate getScheduledDate() { return scheduledDate; }
        public LocalTime getScheduledTime() { return scheduledTime; }
        public int getMaxMembers() { return getCapacity(); }

        
        public boolean isUpcoming() {
            LocalDateTime start = LocalDateTime.of(scheduledDate, scheduledTime);
            return start.isAfter(LocalDateTime.now());
        }

        @Override
        public boolean hasAvailableSeats() {
            return isOpen() && isUpcoming() && getSeatsLeft() > 0;
        }

        @Override
        public String getDetails() {
            String date = scheduledDate.format(DateTimeFormatter.ofPattern("EEE, MMM d"));
            String time = scheduledTime.format(DateTimeFormatter.ofPattern("h:mm a"));
            return date + " at " + time + "  ·  " + getMembers().size() + "/" + getMaxMembers() + " members";
        }

        @Override
        public Type getType() {
            return Type.REVIEW_GROUP;
        }
    }
}
