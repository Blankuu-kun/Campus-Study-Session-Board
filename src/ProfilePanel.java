import javax.swing.*;
import java.awt.*;


public class ProfilePanel extends JPanel {
    private final SessionManager manager;
    private final MainFrame frame;
    private final JTextField name = Theme.field("Full name");
    private final JTextField program = Theme.field("Program");
    private final JComboBox<Integer> year = Theme.combo(new Integer[]{1, 2, 3, 4, 5, 6});
    private final JLabel email = Theme.label("", 13, Theme.MUTED);
    private final Theme.Notice notice = new Theme.Notice();

    public ProfilePanel(SessionManager manager, MainFrame frame) {
        this.manager = manager; this.frame = frame;
        setLayout(new BorderLayout()); setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        JPanel titles = Theme.vbox();
        titles.add(Theme.heading("Profile", 24)); titles.add(Theme.gap(2));
        titles.add(Theme.muted("This is what other students see on your sessions and requests."));
        add(titles, BorderLayout.NORTH);

        JPanel card = Theme.card(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(520, 380));
        card.add(LoginPanel.labeled("Name", name)); card.add(Theme.gap(12));
        card.add(LoginPanel.labeled("Program", program)); card.add(Theme.gap(12));
        card.add(LoginPanel.labeled("Year level", year)); card.add(Theme.gap(12));
        JPanel em = Theme.vbox(); em.setAlignmentX(LEFT_ALIGNMENT);
        JLabel el = Theme.formLabel("E-mail (cannot be changed)"); el.setAlignmentX(LEFT_ALIGNMENT); email.setAlignmentX(LEFT_ALIGNMENT);
        em.add(el); em.add(Theme.gap(4)); em.add(email); card.add(em); card.add(Theme.gap(10));
        notice.setAlignmentX(LEFT_ALIGNMENT); notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.add(notice); card.add(Theme.gap(12));
        JPanel actions = Theme.hbox(); actions.setAlignmentX(LEFT_ALIGNMENT);
        JButton save = Theme.primary("Save changes"); actions.add(save); card.add(actions);
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 20)); wrap.setOpaque(false); wrap.add(card);
        add(wrap, BorderLayout.CENTER);

        save.addActionListener(e -> {
            try {
                manager.updateProfile(frame.getCurrentUser(), name.getText(), program.getText(), (Integer) year.getSelectedItem());
                notice.success("Profile saved."); frame.refreshSidebar();
            } catch (SessionManager.ValidationException ex) { notice.error(ex.getMessage()); }
        });
    }

    public void refresh() {
        Student s = frame.getCurrentUser();
        name.setText(s.getName()); program.setText(s.getProgram()); year.setSelectedItem(s.getYearLevel()); email.setText(s.getEmail());
        notice.clear();
    }
}
