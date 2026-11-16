package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.controller.BookingController;
import hk.edu.polyu.comp.comp2021.crobs.controller.CommandController;
import hk.edu.polyu.comp.comp2021.crobs.controller.RoomController;
import hk.edu.polyu.comp.comp2021.crobs.controller.UserController;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.bookings.ScheduleEntry;
import hk.edu.polyu.comp.comp2021.crobs.model.core.CrobsSystem;
import hk.edu.polyu.comp.comp2021.crobs.model.exceptions.CrobsException;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.Room;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.RoomFeature;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.RoomField;
import hk.edu.polyu.comp.comp2021.crobs.model.rooms.RoomType;
import hk.edu.polyu.comp.comp2021.crobs.model.users.User;
import hk.edu.polyu.comp.comp2021.crobs.util.CommandLogger;
import hk.edu.polyu.comp.comp2021.crobs.util.HtmlLogger;
import hk.edu.polyu.comp.comp2021.crobs.util.TextLogger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Swing graphical user interface (BON1) for operating CROBS via {@link CommandController}
 * while preserving all CLI logging, validation, and undo/redo behavior.
 */
public class GuiView {
    private static final int WINDOW_WIDTH = 1080;
    private static final int WINDOW_HEIGHT = 760;
    private static final int PANEL_PADDING = 8;
    private static final int FLOW_GAP = 6;
    private static final int GRID_PAD = 4;
    private static final int FIELD_COLUMNS_SHORT = 10;
    private static final int FIELD_COLUMNS_MEDIUM = 16;
    private static final int FIELD_COLUMNS_WIDE = 36;
    private static final int LOG_ROWS = 8;
    private static final int LOG_COLUMNS = 80;
    private static final int MONOSPACE_FONT_SIZE = 12;

    private static final int COL_INDEX_ZERO = 0;
    private static final int COL_INDEX_ONE = 1;
    private static final int COL_INDEX_TWO = 2;
    private static final int COL_INDEX_THREE = 3;
    private static final int COL_INDEX_FOUR = 4;
    private static final int COL_INDEX_FIVE = 5;
    private static final int COL_INDEX_SIX = 6;
    private static final int ROW_INDEX_ZERO = 0;
    private static final int ROW_INDEX_ONE = 1;
    private static final int ROW_INDEX_TWO = 2;
    private static final int ZERO_ARG = 0;
    private static final int NEXT_ARG_OFFSET = 1;

    private static final String DEFAULT_HTML_LOG = "crobs-gui-log.html";
    private static final String DEFAULT_TXT_LOG = "crobs-gui-log.txt";
    private static final String ERROR_PREFIX = "Error:";

    private final CrobsSystem system;
    private final CommandController controller;

    private final JFrame frame;
    private final JLabel sessionLabel;
    private final JTextArea consoleArea;
    private final JTextField rawCommandField;

    private final JTextField loginIdField;
    private final JTextField addUserNameField;
    private final JTextField removeUserIdField;
    private final JTextField roleUserIdField;
    private final JComboBox<String> roleCombo;

    private final JComboBox<String> addRoomTypeCombo;
    private final JTextField addRoomCapField;
    private final JTextField addRoomFeaturesField;
    private final JTextField modRoomIdField;
    private final JComboBox<String> modRoomFieldCombo;
    private final JTextField modRoomValField;
    private final JTextField delListRoomIdField;
    private final DefaultTableModel roomsTableModel;

    private final JTextField bookRoomIdField;
    private final JTextField bookDateField;
    private final JTextField bookStartField;
    private final JTextField bookEndField;
    private final JTextField bookPurposeField;
    private final JTextField modBookingIdField;
    private final JTextField searchDateField;
    private final JTextField searchStartField;
    private final JTextField searchEndField;
    private final JTextField searchCapField;
    private final JTextField searchFeaturesField;
    private final DefaultTableModel bookingsTableModel;

    private final JTextField scheduleRoomIdField;
    private final JTextField scheduleWeekStartField;
    private final DefaultTableModel scheduleTableModel;

