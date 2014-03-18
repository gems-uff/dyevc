package br.uff.ic.dyevc.gui.core;

import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

/**
 * Extends JTextArea to automatically update caret when inserting text.
 *
 * @author Cristiano
 */
@SuppressWarnings("serial")
public class LogTextArea extends JTextArea {

    public LogTextArea() {
        super();
    }

    public LogTextArea(Document doc) {
        super(doc);
    }

    public LogTextArea(String text) {
        super(text);
    }

    public LogTextArea(int rows, int columns) {
        super(rows, columns);
    }

    public LogTextArea(String text, int rows, int columns) {
        super(text, rows, columns);
    }

    public LogTextArea(Document doc, String text, int rows, int columns) {
        super(doc, text, rows, columns);
    }
    
    @Override
    public void setText(String t) {
        super.setText(t);
            DefaultCaret caretOut = (DefaultCaret) getCaret();
            if (caretOut.getUpdatePolicy() == DefaultCaret.ALWAYS_UPDATE) {
                setCaretPosition(getDocument().getLength());
            }
    }
}
