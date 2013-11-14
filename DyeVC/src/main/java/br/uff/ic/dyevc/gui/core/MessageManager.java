package br.uff.ic.dyevc.gui.core;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.utils.DateUtil;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Font;

/**
 * Presents important messages sent by application.
 *
 * @author Cristiano
 */
public class MessageManager {
    /**
     * Singleton that manages messages sent by application.
     */
    private final LogTextArea     messages;
    private static MessageManager manager;

    /**
     * Constructs ...
     *
     * @param messages
     */
    private MessageManager(LogTextArea messages) {
        this.messages = messages;
        messages.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }

    public synchronized static MessageManager initialize(LogTextArea messages) {
        if (manager == null) {
            manager = new MessageManager(messages);
        }

        return manager;
    }

    public static MessageManager getInstance() {
        return manager;
    }

    /**
     * Displays a new message at the end of the message area.
     * @param msg the message to be displayed.
     */
    public void addMessage(String msg) {

        messages.append(
            new StringBuilder(DateUtil.getFormattedCurrentDate()).append(" ").append(msg).append("\n").toString());
    }

    /**
     * Clears the message area.
     */
    public void clearMessages() {
        messages.setText(null);
    }
}