    /**
     * Constructs a {@code GuiView} bound to an existing {@link CrobsSystem} and {@link CommandController}.
     *
     * @param system     the model facade
     * @param controller the command controller
     */
    public GuiView(CrobsSystem system, CommandController controller) {
        Objects.requireNonNull(system, "CrobsSystem cannot be null");
        Objects.requireNonNull(controller, "CommandController cannot be null");
        this.system = system;
        this.controller = controller;

        this.frame = new JFrame("CROBS - Campus Room Booking System");
        this.sessionLabel = new JLabel();
        this.consoleArea = new JTextArea(LOG_ROWS, LOG_COLUMNS);
        this.rawCommandField = new JTextField(FIELD_COLUMNS_WIDE);

        this.loginIdField = new JTextField(system.getSuperAdminId(), FIELD_COLUMNS_SHORT);
        this.addUserNameField = new JTextField(FIELD_COLUMNS_MEDIUM);
        this.removeUserIdField = new JTextField(FIELD_COLUMNS_SHORT);
        this.roleUserIdField = new JTextField(FIELD_COLUMNS_SHORT);
        this.roleCombo = new JComboBox<>(new String[]{"regular", "admin"});

        this.addRoomTypeCombo = new JComboBox<>(roomTypeKeywords());
        this.addRoomCapField = new JTextField(FIELD_COLUMNS_SHORT);
        this.addRoomFeaturesField = new JTextField("none", FIELD_COLUMNS_MEDIUM);
        this.modRoomIdField = new JTextField(FIELD_COLUMNS_SHORT);
        this.modRoomFieldCombo = new JComboBox<>(roomFieldKeywords());
        this.modRoomValField = new JTextField(FIELD_COLUMNS_MEDIUM);
        this.delListRoomIdField = new JTextField(FIELD_COLUMNS_SHORT);
        this.roomsTableModel = createReadOnlyTableModel(
                new String[]{"Room ID", "Type", "Capacity", "Features"});

        this.bookRoomIdField = new JTextField(FIELD_COLUMNS_SHORT);
        this.bookDateField = new JTextField(LocalDate.now().toString(), FIELD_COLUMNS_SHORT);
        this.bookStartField = new JTextField("09:00", FIELD_COLUMNS_SHORT);
        this.bookEndField = new JTextField("11:00", FIELD_COLUMNS_SHORT);
        this.bookPurposeField = new JTextField(FIELD_COLUMNS_MEDIUM);
        this.modBookingIdField = new JTextField(FIELD_COLUMNS_SHORT);
        this.searchDateField = new JTextField(LocalDate.now().toString(), FIELD_COLUMNS_SHORT);
        this.searchStartField = new JTextField("09:00", FIELD_COLUMNS_SHORT);
        this.searchEndField = new JTextField("11:00", FIELD_COLUMNS_SHORT);
        this.searchCapField = new JTextField("1", FIELD_COLUMNS_SHORT);
        this.searchFeaturesField = new JTextField("none", FIELD_COLUMNS_MEDIUM);
        this.bookingsTableModel = createReadOnlyTableModel(
                new String[]{"Booking ID", "Room ID", "Time Slot", "Status", "Purpose"});

        this.scheduleRoomIdField = new JTextField(FIELD_COLUMNS_SHORT);
        this.scheduleWeekStartField = new JTextField(LocalDate.now().toString(), FIELD_COLUMNS_SHORT);
        this.scheduleTableModel = createReadOnlyTableModel(
                new String[]{"Time Slot", "Booking ID", "Owner", "Purpose"});

        initializeUi();
    }

