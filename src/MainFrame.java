import javax.swing.*;
import java.awt.*;


public class MainFrame extends JFrame {
    private final SessionManager manager;
    private Student currentUser;

    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);
    private final CardLayout contentCards = new CardLayout();
    private final JPanel content = new JPanel(contentCards);
    // sidebar: the key of each screen, its label, and its button 
    private final String[] navKeys = {"board", "post", "mine", "profile"};
    private final String[] navLabels = {"Browse sessions", "Post a session", "My sessions", "Profile"};
    private final JToggleButton[] navButtons = new JToggleButton[4];
    private final JLabel userName = Theme.heading("", 13);
    private final JLabel userMeta = Theme.muted("");
    private final JLabel toast = new JLabel();
    private final Timer toastTimer = new Timer(2600, e -> toast.setVisible(false));

    private final LoginPanel login;
    private final BoardPanel board;
    private final PostSessionPanel post;
    private final MySessionsPanel mine;
    private final ProfilePanel profile;

    
    public static void main(String[] args) {
        Theme.install();

        Database db = new Database();
        try {
            db.connect();   // open the connection to MySQL
            db.load();      // read all tables into the ArrayLists
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the MySQL database.\n\n" + e.getMessage()
                    + "\n\nCheck that MySQL is running, that you ran database.sql,"
                    + "\nand that USER / PASSWORD in Database.java are correct.",
                    "Database error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SessionManager manager = new SessionManager(db);
        if (db.students.isEmpty()) {
            manager.addSampleData();    
        }

        SwingUtilities.invokeLater(() -> new MainFrame(manager).setVisible(true));
    }

    public MainFrame(SessionManager manager) {
        super("Campus Study Session Board");
        this.manager = manager;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 740); setMinimumSize(new Dimension(900, 600)); setLocationRelativeTo(null);

        login = new LoginPanel(manager, this::login);
        board = new BoardPanel(manager, this);
        post = new PostSessionPanel(manager, this);
        mine = new MySessionsPanel(manager, this);
        profile = new ProfilePanel(manager, this);

        content.setOpaque(false);
        content.add(board, "board"); content.add(post, "post"); content.add(mine, "mine"); content.add(profile, "profile");

        JPanel shell = new JPanel(new BorderLayout()); shell.setBackground(Theme.BG);
        shell.add(sidebar(), BorderLayout.WEST);
        JPanel layered = new JPanel(new BorderLayout()); layered.setOpaque(false);
        layered.add(content, BorderLayout.CENTER);
        toast.setFont(Theme.font(13)); toast.setForeground(Color.WHITE); toast.setOpaque(true); toast.setBackground(Theme.INK);
        toast.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16)); toast.setVisible(false);
        JPanel toastRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8)); toastRow.setOpaque(false); toastRow.add(toast);
        layered.add(toastRow, BorderLayout.SOUTH);
        shell.add(layered, BorderLayout.CENTER);

        root.add(login, "login"); root.add(shell, "app");
        setContentPane(root);
        rootCards.show(root, "login");
        toastTimer.setRepeats(false);
    }

    private JComponent sidebar() {
        JPanel side = new JPanel(); side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(Theme.NAVY); side.setPreferredSize(new Dimension(230, 0));
        side.setBorder(BorderFactory.createEmptyBorder(26, 18, 22, 18));
        JLabel brand = new JLabel("<html>Campus Study<br>Session Board</html>"); brand.setFont(Theme.bold(16)); brand.setForeground(Color.WHITE);
        brand.setAlignmentX(LEFT_ALIGNMENT); side.add(brand); side.add(Theme.gap(30));

        ButtonGroup g = new ButtonGroup();
        for (int i = 0; i < navKeys.length; i++) {
            final String key = navKeys[i];
            navButtons[i] = navButton(navLabels[i]);
            g.add(navButtons[i]);
            navButtons[i].addActionListener(e -> show(key));
            side.add(navButtons[i]); side.add(Theme.gap(4));
        }
        side.add(Box.createVerticalGlue());
        userName.setForeground(Color.WHITE); userName.setAlignmentX(LEFT_ALIGNMENT);
        userMeta.setForeground(new Color(0xB8, 0xC7, 0xDD)); userMeta.setAlignmentX(LEFT_ALIGNMENT);
        side.add(userName); side.add(userMeta); side.add(Theme.gap(12));
        JButton logout = Theme.button("Log out", new Color(0xDD, 0xE6, 0xF4), new Color(0x2A, 0x4A, 0x74), new Color(0x35, 0x58, 0x86));
        logout.setAlignmentX(LEFT_ALIGNMENT); logout.addActionListener(e -> { currentUser = null; rootCards.show(root, "login"); });
        side.add(logout);
        return side;
    }

    private static JToggleButton navButton(String text) {
        JToggleButton b = new JToggleButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) { g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); }
                else if (getModel().isRollover()) { g2.setColor(new Color(255, 255, 255, 28)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); }
                g2.dispose();
                setForeground(isSelected() ? Theme.NAVY : new Color(0xDD, 0xE6, 0xF4));
                super.paintComponent(g);
            }
        };
        b.setFont(Theme.bold(13)); b.setContentAreaFilled(false); b.setFocusPainted(false); b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14)); b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public void login(Student s) {
        currentUser = s; refreshSidebar();
        rootCards.show(root, "app"); show("board");
    }

    /** Navigate to a content card and refresh it. */
    public void show(String key) {
        for (int i = 0; i < navKeys.length; i++) {
            if (navKeys[i].equals(key)) navButtons[i].setSelected(true);
        }
        switch (key) {
            case "board": board.refresh(); break;
            case "post": post.reset(); break;
            case "mine": mine.refresh(); break;
            case "profile": profile.refresh(); break;
        }
        contentCards.show(content, key);
    }

    public void refreshSidebar() {
        userName.setText(currentUser.getName());
        userMeta.setText(currentUser.getProgram() + " · Year " + currentUser.getYearLevel());
    }

    public void toast(String msg) { toast.setText(msg); toast.setVisible(true); toastTimer.restart(); }
    public Student getCurrentUser() { return currentUser; }
    public BoardPanel board() { return board; }
    public PostSessionPanel post() { return post; }
}
