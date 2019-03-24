package org.processmining.sapm.fakecontext;

import java.util.Iterator;

import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.impl.PluginManagerImpl;

public class FakePluginContext extends UIPluginContext {
	
	//private final ConnectionManager connectionManager = new ConnectionManagerImpl(PluginManagerImpl.getInstance());;

    public FakePluginContext() {
        this(MAIN_PLUGINCONTEXT, "Fake Plugin Context");
    }

    public FakePluginContext(UIPluginContext context, String label) {
        super(context, label);
    }

    public FakePluginContext(PluginContext context) {
        this(MAIN_PLUGINCONTEXT, "Fake Plugin Context");
        for (Iterator<ConnectionID> iterator = context.getConnectionManager().getConnectionIDs().iterator(); iterator.hasNext(); ) {
            ConnectionID cid = iterator.next();
            try {
                org.processmining.framework.connections.Connection connection = context.getConnectionManager().getConnection(cid);
                addConnection(connection);
            } catch (ConnectionCannotBeObtained connectioncannotbeobtained) {
            }
        }

    }

    public Progress getProgress() {
        return new FakeProgress();
    }

    public ProMFuture getFutureResult(int i) {
        return new ProMFuture(String.class, "Fake Future") {

            @Override
            protected Object doInBackground() throws Exception {
                return null;
            }
        };
    }

    public void setFuture(PluginExecutionResult pluginexecutionresult) {
    }
    
    //Should not use a separate connection manager because there's one in the UIContext
//    @Override
//	public ConnectionManager getConnectionManager() {
//		return connectionManager;
//	}
    
    //-----------------------------------------------------
    // Initialize the singleton UIContext
    // This UIContext contains a global PluginManager, ConnectionManager and 
    // ProvidedObjectManager which are referenced by all plugins
    //-----------------------------------------------------
    private static UIPluginContext MAIN_PLUGINCONTEXT;
    static {
    	// Need to initialize the PluginManagerImpl here as we don't run Boot.boot through the UI class
    	// The ConnectionManager in the UIContext will reference the PluginManager initialized here
    	PluginManagerImpl.initialize(UIPluginContext.class);
        UIContext MAIN_CONTEXT = new UIContext();
        MAIN_PLUGINCONTEXT = MAIN_CONTEXT.getMainPluginContext().createChildContext("");
    }
}