    /**
     * Standalone entry point for launching the CROBS Swing GUI directly.
     * Supports optional {@code -html <file>} and {@code -txt <file>} arguments, defaulting to
     * {@code crobs-gui-log.html} and {@code crobs-gui-log.txt} if omitted.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        String htmlPath = DEFAULT_HTML_LOG;
        String txtPath = DEFAULT_TXT_LOG;
        for (int i = ZERO_ARG; i + NEXT_ARG_OFFSET < args.length; i++) {
            if ("-html".equals(args[i])) {
                htmlPath = args[i + NEXT_ARG_OFFSET];
                i += NEXT_ARG_OFFSET;
            } else if ("-txt".equals(args[i])) {
                txtPath = args[i + NEXT_ARG_OFFSET];
                i += NEXT_ARG_OFFSET;
            }
        }
        CrobsSystem crobsSystem = new CrobsSystem();
        List<CommandLogger> loggers = new ArrayList<>();
        loggers.add(new HtmlLogger(htmlPath));
        loggers.add(new TextLogger(txtPath));
        CommandController commandController = new CommandController(crobsSystem, loggers);
        new UserController(crobsSystem, commandController);
        new RoomController(crobsSystem, commandController);
        new BookingController(crobsSystem, commandController);
        GuiView gui = new GuiView(crobsSystem, commandController);
        gui.start();
    }

    /**
     * Displays the GUI window on the Swing Event Dispatch Thread.
     */
    public void start() {
        SwingUtilities.invokeLater(this::showFrame);
    }

    private void showFrame() {
        refreshState();
        frame.setVisible(true);
    }

    private void initializeUi() {
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new FrameCloseHandler(controller));
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(frame.getOwner());
        frame.setLayout(new BorderLayout(FLOW_GAP, FLOW_GAP));

        addRoomFeaturesField.setToolTipText("Comma-separated features without spaces, or none");
        searchFeaturesField.setToolTipText("Comma-separated features without spaces, or none");
        bookPurposeField.setToolTipText("Booking purpose (spaces allowed)");

        frame.add(buildSessionPanel(), BorderLayout.NORTH);
        frame.add(buildTabsPanel(), BorderLayout.CENTER);
        frame.add(buildConsolePanel(), BorderLayout.SOUTH);

