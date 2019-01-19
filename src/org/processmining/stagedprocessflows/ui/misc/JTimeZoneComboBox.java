package org.processmining.stagedprocessflows.ui.misc;

import java.awt.Component;
import java.util.TimeZone;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

public class JTimeZoneComboBox extends JComboBox {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3477137619353206167L;
	private final DefaultComboBoxModel cbModel;

	/** Creates a new instance of JTimeZoneComboBox */
	public JTimeZoneComboBox() {
		cbModel = new DefaultComboBoxModel();

		String[] ids = TimeZone.getAvailableIDs();

		for (int i = 0; i < ids.length; i++) {
			TimeZone timeZone = TimeZone.getTimeZone(ids[i]);
			cbModel.addElement(timeZone);
		}

		setModel(cbModel);
		setRenderer(new TimeZoneComboBoxCellRenderer());
		setMaximumRowCount(16);
	}

	public JTimeZoneComboBox(TimeZone defaultTimeZone) {
		this();
		setSelectedItem(defaultTimeZone);
	}

	public void setSelectedItem(Object timeZoneObject) {
		if (cbModel.getIndexOf(timeZoneObject) == -1) {
			cbModel.addElement(timeZoneObject);
		}

		super.setSelectedItem(timeZoneObject);
	}

	private static class TimeZoneComboBoxCellRenderer extends DefaultListCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7816646038259553455L;

		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			TimeZone timeZone = (TimeZone) value;
			int rawOffset = timeZone.getRawOffset() / 60000;
			int hours = rawOffset / 60;
			int minutes = Math.abs(rawOffset) % 60;
			String hrStr = "";

			if (Math.abs(hours) < 10) {
				if (hours < 0) {
					hrStr = "-0" + Math.abs(hours);
				} else {
					hrStr = "0" + Math.abs(hours);
				}
			} else {
				hrStr = Integer.toString(hours);
			}

			String minStr = (minutes < 10) ? ("0" + Integer.toString(minutes)) : Integer.toString(minutes);
			String str = "(UTC " + ((timeZone.getRawOffset() >= 0) ? "+" : "") + hrStr + ":" + minStr + ") "
					+ timeZone.getID();

			return super.getListCellRendererComponent(list, str, index, isSelected, cellHasFocus);
		}
	}
}
