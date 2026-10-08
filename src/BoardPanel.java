import javax.swing.*;
import java.awt.*;
import java.util.List;


public class BoardPanel extends JPanel {
    private final SessionManager manager;
    private final MainFrame frame;
    private final JTextField search = Theme.field("Search subject, keyword, or place");
    private final JComboBox<String> type = Theme.combo(new String[]{"All types", "Study Spot", "Review Group"});
    private final JPanel list = Theme.vbox();
    private final JLabel count = Theme.muted("");

    public BoardPanel(SessionManager manager, MainFrame frame) {
        this.manager = manager; this.frame = frame;
        setLayout(new BorderLayout(0, 16)); setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel header = new JPanel(new BorderLayout()); header.setOpaque(false);
        JPanel titles = Theme.vbox();
        titles.add(Theme.heading("Study sessions", 24));
        titles.add(Theme.gap(2));
        titles.add(Theme.muted("Open sessions on campus right now and this week."));
        header.add(titles, BorderLayout.WEST);
        JButton post = Theme.primary("+  Post a session"); post.addActionListener(e -> frame.show("post"));
        header.add(post, BorderLayout.EAST);

        JPanel filters = new JPanel(new BorderLayout(10, 0)); filters.setOpaque(false);
        search.setPreferredSize(new Dimension(360, 38));
        type.setPreferredSize(new Dimension(160, 38));
        filters.add(search, BorderLayout.CENTER); filters.add(type, BorderLayout.EAST);
        JPanel filterRow = new JPanel(new BorderLayout()); filterRow.setOpaque(false);
        filterRow.add(filters, BorderLayout.WEST); filterRow.add(count, BorderLayout.EAST);

        JPanel top = Theme.vbox(); top.add(header); top.add(Theme.gap(18)); top.add(filterRow);
        add(top, BorderLayout.NORTH);

        JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false); wrap.add(list, BorderLayout.NORTH);
        add(Theme.scroll(wrap), BorderLayout.CENTER);

        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refresh(); }
        });
        type.addActionListener(e -> refresh());
    }

    public void refresh() {
        list.removeAll();
        Session.Type t = type.getSelectedIndex() == 1 ? Session.Type.STUDY_SPOT : type.getSelectedIndex() == 2 ? Session.Type.REVIEW_GROUP : null;
        List<Session> sessions = manager.getOpenSessions(search.getText(), t);
        count.setText(sessions.size() + " open session" + (sessions.size() == 1 ? "" : "s"));
        if (sessions.isEmpty()) {
            JPanel empty = Theme.card(); empty.setLayout(new BorderLayout());
            empty.add(Theme.label("No sessions match. Try another subject, or post one yourself.", 13, Theme.MUTED));
            list.add(empty);
        }
        for (Session s : sessions) {
            list.add(new SessionCard(s, "View details", () -> showDetails(s)));
            list.add(Theme.gap(12));
        }
        list.revalidate(); list.repaint();
    }

    /** Session details as a modal sheet with the join action and inline validation. */
    public void showDetails(Session s) {
        Student me = frame.getCurrentUser();
        JDialog dlg = new JDialog(frame, "Session details", true);
        dlg.setUndecorated(false);
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(Theme.BG);
        root.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Theme.LINE), BorderFactory.createEmptyBorder(24, 28, 24, 28)));

        JPanel body = Theme.vbox();
        JPanel top = Theme.hbox(); top.add(SessionCard.typePill(s));
        if (!s.isOpen()) top.add(Theme.pill("CLOSED", Theme.MUTED, Theme.LINE));
        top.setAlignmentX(LEFT_ALIGNMENT); body.add(top); body.add(Theme.gap(8));
        JLabel h = Theme.heading(s.getSubject(), 22); h.setAlignmentX(LEFT_ALIGNMENT); body.add(h); body.add(Theme.gap(4));
        JLabel d = Theme.label(s.getDetails(), 14, Theme.INK); d.setAlignmentX(LEFT_ALIGNMENT); body.add(d); body.add(Theme.gap(14));

        body.add(row("Description", s.getDescription()));
        body.add(row("Posted by", s.getCreator().getName() + "  ·  " + s.getCreator().getProgram()));
        String cap = s.getType() == Session.Type.STUDY_SPOT
                ? s.getSeatsLeft() + " of " + s.getCapacity() + " seats open"
                : s.getMembers().size() + " of " + s.getCapacity() + " member slots filled";
        body.add(row("Capacity", cap));
        StringBuilder members = new StringBuilder(s.getCreator().getName() + " (creator)");
        for (Student m : s.getMembers()) members.append(", ").append(m.getName());
        body.add(row("Members", members.toString()));
        body.add(Theme.gap(12));

        Theme.Notice notice = new Theme.Notice(); notice.setAlignmentX(LEFT_ALIGNMENT);
        notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        body.add(notice); body.add(Theme.gap(12));

        JPanel actions = Theme.hbox(); actions.setAlignmentX(LEFT_ALIGNMENT);
        JButton join = Theme.primary("Request to join");
        JButton close = Theme.ghost("Close");
        JoinRequest mine = manager.getMyRequest(me, s);
        if (s.isCreator(me)) { join.setEnabled(false); join.setText("You created this session"); }
        else if (s.isMember(me)) { join.setEnabled(false); join.setText("You are a member"); }
        else if (mine != null && mine.getStatus() == JoinRequest.Status.PENDING) { join.setEnabled(false); join.setText("Request pending"); }
        join.addActionListener(e -> {
            try {
                manager.requestToJoin(me, s.getSessionId());
                notice.success("Join request sent. The creator will accept or decline it.");
                join.setEnabled(false); join.setText("Request pending");
            } catch (SessionManager.ValidationException ex) { notice.error(ex.getMessage()); }
        });
        close.addActionListener(e -> dlg.dispose());
        actions.add(join); actions.add(close);
        body.add(actions);

        root.add(body, BorderLayout.CENTER);
        dlg.setContentPane(root); dlg.setSize(520, 430); dlg.setLocationRelativeTo(frame);
        dlg.setVisible(true);
        refresh();
    }

    private static JComponent row(String k, String v) {
        JPanel p = new JPanel(new BorderLayout(12, 0)); p.setOpaque(false); p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel key = Theme.formLabel(k); key.setPreferredSize(new Dimension(90, 20)); key.setVerticalAlignment(SwingConstants.TOP);
        JLabel val = new JLabel("<html><body style='width:330px'>" + v + "</body></html>"); val.setFont(Theme.font(13)); val.setForeground(Theme.INK);
        p.add(key, BorderLayout.WEST); p.add(val, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    
    public static class SessionCard extends JPanel {

        public SessionCard(Session s, String actionText, Runnable action) {
            JPanel card = Theme.card(); card.setLayout(new BorderLayout(16, 0));
            setLayout(new BorderLayout()); setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

            JPanel text = Theme.vbox();
            JPanel top = Theme.hbox();
            top.add(typePill(s));
            top.add(Theme.heading(s.getSubject(), 15));
            if (!s.isOpen()) top.add(Theme.pill("CLOSED", Theme.MUTED, Theme.LINE));
            top.setAlignmentX(LEFT_ALIGNMENT);
            text.add(top); text.add(Theme.gap(6));
            JLabel details = Theme.label(s.getDetails(), 13, Theme.INK); details.setAlignmentX(LEFT_ALIGNMENT);
            text.add(details); text.add(Theme.gap(4));
            String desc = s.getDescription().length() > 90 ? s.getDescription().substring(0, 88) + "…" : s.getDescription();
            JLabel d = Theme.muted(desc); d.setAlignmentX(LEFT_ALIGNMENT); text.add(d); text.add(Theme.gap(6));
            JLabel by = Theme.muted("Posted by " + s.getCreator().getName()); by.setFont(Theme.font(11)); by.setAlignmentX(LEFT_ALIGNMENT);
            text.add(by);
            card.add(text, BorderLayout.CENTER);

            JPanel right = Theme.vbox();
            JLabel seats = Theme.heading(String.valueOf(s.getSeatsLeft()), 24);
            seats.setForeground(s.getSeatsLeft() > 0 && s.isOpen() ? Theme.NAVY : Theme.MUTED);
            seats.setAlignmentX(RIGHT_ALIGNMENT);
            JLabel seatsLbl = Theme.muted(s.getType() == Session.Type.STUDY_SPOT ? "seats open" : "slots left"); seatsLbl.setAlignmentX(RIGHT_ALIGNMENT);
            right.add(seats); right.add(seatsLbl); right.add(Theme.gap(10));
            if (actionText != null) {
                JButton b = Theme.secondary(actionText); b.setAlignmentX(RIGHT_ALIGNMENT);
                b.addActionListener(e -> action.run()); right.add(b);
            }
            card.add(right, BorderLayout.EAST);
            add(card, BorderLayout.CENTER);
        }

        public static JComponent typePill(Session s) {
            return s.getType() == Session.Type.STUDY_SPOT
                    ? Theme.pill("STUDY SPOT", Theme.SPOT, Theme.SPOT_SOFT)
                    : Theme.pill("REVIEW GROUP", Theme.REVIEW, Theme.REVIEW_SOFT);
        }
    }
}
