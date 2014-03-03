package br.uff.ic.dyevc.utils;

//~--- non-JDK imports --------------------------------------------------------

import br.uff.ic.dyevc.beans.ApplicationSettingsBean;
import br.uff.ic.dyevc.gui.graph.CommitHistoryWindow;

import org.apache.commons.lang.time.StopWatch;

import org.slf4j.LoggerFactory;

/**
 * Utility class to log elapsed times based on a stop watch.
 * @author Cristiano
 */
public class StopWatchLogger {
    StopWatch               watch;
    ApplicationSettingsBean settings;
    Class                   clazz;

    /**
     * Constructs an instance of StopWatchLogger.
     *
     * @param clazz The class for which the logger will check the log level.
     */
    public StopWatchLogger(Class clazz) {
        watch      = new StopWatch();
        settings   = PreferencesManager.getInstance().loadPreferences();
        this.clazz = clazz;
    }

    public void start() {
        if (settings.isPerformanceMode()) {
            watch.reset();
            watch.start();
        }
    }

    public void stopAndLog(String msg) {
        if (settings.isPerformanceMode()) {
            watch.stop();
            LoggerFactory.getLogger(clazz).info(msg + " Elapsed time: " + watch.toString());
        }
    }

    public void splitAndLog(String msg) {
        if (settings.isPerformanceMode()) {
            watch.split();
            LoggerFactory.getLogger(clazz).info(msg + " Split time: " + watch.toSplitString());
        }
    }

    public void resume() {
        if (settings.isPerformanceMode()) {
            watch.resume();
        }
    }

    public void suspend() {
        if (settings.isPerformanceMode()) {
            watch.suspend();
        }
    }
}
