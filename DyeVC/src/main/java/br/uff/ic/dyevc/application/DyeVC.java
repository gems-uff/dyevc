package br.uff.ic.dyevc.application;

import br.uff.ic.dyevc.gui.main.MainWindow;
import javax.swing.UIManager;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Cristiano
 */
public class DyeVC {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the System look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LoggerFactory.getLogger(DyeVC.class).error("Error starting DyeVC", ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow();
            }
        });
    }
}
