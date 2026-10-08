import java.time.LocalDateTime;


public class JoinRequest {

    public enum Status { PENDING, ACCEPTED, DECLINED }

    private int requestId;
    private Student requester;
    private Session session;
    private Status status;
    private LocalDateTime requestedAt;

    public JoinRequest(int requestId, Student requester, Session session, Status status, LocalDateTime requestedAt) {
        this.requestId = requestId;
        this.requester = requester;
        this.session = session;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public int getRequestId() { return requestId; }
    public Student getRequester() { return requester; }
    public Session getSession() { return session; }
    public Status getStatus() { return status; }
    public LocalDateTime getRequestedAt() { return requestedAt; }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    public void accept() {
        status = Status.ACCEPTED;
    }

    public void decline() {
        status = Status.DECLINED;
    }
}
