package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.application.IConstants;
import br.uff.ic.dyevc.monitor.RepositoryMonitor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.LoggerFactory;

public class StdOutErrWindow extends JFrame {

    private static final long serialVersionUID = 5080471850955743986L;
    private JTextArea jTextAreaOut;
    private JPopupMenu jPopupTextAreaOut;

    public StdOutErrWindow() {
        initComponents();
        System.setOut(new PrintStream(new JTextAreaOutputStream(jTextAreaOut)));
        System.setErr(new PrintStream(new JTextAreaOutputStream(jTextAreaOut)));
    }

    private void initComponents() {
        this.setTitle("Console Messages");
        setSize(700, 600);
        setMinimumSize(new Dimension(700, 600));
        setVisible(false);
        jTextAreaOut = new JTextArea(20, 50);
        jTextAreaOut.setFont(new Font("Monospaced", Font.PLAIN, 12));
        DefaultCaret caretOut = (DefaultCaret) jTextAreaOut.getCaret();
        caretOut.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane outPane = new JScrollPane(jTextAreaOut);
        
        //moves vertical scroll to the end of the messages
        outPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
            }
        });
        
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
        jPopupTextAreaOut.add(mntClear);
        
        JMenuItem mntLogTrace = new javax.swing.JMenuItem();
        mntLogTrace.setText("Log Level -> Trace");
        mntLogTrace.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.TRACE);
            }
        });
        jPopupTextAreaOut.add(mntLogTrace);
        
        JMenuItem mntLogDebug = new javax.swing.JMenuItem();
        mntLogDebug.setText("Log Level -> Debug");
        mntLogDebug.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.DEBUG);
            }
        });
        jPopupTextAreaOut.add(mntLogDebug);
        
        JMenuItem mntLogInfo = new javax.swing.JMenuItem();
        mntLogInfo.setText("Log Level -> Info");
        mntLogInfo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.INFO);
            }
        });
        jPopupTextAreaOut.add(mntLogInfo);
        
        JMenuItem mntLogWarn = new javax.swing.JMenuItem();
        mntLogWarn.setText("Log Level -> Warning");
        mntLogWarn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.WARN);
            }
        });
        jPopupTextAreaOut.add(mntLogWarn);
        
        JMenuItem mntLogError = new javax.swing.JMenuItem();
        mntLogError.setText("Log Level -> Error");
        mntLogError.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LogManager.getLogger(IConstants.APPLICATION_PACKAGE).setLevel(Level.ERROR);
            }
        });
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

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="JTextAreaOutputStream">
    public class JTextAreaOutputStream extends OutputStream {

        JTextArea ta;

        public JTextAreaOutputStream(JTextArea t) {
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
