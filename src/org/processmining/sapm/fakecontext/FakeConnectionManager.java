package org.processmining.sapm.fakecontext;

import java.util.Collection;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.impl.ConnectionManagerImpl;
import org.processmining.framework.plugin.PluginContext;

public class FakeConnectionManager extends ConnectionManagerImpl {


	public FakeConnectionManager() {
		super(null);
	}

	public <T extends Connection> T getFirstConnection(Class<T> connectionType, PluginContext context,
			Object... objects) throws ConnectionCannotBeObtained {
		return getConnections(true, connectionType, context, objects).iterator().next();
	}

	public <T extends Connection> Collection<T> getConnections(Class<T> connectionType, PluginContext context,
			Object... objects) throws ConnectionCannotBeObtained {
		return getConnections(false, connectionType, context, objects);
	}

	@SuppressWarnings("unchecked")
	private <T extends Connection> Collection<T> getConnections(boolean stopAtFirst, Class<T> connectionType,
			PluginContext context, Object... objects) throws ConnectionCannotBeObtained {
		throw new ConnectionCannotBeObtained("No plugin available to create connection", connectionType, objects);
	}

}
