import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


public class PostSessionPanel extends JPanel {
    private final SessionManager manager;
    private final MainFrame frame;

    private final JToggleButton spotBtn = typeButton("Study Spot", "I am here now, seats are open");
    private final JToggleButton reviewBtn = typeButton("Review Group", "Scheduled for a later date and time");
    private final JTextField subject = Theme.field("e.g. CSS123P");
    private final JTextArea description = Theme.area(3);
    private final JComboBox<Integer> capacity = Theme.combo(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    private final JTextField location = Theme.field("e.g. Library, 3rd floor, Table 12");
    private final JTextField date = Theme.field("YYYY-MM-DD");
    private final JTextField time = Theme.field("HH:MM (24-hour)");
    private final JPanel spotFields = Theme.vbox();
    private final JPanel reviewFields = Theme.vbox();
    private final JLabel capacityLabel = Theme.formLabel("Available seats (1 to 20)");
    private final Theme.Notice notice = new Theme.Notice();

    public PostSessionPanel(SessionManager manager, MainFrame frame) {
        this.manager = manager; this.frame = frame;
        setLayout(new BorderLayout()); setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel titles = Theme.vbox();
        titles.add(Theme.heading("Post a session", 24)); titles.add(Theme.gap(2));
        titles.add(Theme.muted("Tell other students what you are studying and how many can join."));
        add(titles, BorderLayout.NORTH);

        JPanel card = Theme.card(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(640, Integer.MAX_VALUE));

        ButtonGroup g = new ButtonGroup(); g.add(spotBtn); g.add(reviewBtn); spotBtn.setSelected(true);
        JPanel types = new JPanel(new GridLayout(1, 2, 12, 0)); types.setOpaque(false); types.setAlignmentX(LEFT_ALIGNMENT);
        types.add(spotBtn); types.add(reviewBtn); types.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        card.add(LoginPanel.labeled("Session type", types)); card.add(Theme.gap(16));

        card.add(LoginPanel.labeled("Subject", subject)); card.add(Theme.gap(12));
        JScrollPane descScroll = new JScrollPane(description); descScroll.setBorder(null); descScroll.setPreferredSize(new Dimension(300, 80));
        card.add(LoginPanel.labeled("Description", descScroll)); card.add(Theme.gap(12));

        capacity.setPreferredSize(new Dimension(120, 38)); capacity.setSelectedItem(4);
        JPanel capRow = Theme.vbox(); capRow.setAlignmentX(LEFT_ALIGNMENT);
        capacityLabel.setAlignmentX(LEFT_ALIGNMENT); capacity.setAlignmentX(LEFT_ALIGNMENT);
        capacity.setMaximumSize(new Dimension(120, 38));
        capRow.add(capacityLabel); capRow.add(Theme.gap(4)); capRow.add(capacity);
        card.add(capRow); card.add(Theme.gap(12));

        spotFields.add(LoginPanel.labeled("Location", location)); spotFields.setAlignmentX(LEFT_ALIGNMENT);
        JPanel when = new JPanel(new GridLayout(1, 2, 12, 0)); when.setOpaque(false); when.setAlignmentX(LEFT_ALIGNMENT);
        when.add(LoginPanel.labeled("Date", date)); when.add(LoginPanel.labeled("Time", time));
        when.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        reviewFields.add(when); reviewFields.setAlignmentX(LEFT_ALIGNMENT); reviewFields.setVisible(false);
        card.add(spotFields); card.add(reviewFields); card.add(Theme.gap(8));

        notice.setAlignmentX(LEFT_ALIGNMENT); notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.add(notice); card.add(Theme.gap(12));

        JPanel actions = Theme.hbox(); actions.setAlignmentX(LEFT_ALIGNMENT);
        JButton post = Theme.primary("Post session"); JButton cancel = Theme.ghost("Cancel");
        actions.add(post); actions.add(cancel); card.add(actions);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 20)); wrap.setOpaque(false);
        card.setPreferredSize(new Dimension(640, 520));
        wrap.add(card);
        add(Theme.scroll(wrap), BorderLayout.CENTER);

        spotBtn.addActionListener(e -> setType(true));
        reviewBtn.addActionListener(e -> setType(false));
        post.addActionListener(e -> submit());
        cancel.addActionListener(e -> { reset(); frame.show("board"); });
    }

    private void setType(boolean spot) {
        spotFields.setVisible(spot); reviewFields.setVisible(!spot);
        capacityLabel.setText(spot ? "Available seats (1 to 20)" : "Maximum members (1 to 20)");
        notice.clear(); revalidate(); repaint();
    }

    public void submit() {
        try {
            int cap = (Integer) capacity.getSelectedItem();
            if (spotBtn.isSelected()) {
                manager.postStudySpot(frame.getCurrentUser(), subject.getText(), description.getText(), cap, location.getText());
            } else {
                LocalDate d; LocalTime t;
                try { d = LocalDate.parse(date.getText().trim()); }
                catch (DateTimeParseException ex) { throw new SessionManager.ValidationException("Enter the date as YYYY-MM-DD, e.g. 2026-09-20."); }
                try { t = LocalTime.parse(time.getText().trim(), DateTimeFormatter.ofPattern("H:mm")); }
                catch (DateTimeParseException ex) { throw new SessionManager.ValidationException("Enter the time as HH:MM in 24-hour format, e.g. 14:00."); }
                manager.postReviewGroup(frame.getCurrentUser(), subject.getText(), description.getText(), cap, d, t);
            }
            reset();
            frame.toast("Session posted. It is now visible on the board.");
            frame.show("board");
        } catch (SessionManager.ValidationException ex) { notice.error(ex.getMessage()); }
    }

    public void reset() {
        subject.setText(""); description.setText(""); capacity.setSelectedItem(4); location.setText(""); date.setText(""); time.setText("");
        spotBtn.setSelected(true); setType(true); notice.clear();
    }

    private static JToggleButton typeButton(String title, String sub) {
        JToggleButton b = new JToggleButton("<html><b>" + title + "</b><br><span style='color:#6B7280'>" + sub + "</span></html>") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? Theme.ACCENT_SOFT : Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(isSelected() ? Theme.NAVY : Theme.LINE);
                g2.setStroke(new BasicStroke(isSelected() ? 2f : 1f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(Theme.font(13)); b.setForeground(Theme.INK); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT); b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
