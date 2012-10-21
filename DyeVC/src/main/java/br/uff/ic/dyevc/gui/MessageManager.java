/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.ic.dyevc.gui;

import br.uff.ic.dyevc.exception.DyeVCException;
import javax.swing.JTextArea;

/**
 *
 * @author Cristiano
 */
public class MessageManager {
    private JTextArea messages;
    private static MessageManager manager;
    
    private MessageManager (JTextArea messages) {
        this.messages = messages;
    }
    
    public synchronized static MessageManager initialize (JTextArea messages) {
        if (manager == null) {
            manager = new MessageManager(messages);
        }
        return manager;
    }
    
    public static MessageManager getInstance() {
        return manager;
    }
    
    public void addMessage(String msg) {
        messages.append(msg);
    }
    
    public void clearMessages() {
        messages.setText(null);
    }
}
