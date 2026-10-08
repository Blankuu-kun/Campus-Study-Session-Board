import javax.swing.*;
import java.awt.*;
import java.util.List;


public class MySessionsPanel extends JPanel {
    private final SessionManager manager;
    private final MainFrame frame;
    private final JPanel created = Theme.vbox();
    private final JPanel joined = Theme.vbox();
    private final JToggleButton tabCreated = tab("Created by me");
    private final JToggleButton tabJoined = tab("Joined / requested");
    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);

    public MySessionsPanel(SessionManager manager, MainFrame frame) {
        this.manager = manager; this.frame = frame;
        setLayout(new BorderLayout(0, 16)); setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel titles = Theme.vbox();
        titles.add(Theme.heading("My sessions", 24)); titles.add(Theme.gap(2));
        titles.add(Theme.muted("Manage join requests for your sessions and see the ones you joined."));
        JPanel tabs = Theme.hbox(); ButtonGroup g = new ButtonGroup(); g.add(tabCreated); g.add(tabJoined);
        tabs.add(tabCreated); tabs.add(tabJoined); tabCreated.setSelected(true);
        titles.setAlignmentX(LEFT_ALIGNMENT); tabs.setAlignmentX(LEFT_ALIGNMENT);
        JPanel top = Theme.vbox(); top.add(titles); top.add(Theme.gap(16)); top.add(tabs);
        add(top, BorderLayout.NORTH);

        deck.setOpaque(false);
        deck.add(wrap(created), "created"); deck.add(wrap(joined), "joined");
        add(deck, BorderLayout.CENTER);
        tabCreated.addActionListener(e -> cards.show(deck, "created"));
        tabJoined.addActionListener(e -> cards.show(deck, "joined"));
    }

    private JScrollPane wrap(JPanel list) {
        JPanel w = new JPanel(new BorderLayout()); w.setOpaque(false); w.add(list, BorderLayout.NORTH); return Theme.scroll(w);
    }

    public void refresh() {
        Student me = frame.getCurrentUser();
        created.removeAll(); joined.removeAll();

        List<Session> mine = manager.getSessionsCreatedBy(me);
        if (mine.isEmpty()) created.add(emptyCard("You have not posted a session yet."));
        for (Session s : mine) { created.add(creatorCard(s, me)); created.add(Theme.gap(12)); }

        List<JoinRequest> reqs = manager.getRequestsBy(me);
        if (reqs.isEmpty()) joined.add(emptyCard("You have not requested to join any session yet."));
        for (JoinRequest r : reqs) {
            Session s = r.getSession();
            String action = r.getStatus() == JoinRequest.Status.ACCEPTED ? "View members" : "View details";
            JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
            BoardPanel.SessionCard card = new BoardPanel.SessionCard(s, action, () -> frame.board().showDetails(s));
            JPanel status = Theme.hbox(); status.setBorder(BorderFactory.createEmptyBorder(0, 4, 6, 0));
            status.add(statusPill(r.getStatus()));
            JPanel v = Theme.vbox(); v.add(status); v.add(card);
            row.add(v, BorderLayout.CENTER);
            joined.add(row); joined.add(Theme.gap(12));
        }
        created.revalidate(); created.repaint(); joined.revalidate(); joined.repaint();
    }

    private JComponent creatorCard(Session s, Student me) {
        JPanel card = Theme.card(); card.setLayout(new BorderLayout(0, 12));
        JPanel head = new JPanel(new BorderLayout(16, 0)); head.setOpaque(false);
        JPanel text = Theme.vbox();
        JPanel top = Theme.hbox(); top.add(BoardPanel.SessionCard.typePill(s)); top.add(Theme.heading(s.getSubject(), 15));
        top.add(s.isOpen() ? Theme.pill("OPEN", Theme.OK, Theme.OK_SOFT) : Theme.pill("CLOSED", Theme.MUTED, Theme.LINE));
        top.setAlignmentX(LEFT_ALIGNMENT); text.add(top); text.add(Theme.gap(6));
        JLabel details = Theme.label(s.getDetails(), 13, Theme.INK); details.setAlignmentX(LEFT_ALIGNMENT); text.add(details);
        text.add(Theme.gap(4));
        StringBuilder mem = new StringBuilder("Members: you");
        for (Student m : s.getMembers()) mem.append(", ").append(m.getName());
        JLabel members = Theme.muted(mem.toString()); members.setAlignmentX(LEFT_ALIGNMENT); text.add(members);
        head.add(text, BorderLayout.CENTER);
        if (s.isOpen()) {
            JButton close = Theme.danger("Close session");
            close.addActionListener(e -> {
                int ok = JOptionPane.showConfirmDialog(frame, "Close this session? Pending requests will be declined.", "Close session",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (ok == JOptionPane.OK_OPTION) {
                    try { manager.closeSession(s.getSessionId(), me); frame.toast("Session closed."); }
                    catch (SessionManager.ValidationException ex) { frame.toast(ex.getMessage()); }
                    refresh();
                }
            });
            JPanel right = Theme.vbox(); right.add(close); head.add(right, BorderLayout.EAST);
        }
        card.add(head, BorderLayout.NORTH);

        List<JoinRequest> pending = manager.getPendingRequests(s.getSessionId());
        JPanel reqBox = Theme.vbox();
        JLabel rh = Theme.formLabel(pending.isEmpty() ? "NO PENDING REQUESTS" : "PENDING REQUESTS (" + pending.size() + ")");
        rh.setAlignmentX(LEFT_ALIGNMENT); reqBox.add(rh); reqBox.add(Theme.gap(6));
        for (JoinRequest r : pending) {
            JPanel row = new JPanel(new BorderLayout(12, 0)); row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.LINE),
                    BorderFactory.createEmptyBorder(8, 0, 4, 0)));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            JPanel who = Theme.vbox();
            JLabel n = Theme.label(r.getRequester().getName(), 13, Theme.INK); n.setFont(Theme.bold(13)); n.setAlignmentX(LEFT_ALIGNMENT);
            JLabel p = Theme.muted(r.getRequester().getProgram() + "  ·  Year " + r.getRequester().getYearLevel()); p.setAlignmentX(LEFT_ALIGNMENT);
            who.add(n); who.add(p);
            row.add(who, BorderLayout.CENTER);
            JPanel btns = Theme.hbox();
            JButton accept = Theme.success("Accept"); JButton decline = Theme.ghost("Decline");
            accept.addActionListener(e -> {
                try { manager.acceptRequest(r.getRequestId(), me); frame.toast(r.getRequester().getName() + " added to " + s.getSubject() + "."); }
                catch (SessionManager.ValidationException ex) { frame.toast(ex.getMessage()); }
                refresh();
            });
            decline.addActionListener(e -> {
                try { manager.declineRequest(r.getRequestId(), me); }
                catch (SessionManager.ValidationException ex) { frame.toast(ex.getMessage()); }
                refresh();
            });
            btns.add(accept); btns.add(decline); row.add(btns, BorderLayout.EAST);
            reqBox.add(row);
        }
        card.add(reqBox, BorderLayout.CENTER);
        JPanel holder = new JPanel(new BorderLayout()); holder.setOpaque(false); holder.add(card);
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140 + pending.size() * 52));
        return holder;
    }

    private static JComponent emptyCard(String msg) {
        JPanel p = Theme.card(); p.setLayout(new BorderLayout()); p.add(Theme.label(msg, 13, Theme.MUTED));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70)); return p;
    }

    static JComponent statusPill(JoinRequest.Status st) {
        switch (st) {
            case ACCEPTED: return Theme.pill("ACCEPTED", Theme.OK, Theme.OK_SOFT);
            case DECLINED: return Theme.pill("DECLINED", Theme.DANGER, Theme.DANGER_SOFT);
            default: return Theme.pill("PENDING", Theme.REVIEW, Theme.REVIEW_SOFT);
        }
    }

    private static JToggleButton tab(String text) {
        JToggleButton b = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? Theme.NAVY : Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (!isSelected()) { g2.setColor(Theme.LINE); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20); }
                g2.dispose();
                setForeground(isSelected() ? Color.WHITE : Theme.INK);
                super.paintComponent(g);
            }
        };
        b.setFont(Theme.bold(12)); b.setContentAreaFilled(false); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
