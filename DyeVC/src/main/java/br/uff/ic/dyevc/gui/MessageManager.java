package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.utils.DateUtil;
import java.awt.Font;
import javax.swing.JTextArea;

/**
 *
 * @author Cristiano
 */
public class MessageManager {

    private JTextArea messages;
    private static MessageManager manager;

    private MessageManager(JTextArea messages) {
        this.messages = messages;
        messages.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    public synchronized static MessageManager initialize(JTextArea messages) {
        if (manager == null) {
            manager = new MessageManager(messages);
        }
        return manager;
    }

    public static MessageManager getInstance() {
        return manager;
    }

    public void addMessage(String msg) {
        
        messages.append(new StringBuilder(DateUtil.getFormattedCurrentDate())
                .append(" ").append(msg).append("\n").toString());
    }

    public void clearMessages() {
        messages.setText(null);
    }
}
