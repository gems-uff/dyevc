package br.uff.ic.dyevc.gui.core;

import br.uff.ic.dyevc.gui.core.LogTextArea;
import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.utils.LimitLinesDocumentListener;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

/**
 * This class receives stdout and stderr messages and displays them. After being
 * initialized, all console messages are redirected to this window.<br>
 * The class also allows the user to dynamically set the desired log level to be
 * displayed.
 *
 * @author Cristiano
 */
public class StdOutErrWindow extends JFrame {

    private static final long serialVersionUID = 5080471850955743986L;
    private LogTextArea jTextAreaOut;
    private JPopupMenu jPopupTextAreaOut;
    private JMenuItem mntLogSize;
    private boolean autoScroll = true;
    LimitLinesDocumentListener documentListener;

    public StdOutErrWindow() {
        initComponents();
        System.setOut(new PrintStream(new JTextAreaOutputStream(jTextAreaOut)));
        System.setErr(new PrintStream(new JTextAreaOutputStream(jTextAreaOut)));
    }

    private void initComponents() {
        this.setTitle("Console Messages");
        setSize(800, 600);
        setMinimumSize(new Dimension(800, 600));
        setVisible(false);
        jTextAreaOut = new LogTextArea(20, 50);
        jTextAreaOut.setFont(new Font("Monospaced", Font.PLAIN, 12));
        documentListener = new LimitLinesDocumentListener();
        jTextAreaOut.getDocument().addDocumentListener(documentListener);

        JScrollPane outPane = new JScrollPane(jTextAreaOut);
        setAutoScrolling();

        getContentPane().add(outPane);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        buildTextAreaPopup();

        jTextAreaOut.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextAreaOutMouseClicked(evt);
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="textArea menu">  
    private void buildTextAreaPopup() {

        jPopupTextAreaOut = new JPopupMenu();
        JMenuItem mntClear = new javax.swing.JMenuItem();
        mntClear.setText("Clear Messages");
        mntClear.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextAreaOut.setText(null);
            }
        });

        JCheckBoxMenuItem mntScrolling = new JCheckBoxMenuItem();
        mntScrolling.setText("Auto-scroll window");
        mntScrolling.setSelected(autoScroll);
        mntScrolling.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JCheckBoxMenuItem source = (JCheckBoxMenuItem) evt.getSource();
                autoScroll = source.isSelected();
                setAutoScrolling();
            }
        });

        mntLogSize = new javax.swing.JMenuItem();
        mntLogSize.setText("Limit Log Buffer (" + documentListener.getLimitLines() + ")");
        mntLogSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setLogLimit();
            }
        });

        JMenuItem mntLogTrace = new javax.swing.JMenuItem();
        mntLogTrace.setText("Log Level -> Trace");
        mntLogTrace.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.TRACE);
            }
        });

        JMenuItem mntLogDebug = new javax.swing.JMenuItem();
        mntLogDebug.setText("Log Level -> Debug");
        mntLogDebug.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.DEBUG);
            }
        });

        JMenuItem mntLogInfo = new javax.swing.JMenuItem();
        mntLogInfo.setText("Log Level -> Info");
        mntLogInfo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.INFO);
            }
        });

        JMenuItem mntLogWarn = new javax.swing.JMenuItem();
        mntLogWarn.setText("Log Level -> Warning");
        mntLogWarn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.WARN);
            }
        });

        JMenuItem mntLogError = new javax.swing.JMenuItem();
        mntLogError.setText("Log Level -> Error");
        mntLogError.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.ERROR);
            }
        });

        jPopupTextAreaOut.add(mntScrolling);
        jPopupTextAreaOut.add(mntLogSize);
        jPopupTextAreaOut.add(mntClear);
        jPopupTextAreaOut.addSeparator();
        jPopupTextAreaOut.add(mntLogTrace);
        jPopupTextAreaOut.add(mntLogDebug);
        jPopupTextAreaOut.add(mntLogInfo);
        jPopupTextAreaOut.add(mntLogWarn);
        jPopupTextAreaOut.add(mntLogError);
    }

    /**
     * Shows up a popup menu when the user clicks with the right button
     *
     * @param evt
     */
    private void jTextAreaOutMouseClicked(MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON3) {
            jPopupTextAreaOut.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    private void setAutoScrolling() {
        if (autoScroll) {
            jTextAreaOut.setCaretPosition(jTextAreaOut.getDocument().getLength());
            DefaultCaret caretOut = (DefaultCaret) jTextAreaOut.getCaret();
            caretOut.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        } else {
            DefaultCaret caretOut = (DefaultCaret) jTextAreaOut.getCaret();
            caretOut.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    private void setLogLimit() {
        String newLimit = null;
        int newLimitInt = documentListener.getLimitLines();
        while (newLimit == null || newLimit.equals("")) {
            newLimit = JOptionPane.showInputDialog("Enter how many lines to keep in log.", newLimitInt);
            try {
                newLimitInt = Integer.parseInt(newLimit);
            } catch (NumberFormatException e) {
                newLimit = null;
            }
        }
        documentListener.setLimitLines(newLimitInt);
        mntLogSize.setText("Limit Log Buffer (" + documentListener.getLimitLines() + ")");
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="JTextAreaOutputStream">
    public class JTextAreaOutputStream extends OutputStream {

        LogTextArea ta;

        public JTextAreaOutputStream(LogTextArea t) {
            super();
            ta = t;
        }

        @Override
        public void write(int i) {
            ta.append(Character.toString((char) i));
        }

        public void write(char[] buf, int off, int len) {
            String s = new String(buf, off, len);
            ta.append(s);
        }
    }
    // </editor-fold>
}
