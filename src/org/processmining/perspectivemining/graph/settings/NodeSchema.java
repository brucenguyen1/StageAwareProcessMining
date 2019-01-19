package org.processmining.perspectivemining.graph.settings;

import java.util.ArrayList;
import java.util.List;

import org.processmining.perspectivemining.log.attribute.AttributeRow;

/**
 * Node Type represents classes of nodes, each is a set of objects
 * Thus, a Node Type is represented by a list of object types
 * @author Bruce
 *
 */
public class NodeSchema {
	private String name = "";
	private List<AttributeRow> attributes = null;
	
	public NodeSchema(String name) {
		this.name = name;
		this.attributes = new ArrayList<>();
	}
	
	public String getName() {
		return name;
	}
	
	public List<AttributeRow> getAttributes() {
		return attributes;
	}
	
	public List<String> getAttributeNames() {
		List<String> columnNames = new ArrayList<>();
		for (AttributeRow att : this.attributes) {
			columnNames.add(att.getName());
		}
		return columnNames;
	}
	
	public String getNodeName() {
		return this.getAttributeNames().toString();
	}
	
}
