package org.processmining.stagedprocessflows.ui.jidesoft;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.joda.time.DateTime;

public class RangeSliderDemo2 extends ApplicationFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6926467788073655177L;
	private RangeSlider rangeSlider;

	public RangeSliderDemo2(String s) {
		super(s);
		JPanel jpanel = createRangePanel();
		jpanel.setPreferredSize(new Dimension(500, 270));
		setContentPane(jpanel);
	}

	public static void main(String args[]) {
		RangeSliderDemo2 demo = new RangeSliderDemo2("test");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

	private JPanel createRangePanel() {
		final JTextField minField = new JTextField();
		minField.setHorizontalAlignment(SwingConstants.LEFT);
		final JTextField maxField = new JTextField();
		maxField.setHorizontalAlignment(SwingConstants.RIGHT);

		rangeSlider = new RangeSlider(1383964628, 1447036628, 1415500628, 1447036628);
		rangeSlider.setPaintTicks(true);
		rangeSlider.setMajorTickSpacing(3600 * 24 * 7);
		rangeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				minField.setText((new DateTime(((long) rangeSlider.getLowValue()) * 1000)).toString("dd.MM.yyyy HH:mm"));
				maxField.setText((new DateTime(((long) rangeSlider.getHighValue()) * 1000))
						.toString("dd.MM.yyyy HH:mm"));
			}
		});

		minField.setText("" + rangeSlider.getLowValue());
		maxField.setText("" + rangeSlider.getHighValue());

		JPanel minPanel = new JPanel(new BorderLayout());
		minPanel.add(new JLabel("Min"), BorderLayout.BEFORE_FIRST_LINE);
		minField.setEditable(false);
		minPanel.add(minField);

		JPanel maxPanel = new JPanel(new BorderLayout());
		maxPanel.add(new JLabel("Max", SwingConstants.TRAILING), BorderLayout.BEFORE_FIRST_LINE);
		maxField.setEditable(false);
		maxPanel.add(maxField);

		JPanel textFieldPanel = new JPanel(new GridLayout(1, 3));
		textFieldPanel.add(minPanel);
		textFieldPanel.add(new JPanel());
		textFieldPanel.add(maxPanel);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(rangeSlider, BorderLayout.CENTER);
		panel.add(textFieldPanel, BorderLayout.AFTER_LAST_LINE);

		return panel;
	}
}
