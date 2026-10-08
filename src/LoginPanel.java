import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;


public class LoginPanel extends JPanel {
    private final SessionManager manager;
    private final Consumer<Student> onLogin;
    private boolean registerMode = false;

    private final JTextField email = Theme.field("you@mymail.mapua.edu.ph");
    private final JPasswordField password = Theme.password();
    private final JTextField name = Theme.field("Full name");
    private final JTextField program = Theme.field("Program, e.g. BS Computer Science");
    private final JComboBox<Integer> year = Theme.combo(new Integer[]{1, 2, 3, 4, 5});
    private final JPanel registerFields = Theme.vbox();
    private final JLabel title = Theme.heading("Welcome back", 22);
    private final JLabel subtitle = Theme.muted("Sign in to see who is studying right now.");
    private final JButton submit = Theme.primary("Sign in");
    private final JButton toggle = Theme.ghost("Create an account");
    private final Theme.Notice notice = new Theme.Notice();

    public LoginPanel(SessionManager manager, Consumer<Student> onLogin) {
        this.manager = manager; this.onLogin = onLogin;
        setLayout(new GridBagLayout()); setBackground(Theme.BG);

        JPanel card = Theme.card(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JPanel fixedWidth = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() { return new Dimension(420, super.getPreferredSize().height); }
        };
        fixedWidth.setOpaque(false); fixedWidth.add(card);

        JLabel brand = Theme.heading("Campus Study Session Board", 13); brand.setForeground(Theme.NAVY);
        card.add(left(brand)); card.add(Theme.gap(14));
        card.add(left(title)); card.add(Theme.gap(4)); card.add(left(subtitle)); card.add(Theme.gap(18));

        registerFields.add(labeled("Name", name)); registerFields.add(Theme.gap(10));
        registerFields.add(labeled("Program", program)); registerFields.add(Theme.gap(10));
        registerFields.add(labeled("Year level", year)); registerFields.add(Theme.gap(10));
        registerFields.setVisible(false);
        card.add(registerFields);

        card.add(labeled("E-mail", email)); card.add(Theme.gap(10));
        card.add(labeled("Password", password)); card.add(Theme.gap(6));
        card.add(left(notice)); card.add(Theme.gap(12));

        submit.setAlignmentX(LEFT_ALIGNMENT); submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(submit); card.add(Theme.gap(8));
        toggle.setAlignmentX(LEFT_ALIGNMENT); toggle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(toggle);
        card.add(Theme.gap(10));
        JLabel hint = Theme.muted("Demo account: althea@mymail.mapua.edu.ph / password123"); hint.setFont(Theme.font(11));
        card.add(left(hint));

        add(fixedWidth);

        submit.addActionListener(e -> submit());
        password.addActionListener(e -> submit());
        toggle.addActionListener(e -> setMode(!registerMode));
    }

    private void setMode(boolean register) {
        registerMode = register; notice.clear();
        registerFields.setVisible(register);
        title.setText(register ? "Create your account" : "Welcome back");
        subtitle.setText(register ? "Use your school e-mail. Password must be 8+ characters." : "Sign in to see who is studying right now.");
        submit.setText(register ? "Create account" : "Sign in");
        toggle.setText(register ? "I already have an account" : "Create an account");
        revalidate(); repaint();
    }

    private void submit() {
        try {
            Student s;
            if (registerMode) {
                s = manager.register(name.getText(), email.getText(), new String(password.getPassword()),
                        program.getText(), (Integer) year.getSelectedItem());
            } else {
                s = manager.login(email.getText(), new String(password.getPassword()));
            }
            notice.clear(); password.setText("");
            onLogin.accept(s);
        } catch (SessionManager.ValidationException ex) {
            notice.error(ex.getMessage());
        }
    }

    private static JComponent left(JComponent c) { c.setAlignmentX(LEFT_ALIGNMENT); return c; }
    static JPanel labeled(String label, JComponent input) {
        JPanel p = Theme.vbox(); p.setAlignmentX(LEFT_ALIGNMENT);
        JLabel l = Theme.formLabel(label); l.setAlignmentX(LEFT_ALIGNMENT);
        input.setAlignmentX(LEFT_ALIGNMENT); input.setMaximumSize(new Dimension(Integer.MAX_VALUE, input.getPreferredSize().height));
        p.add(l); p.add(Theme.gap(4)); p.add(input); return p;
    }
}
