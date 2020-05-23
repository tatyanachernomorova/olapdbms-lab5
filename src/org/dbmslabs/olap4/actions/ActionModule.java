package org.dbmslabs.olap4.actions;

import org.dbmslabs.olap4.RestController;
import org.dbmslabs.olap4.RestHandler;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ActionModule {
    private final RestController restController;
    private final ArrayList<Supplier<RestHandler>> actionPlugins;

    public ActionModule(ArrayList<Supplier<RestHandler>> actionPlugins) {
        this.restController = new RestController();//new RestController(headers, restWrapper);
        this.actionPlugins = actionPlugins;

        initRestHandlers();
    }

    public void initRestHandlers() {
        for (Supplier<RestHandler> ap : actionPlugins) {
            restController.registerHandler(ap.get());
        }
    }

    public RestController getRestController() {
        return restController;
    }
}
