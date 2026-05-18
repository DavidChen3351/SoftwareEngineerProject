package com.bupt.ta.listener;

import com.bupt.ta.util.DataStore;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Align job filledSlots with ACCEPTED applications on startup. */
@WebListener
public class DataIntegrityListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        var context = event.getServletContext();
        var dataDir = DataStore.getDataDirectory(context);
        context.log("[TA] Persistent data directory: " + dataDir.toAbsolutePath());
        DataStore.recalculateFilledSlots(context);
    }
}
