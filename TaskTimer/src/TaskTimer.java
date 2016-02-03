
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class TaskTimer implements Runnable {
    // update the log file every updatePeriod minutes
    private static final int updatePeriod = 10;
    // instance variables
    private String[] stateImages, stateNames;
    private Clock[] stateClocks;
    private Clock lastUpdate, elapsed, now;
    private int currentState, numStates;
    private TrayIcon trayIcon;
    private PopupMenu popup;
    private File logFile;
    private Thread updaterThread;
    private Font normalFont, boldFont;
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            (new TaskTimer()).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Task timer",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void start() {
        updaterThread = new Thread(this);
        updaterThread.start();
    }

    public void run() {
        boolean exit = false;
        do {
            try { Thread.sleep(updatePeriod * 60000); }
            catch (InterruptedException e) { exit = true; }
            try { updateClocks(); }
            catch (Exception e) {
                trayIcon.displayMessage("Task timer",
                        "The log file could not be updated",
                        TrayIcon.MessageType.ERROR);
            }
        } while (!exit);
    }

    public TaskTimer() {
    	// check system tray support
    	SystemTray tray = null;
    	trayIcon = null;
        updaterThread = null;
    	try {
            if (!SystemTray.isSupported()) { throw new Exception(); }
            tray = SystemTray.getSystemTray();
    	} catch (Exception e) {
            throw new IllegalArgumentException("The system tray is not supported");
    	}
        // create list of states
        File statesDir = new File(".");
        FilenameFilter imageFilter = new FilenameFilter() {
        	public boolean accept(File dir, String name) {
                    return ( name.endsWith(".gif") ||
                            name.endsWith(".jpg") ||
                            name.endsWith(".jpeg") ||
                            name.endsWith(".png") );
        	}
        };
        stateImages = statesDir.list(imageFilter);
        numStates = stateImages.length;
        if ( numStates == 0 ) {
            throw new IllegalArgumentException("The current directory does " +
                    "not contain any supported image file (gif, jpeg or png)");
        }
        Arrays.sort(stateImages);
        stateNames = new String[numStates];
        for (int i = 0; i < numStates; i++) { stateNames[i] = getStateName(i); }
        // create listeners
        ActionListener menuListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MenuItem source = (MenuItem) e.getSource();
                if (source.getLabel().equals("Reset")) {
                    if (JOptionPane.showConfirmDialog(null, "Reset?",
                    "Task timer", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
                        resetClocks();
                    }
                } else if (source.getLabel().equals("Reset and exit")) {
                    if (JOptionPane.showConfirmDialog(null, "Reset and exit?",
                    "Task timer", JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION) {
                        resetClocks();   updaterThread.interrupt();   System.exit(0);
                    }
                } else if (source.getLabel().equals("Exit")) {
                    updateClocks();   updaterThread.interrupt();   System.exit(0);
                } else {   // change state
                    int token = source.getLabel().indexOf("(") - 1;
                    String label = source.getLabel().substring(0,token);
                    int i = 0;
                    while ( (i < numStates) && !stateNames[i].equals(label) ) { i++; }
                    if (i == numStates) {
                        trayIcon.displayMessage("Task timer",
                                "Invalid menu item", TrayIcon.MessageType.ERROR);
                        return;
                    }
                    updateClocks();
                    popup.getItem(currentState).setFont(normalFont);
                    currentState = i;
                    popup.getItem(currentState).setFont(boldFont);
                    Image image = Toolkit.getDefaultToolkit().getImage(
                            stateImages[currentState]);
                    trayIcon.setImage(image);
                    trayIcon.setToolTip("Task timer (" + stateNames[currentState] + ")");
                }
            }
        };
        popup = new PopupMenu();
        MouseListener mouseListener = new MouseListener() {
            public void mousePressed(MouseEvent e) { updatePopupMenu(); }
            public void mouseReleased(MouseEvent e) {}
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // show menu with left click, too
                    try {
                        Robot r = new Robot();
                        r.mousePress(InputEvent.BUTTON3_MASK);
                        r.mouseRelease(InputEvent.BUTTON3_MASK);
                    } catch (AWTException ex) {}
                }
            }
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        };
        // create popup menu
        MenuItem item;
        for (int i = 0; i < stateNames.length; i++) {
            item = new MenuItem( stateNames[i] );
            item.addActionListener(menuListener);
            popup.add(item);
        }
        popup.addSeparator();
        item = new MenuItem("Reset and exit");
        item.addActionListener(menuListener);
        popup.add(item);
        item = new MenuItem("Reset");
        item.addActionListener(menuListener);
        popup.add(item);
        item = new MenuItem("Exit");
        item.addActionListener(menuListener);
        popup.add(item);
        normalFont = new Font("Tahoma", Font.PLAIN, 12);
        boldFont = new Font("Tahoma", Font.BOLD, 12);
        // display icon
        currentState = 0;   popup.getItem(currentState).setFont(boldFont);
        Image image = Toolkit.getDefaultToolkit().getImage(
                stateImages[currentState]);
        trayIcon = new TrayIcon(image, "Task timer", popup);
        trayIcon.setToolTip("Task timer (" + stateNames[currentState] + ")");
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(mouseListener);
        try { tray.add(trayIcon); }
        catch (AWTException e) {
            throw new IllegalArgumentException("The tray icon could not be created");
        }
        initializeClocks();
    }

    private void initializeClocks() {
        logFile = new File("taskTimerLog.txt");
        stateClocks = new Clock[numStates];
        elapsed = new Clock();   now = new Clock();
        int i, token;
        BufferedReader in = null;
        // try to load the logFile into the clocks
        try {
            if (!logFile.exists()) { throw new Exception(); }
            in = new BufferedReader(new FileReader(logFile));
            String line = in.readLine();   i = 0;
            while ( (line != null) && (i < numStates) ) {
                token = line.indexOf(":");
                if (token < 0) { throw new Exception(); }
                if ( !stateNames[i].equals(line.substring(0,token)) ) {
                    throw new Exception();
                }
                stateClocks[i] = new Clock(line.substring(token+2));
                line = in.readLine();   i++;
            }
            if (i < numStates) { throw new Exception(); }
            lastUpdate = new Clock();   lastUpdate.getNow();
        } catch (Exception e) {   // the loading failed
            for (i = 0; i < numStates; i++) { stateClocks[i] = new Clock(); }
            lastUpdate = new Clock();   lastUpdate.getNow();
            updateClocks();
            trayIcon.displayMessage("Task timer",
                    "The clocks were reinitialized", TrayIcon.MessageType.INFO);
        }
        if (in != null) { try { in.close(); } catch (Exception e) {} }
    }

    private synchronized void updateClocks() {
        // update clocks
        now.getNow();
        elapsed.copy(now);   elapsed.substract(lastUpdate);
        stateClocks[currentState].add(elapsed);
        lastUpdate.copy(now);
        // save clocks
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(logFile));
            for (int i = 0; i < numStates; i++) {
                out.write( stateNames[i] + ": " + stateClocks[i] );
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("The log file could not be updated");
        }
    }

    private synchronized void resetClocks() {
        for (int i = 0; i < numStates; i++) { stateClocks[i].reset(); }
        lastUpdate.getNow();   updateClocks();
    }

    private String getStateName(int i) {
    	String state = stateImages[i];
    	int end = state.lastIndexOf(".");
    	int begin = 0;
    	while ( Character.isDigit(state.charAt(begin)) ) { begin++; }
    	return state.substring(begin,end);
    }

    private void updatePopupMenu() {
        MenuItem item;
        updateClocks();
        for (int i = 0; i < numStates; i++) {
            item = popup.getItem(i);
            item.setLabel( stateNames[i] + " (" + stateClocks[i] + ")" );
        }
    }
}