        consoleArea.setEditable(false);
        consoleArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, MONOSPACE_FONT_SIZE));
        appendOutput("Welcome to CROBS GUI. Superadmin ID: " + system.getSuperAdminId());
    }

    private JPanel buildSessionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_GAP, FLOW_GAP));
        panel.setBorder(BorderFactory.createTitledBorder("Session & Undo/Redo"));

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> executeCommand("login " + loginIdField.getText().trim()));
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> executeCommand("logout"));
        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> executeCommand("undo"));
        JButton redoButton = new JButton("Redo");
        redoButton.addActionListener(e -> executeCommand("redo"));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshState());
        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> executeCommand("quit"));

        panel.add(new JLabel("User ID:"));
        panel.add(loginIdField);
        panel.add(loginButton);
        panel.add(logoutButton);
        panel.add(undoButton);
        panel.add(redoButton);
        panel.add(refreshButton);
        panel.add(quitButton);
        panel.add(sessionLabel);
        return panel;
    }

    private JTabbedPane buildTabsPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rooms", buildRoomsTab());
        tabs.addTab("Bookings & Search", buildBookingsTab());
        tabs.addTab("Weekly Schedule", buildScheduleTab());
        tabs.addTab("Users", buildUsersTab());
        return tabs;
    }

    private JPanel buildRoomsTab() {
        JPanel root = new JPanel(new BorderLayout(FLOW_GAP, FLOW_GAP));
        root.setBorder(BorderFactory.createEmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));

        JPanel forms = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createBaseConstraints();

        JButton addRoomButton = new JButton("Add Room");
        addRoomButton.addActionListener(e -> executeCommand(
                "addRoom " + addRoomTypeCombo.getSelectedItem()
                        + " " + addRoomCapField.getText().trim()
                        + " " + addRoomFeaturesField.getText().trim()));

        addFormRow(forms, gbc, ROW_INDEX_ZERO,
                new JLabel("Type:"), addRoomTypeCombo,
                new JLabel("Capacity:"), addRoomCapField,
                new JLabel("Features:"), addRoomFeaturesField,
                addRoomButton);

        JButton modifyRoomButton = new JButton("Modify Room");
        modifyRoomButton.addActionListener(e -> executeCommand(
                "modifyRoom " + modRoomIdField.getText().trim()
                        + " " + modRoomFieldCombo.getSelectedItem()
                        + " " + modRoomValField.getText().trim()));

        addFormRow(forms, gbc, ROW_INDEX_ONE,
                new JLabel("Room ID:"), modRoomIdField,
                new JLabel("Field:"), modRoomFieldCombo,
                new JLabel("New Value:"), modRoomValField,
                modifyRoomButton);

        JPanel actionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_GAP, FLOW_GAP));
        JButton listRoomButton = new JButton("List Room");
        listRoomButton.addActionListener(e -> executeCommand("listRoom " + delListRoomIdField.getText().trim()));
        JButton deleteRoomButton = new JButton("Delete Room");
        deleteRoomButton.addActionListener(e -> executeCommand("deleteRoom " + delListRoomIdField.getText().trim()));
        JButton listAllRoomsButton = new JButton("List All Rooms");
        listAllRoomsButton.addActionListener(e -> executeCommand("listAllRooms"));
        actionsRow.add(new JLabel("Target Room ID:"));
        actionsRow.add(delListRoomIdField);
        actionsRow.add(listRoomButton);
        actionsRow.add(deleteRoomButton);
        actionsRow.add(listAllRoomsButton);

        JPanel top = new JPanel(new BorderLayout(FLOW_GAP, FLOW_GAP));
        top.add(forms, BorderLayout.CENTER);
        top.add(actionsRow, BorderLayout.SOUTH);

        JTable table = new JTable(roomsTableModel);
        root.add(top, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildBookingsTab() {
        JPanel root = new JPanel(new BorderLayout(FLOW_GAP, FLOW_GAP));
        root.setBorder(BorderFactory.createEmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));

        JPanel forms = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = createBaseConstraints();

        JButton bookButton = new JButton("Book");
        bookButton.addActionListener(e -> executeCommand(
                "book " + bookRoomIdField.getText().trim()
                        + " " + bookDateField.getText().trim()
                        + " " + bookStartField.getText().trim()
                        + " " + bookEndField.getText().trim()
                        + " " + bookPurposeField.getText().trim()));

        addFormRow(forms, gbc, ROW_INDEX_ZERO,
                new JLabel("Room:"), bookRoomIdField,
                new JLabel("Date:"), bookDateField,
                new JLabel("Start:"), bookStartField,
                bookButton);

        JButton modifyBookingButton = new JButton("Modify Booking");
        modifyBookingButton.addActionListener(e -> executeCommand(
                "modifyBooking " + modBookingIdField.getText().trim()
                        + " " + bookRoomIdField.getText().trim()
                        + " " + bookDateField.getText().trim()
                        + " " + bookStartField.getText().trim()
                        + " " + bookEndField.getText().trim()
                        + " " + bookPurposeField.getText().trim()));

        addFormRow(forms, gbc, ROW_INDEX_ONE,
                new JLabel("End:"), bookEndField,
                new JLabel("Purpose:"), bookPurposeField,
                new JLabel("Booking ID:"), modBookingIdField,
                modifyBookingButton);

        JButton searchButton = new JButton("Search Available");
        searchButton.addActionListener(e -> executeCommand(
                "search " + searchDateField.getText().trim()
                        + " " + searchStartField.getText().trim()
                        + " " + searchEndField.getText().trim()
                        + " " + searchCapField.getText().trim()
                        + " " + searchFeaturesField.getText().trim()));

        addFormRow(forms, gbc, ROW_INDEX_TWO,
                new JLabel("Search Date:"), searchDateField,
                new JLabel("Start:"), searchStartField,
                new JLabel("End:"), searchEndField,
                searchButton);

        JPanel extraRow = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_GAP, FLOW_GAP));
        JButton deleteBookingButton = new JButton("Delete Booking");
        deleteBookingButton.addActionListener(e -> executeCommand(
                "deleteBooking " + modBookingIdField.getText().trim()));
        JButton listBookingsButton = new JButton("List My Bookings");
        listBookingsButton.addActionListener(e -> executeCommand("listBookings"));
        extraRow.add(new JLabel("Min Cap:"));
        extraRow.add(searchCapField);
        extraRow.add(new JLabel("Req Features:"));
        extraRow.add(searchFeaturesField);
        extraRow.add(deleteBookingButton);
        extraRow.add(listBookingsButton);

        JPanel top = new JPanel(new BorderLayout(FLOW_GAP, FLOW_GAP));
        top.add(forms, BorderLayout.CENTER);
        top.add(extraRow, BorderLayout.SOUTH);

        JTable table = new JTable(bookingsTableModel);
        root.add(top, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildScheduleTab() {
        JPanel root = new JPanel(new BorderLayout(FLOW_GAP, FLOW_GAP));
        root.setBorder(BorderFactory.createEmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_GAP, FLOW_GAP));
        JButton viewButton = new JButton("View Weekly Schedule");
        viewButton.addActionListener(e -> loadSchedule());
        controls.add(new JLabel("Room ID:"));
        controls.add(scheduleRoomIdField);
        controls.add(new JLabel("Week Start (yyyy-MM-dd):"));
        controls.add(scheduleWeekStartField);
        controls.add(viewButton);

        JTable table = new JTable(scheduleTableModel);
        root.add(controls, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        return root;
    }

    private JPanel buildUsersTab() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));
        GridBagConstraints gbc = createBaseConstraints();

        JButton addUserButton = new JButton("Add Regular User");
        addUserButton.addActionListener(e -> executeCommand("addUser " + addUserNameField.getText().trim()));
        addFormRow(root, gbc, ROW_INDEX_ZERO,
                new JLabel("Name:"), addUserNameField,
                new JLabel(), new JLabel(),
                new JLabel(), new JLabel(),
                addUserButton);

        JButton removeUserButton = new JButton("Remove Regular User");
        removeUserButton.addActionListener(e -> executeCommand("removeUser " + removeUserIdField.getText().trim()));
        addFormRow(root, gbc, ROW_INDEX_ONE,
                new JLabel("Target User ID:"), removeUserIdField,
                new JLabel(), new JLabel(),
                new JLabel(), new JLabel(),
                removeUserButton);

        JButton changeRoleButton = new JButton("Change Role");
        changeRoleButton.addActionListener(e -> executeCommand(
                "changeRole " + roleUserIdField.getText().trim() + " " + roleCombo.getSelectedItem()));
        addFormRow(root, gbc, ROW_INDEX_TWO,
                new JLabel("Target User ID:"), roleUserIdField,
                new JLabel("New Role:"), roleCombo,
                new JLabel(), new JLabel(),
                changeRoleButton);

        return root;
    }

    private JPanel buildConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout(FLOW_GAP, FLOW_GAP));
        panel.setBorder(BorderFactory.createTitledBorder("Command Output & Direct CLI"));

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, FLOW_GAP, FLOW_GAP));
        JButton runButton = new JButton("Execute");
        runButton.addActionListener(e -> runRawCommand());
        rawCommandField.addActionListener(e -> runRawCommand());
        inputRow.add(new JLabel("Command:"));
        inputRow.add(rawCommandField);
        inputRow.add(runButton);

        panel.add(new JScrollPane(consoleArea), BorderLayout.CENTER);
        panel.add(inputRow, BorderLayout.SOUTH);
        return panel;
    }

    private void runRawCommand() {
        String text = rawCommandField.getText();
        rawCommandField.setText("");
        executeCommand(text);
    }

    private void executeCommand(String commandLine) {
        String output = controller.execute(commandLine);
        if (output != null && !output.isEmpty()) {
            appendOutput("> " + commandLine.trim());
            appendOutput(output);
        }
        if (!controller.isRunning()) {
            controller.shutdown();
            frame.dispose();
            return;
        }
        refreshState();
    }

    private void loadSchedule() {
        String roomId = scheduleRoomIdField.getText().trim();
        String weekStart = scheduleWeekStartField.getText().trim();
        String command = "schedule " + roomId + " " + weekStart;
        String output = controller.execute(command);
        if (output != null && !output.isEmpty()) {
            appendOutput("> " + command);
            appendOutput(output);
        }
        scheduleTableModel.setRowCount(ROW_INDEX_ZERO);
        if (output != null && !output.startsWith(ERROR_PREFIX)) {
            try {
                List<ScheduleEntry> entries = system.getWeeklySchedule(roomId, weekStart);
                for (ScheduleEntry entry : entries) {
                    Booking b = entry.getBooking();
                    scheduleTableModel.addRow(new Object[]{
                            b.getSlot().toString(),
                            b.getId(),
                            entry.getOwnerDisplay(),
                            b.getPurpose()
                    });
                }
            } catch (CrobsException e) {
                appendOutput("Unable to populate schedule table: " + e.getMessage());
            }
        }
    }

    private void refreshState() {
        roomsTableModel.setRowCount(ROW_INDEX_ZERO);
        bookingsTableModel.setRowCount(ROW_INDEX_ZERO);
        try {
            User current = system.getCurrentUser();
            sessionLabel.setText("Logged in: " + OutputFormatter.formatUser(current));
            for (Room room : system.getAllRooms()) {
                roomsTableModel.addRow(new Object[]{
                        room.getId(),
                        room.getType().getKeyword(),
                        room.getCapacity(),
                        RoomFeature.format(room.getFeatures())
                });
            }
            for (Booking booking : system.getMyBookings()) {
                bookingsTableModel.addRow(new Object[]{
                        booking.getId(),
                        booking.getRoomId(),
                        booking.getSlot().toString(),
                        booking.getStatus().name(),
                        booking.getPurpose()
                });
            }
        } catch (CrobsException e) {
            sessionLabel.setText("Not logged in (Superadmin: " + system.getSuperAdminId() + ")");
        }
    }

    private void appendOutput(String text) {
        consoleArea.append(text + "\n");
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }

    private static String[] roomTypeKeywords() {
        RoomType[] types = RoomType.values();
        String[] result = new String[types.length];
        for (int i = ROW_INDEX_ZERO; i < types.length; i++) {
            result[i] = types[i].getKeyword();
        }
        return result;
    }

    private static String[] roomFieldKeywords() {
        RoomField[] fields = RoomField.values();
        String[] result = new String[fields.length];
        for (int i = ROW_INDEX_ZERO; i < fields.length; i++) {
            result[i] = fields[i].getKeyword();
        }
        return result;
    }

    private static DefaultTableModel createReadOnlyTableModel(String[] columns) {
        return new ReadOnlyTableModel(columns);
    }

    private static GridBagConstraints createBaseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(GRID_PAD, GRID_PAD, GRID_PAD, GRID_PAD);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private static void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            Component c0,
            Component c1,
            Component c2,
            Component c3,
            Component c4,
            Component c5,
            Component c6) {
        gbc.gridy = row;
        gbc.gridx = COL_INDEX_ZERO;
        panel.add(c0, gbc);
        gbc.gridx = COL_INDEX_ONE;
        panel.add(c1, gbc);
        gbc.gridx = COL_INDEX_TWO;
        panel.add(c2, gbc);
        gbc.gridx = COL_INDEX_THREE;
        panel.add(c3, gbc);
        gbc.gridx = COL_INDEX_FOUR;
        panel.add(c4, gbc);
        gbc.gridx = COL_INDEX_FIVE;
        panel.add(c5, gbc);
        gbc.gridx = COL_INDEX_SIX;
        panel.add(c6, gbc);
    }

    /**
     * Named table model disabling direct cell editing in Swing tables.
     */
    private static final class ReadOnlyTableModel extends DefaultTableModel {
        /**
         * Creates a read-only table model with the specified column headers.
         *
         * @param columnNames column header names
         */
        private ReadOnlyTableModel(Object[] columnNames) {
            super(columnNames, ROW_INDEX_ZERO);
        }

        /**
         * Disables inline table cell editing.
         *
         * @param row    the row index
         * @param column the column index
         * @return always {@code false}
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    /**
     * Named window listener ensuring {@link CommandController#shutdown()} runs on window close.
     */
    private static final class FrameCloseHandler extends WindowAdapter {
        private final CommandController controller;

        /**
         * Creates a window close listener bound to {@code controller}.
         *
         * @param controller the command controller to shut down on close
         */
        private FrameCloseHandler(CommandController controller) {
            this.controller = controller;
        }

        /**
         * Shuts down logging resources when the main window closes.
         *
         * @param e the window closing event
         */
        @Override
        public void windowClosing(WindowEvent e) {
            controller.shutdown();
        }
    }
}