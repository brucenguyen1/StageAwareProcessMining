// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space

package org.processmining.stagedprocessflows.ui.system;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Seconds;
import org.processmining.framework.util.ui.widgets.ProMTable;
import org.processmining.stagedprocessflows.models.SPF;
import org.processmining.stagedprocessflows.models.SPFStage;
import org.processmining.stagedprocessflows.ui.jidesoft.RangeSlider;
import org.processmining.stagedprocessflows.ui.main.NodeView;

import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class SPFSummaryView extends NodeView implements ActionListener {
	private RangeSlider rangeSlider;
	private final JPanel tablePanel = new JPanel();
	private JPanel mainPanel;
	private final JLabel labelRange = SlickerFactory.instance().createLabel("");
	private DateTime start;
	private DateTime end;

	public SPFSummaryView(SPF bpf, String label) {
		super(bpf, null, label);
		start = bpf.getStartTimePoint();
		end = bpf.getLastSeriesPoint();
		updateLabelRange(start, end);
		createTablePanel(start, end);
	}

	@Override
	public JPanel createPanel() {
		chartPanel = null;
		double size[][] = { { 70, TableLayoutConstants.FILL, 100, TableLayoutConstants.FILL, 20 },
				{ 50, 20, 20, TableLayoutConstants.FILL } };
		mainPanel = new JPanel(new TableLayout(size));

		// Note: always get from BPF to show the full time range
		//ROW 1
		mainPanel.add(createRangePanel(bpf.getStartTimePoint(), bpf.getLastSeriesPoint()), "1,0,3,0");

		//ROW 2
		JButton btnUpdate = new JButton("Update");
		btnUpdate.setActionCommand("UPDATE");
		btnUpdate.addActionListener(this);
		mainPanel.add(btnUpdate, "2,1");

		//ROW 3
		mainPanel.add(labelRange, "1,2,3,2");

		//ROW 4
		mainPanel.add(tablePanel, "1,3,3,3");

		return mainPanel;
	}

	private void updateLabelRange(DateTime start, DateTime end) {
		labelRange.setText("<html><center>"
				+ "<h4>From "
				+ start.withZone(DateTimeZone.forTimeZone(bpf.getConfig().getTimeZone())).toString(
						"EEE, d MMM yyyy HH:mm:ss Z")
				+ " To "
				+ end.withZone(DateTimeZone.forTimeZone(bpf.getConfig().getTimeZone())).toString(
						"EEE, d MMM yyyy HH:mm:ss Z") + " (" + Days.daysBetween(start, end).getDays()
				+ " days) (Mean/Median)</h4>" + "</center></html>");
		labelRange.setHorizontalAlignment(SwingConstants.CENTER);
		labelRange.setVerticalAlignment(SwingConstants.CENTER);
	}

	private JPanel createTablePanel(DateTime start, DateTime end) {
		NumberFormat formatter = new DecimalFormat("#0.00");
		Object[][] tableContent = new Object[bpf.getStages().size() * 2 + 1][7];

		try {
			tableContent[0][0] = "System";
			tableContent[0][1] = formatter.format(24 * bpf.getMeanArrivalRate(start, end)) + " / "
					+ formatter.format(24 * bpf.getMedianArrivalRate(start, end));
			tableContent[0][2] = formatter.format(24 * bpf.getMeanDepartureRate(start, end)) + " / "
					+ formatter.format(24 * bpf.getMedianDepartureRate(start, end));
			tableContent[0][3] = formatter.format(24 * bpf.getMeanExitRate(start, end)) + " / "
					+ formatter.format(24 * bpf.getMedianExitRate(start, end));
			tableContent[0][4] = formatter.format(bpf.getMeanWIP(start, end)) + " / "
					+ formatter.format(bpf.getMedianWIP(start, end));
			tableContent[0][5] = formatter.format(bpf.getMeanTIS(start, end)) + " / "
					+ formatter.format(bpf.getMedianTIS(start, end));
			if (bpf.getConfig().getCheckStartCompleteEvents()) {
				tableContent[0][6] = formatter.format(100 * bpf.getMeanFE(start, end));
			} else {
				tableContent[0][6] = "--";
			}

			int i = 1;
			for (SPFStage stage : bpf.getStages()) {
				tableContent[i][0] = stage.getName() + "-queue";

				//QUEUE
				tableContent[i][1] = formatter.format(24 * stage.getMean(SPF.CHAR_QUEUE_ARRIVAL_RATE, start, end))
						+ " / " + formatter.format(24 * stage.getMedian(SPF.CHAR_QUEUE_ARRIVAL_RATE, start, end));
				tableContent[i][2] = formatter.format(24 * stage.getMean(SPF.CHAR_QUEUE_DEPARTURE_RATE, start, end))
						+ " / " + formatter.format(24 * stage.getMedian(SPF.CHAR_QUEUE_DEPARTURE_RATE, start, end));
				tableContent[i][4] = formatter.format(stage.getMean(SPF.CHAR_QUEUE_CIP, start, end)) + " / "
						+ formatter.format(stage.getMedian(SPF.CHAR_QUEUE_CIP, start, end));
				tableContent[i][5] = formatter.format(stage.getMean(SPF.CHAR_QUEUE_TIS, start, end)) + " / "
						+ formatter.format(stage.getMedian(SPF.CHAR_QUEUE_TIS, start, end));

				//SERVICE
				tableContent[i + 1][0] = stage.getName() + "-service";
				tableContent[i + 1][1] = formatter
						.format(24 * stage.getMean(SPF.CHAR_SERVICE_ARRIVAL_RATE, start, end))
						+ " / "
						+ formatter.format(24 * stage.getMedian(SPF.CHAR_SERVICE_ARRIVAL_RATE, start, end));
				tableContent[i + 1][2] = formatter.format(24 * stage.getMean(SPF.CHAR_SERVICE_DEPARTURE_RATE, start,
						end))
						+ " / "
						+ formatter.format(24 * stage.getMedian(SPF.CHAR_SERVICE_DEPARTURE_RATE, start, end));
				tableContent[i + 1][3] = formatter.format(24 * stage.getMean(SPF.CHAR_SERVICE_EXIT_RATE, start, end))
						+ " / " + formatter.format(24 * stage.getMedian(SPF.CHAR_SERVICE_EXIT_RATE, start, end));
				tableContent[i + 1][4] = formatter.format(stage.getMean(SPF.CHAR_SERVICE_CIP, start, end)) + " / "
						+ formatter.format(stage.getMedian(SPF.CHAR_SERVICE_CIP, start, end));
				tableContent[i + 1][5] = formatter.format(stage.getMean(SPF.CHAR_SERVICE_TIS, start, end)) + " / "
						+ formatter.format(stage.getMedian(SPF.CHAR_SERVICE_TIS, start, end));

				if (bpf.getConfig().getCheckStartCompleteEvents()) {
					tableContent[i + 1][6] = formatter.format(100 * stage.getMean(SPF.CHAR_SERVICE_FLOW_EFFICIENCY,
							start, end))
							+ " / "
							+ formatter.format(100 * stage.getMedian(SPF.CHAR_SERVICE_FLOW_EFFICIENCY, start, end));
				} else {
					tableContent[i + 1][6] = "--";
				}

				i = i + 2;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return null;
		}

		DefaultTableModel tableModel = new DefaultTableModel(tableContent, new Object[] { "", "AR(cases/day)",
				"DR(cases/day)", "ER(cases/day)", "CIP(cases)", "TIS(hours)", "FE(%)" });

		tablePanel.removeAll();
		ProMTable table = new ProMTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tablePanel.add(table);

		return tablePanel;
	}

	private JPanel createRangePanel(DateTime start, DateTime end) {
		final JTextField minField = new JTextField();
		minField.setHorizontalAlignment(SwingConstants.LEFT);
		final JTextField maxField = new JTextField();
		maxField.setHorizontalAlignment(SwingConstants.RIGHT);

		rangeSlider = new RangeSlider((int) (start.getMillis() / 1000), (int) (end.getMillis() / 1000),
				(int) (start.getMillis() / 1000), (int) (end.getMillis() / 1000));
		rangeSlider.setPaintTicks(true);
		rangeSlider.setMajorTickSpacing(Seconds.secondsBetween(start, end).getSeconds() / 100);
		rangeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				minField.setText((new DateTime(((long) rangeSlider.getLowValue()) * 1000)).withZone(
						DateTimeZone.forTimeZone(bpf.getConfig().getTimeZone())).toString("EEE, d MMM yyyy HH:mm:ss Z"));
				maxField.setText((new DateTime(((long) rangeSlider.getHighValue()) * 1000)).withZone(
						DateTimeZone.forTimeZone(bpf.getConfig().getTimeZone())).toString("EEE, d MMM yyyy HH:mm:ss Z"));
			}
		});

		minField.setText(start.withZone(DateTimeZone.forTimeZone(bpf.getConfig().getTimeZone())).toString(
				"EEE, d MMM yyyy HH:mm:ss Z"));
		maxField.setText(end.withZone(DateTimeZone.forTimeZone(bpf.getConfig().getTimeZone())).toString(
				"EEE, d MMM yyyy HH:mm:ss Z"));

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

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("UPDATE")) {
			start = new DateTime(((long) rangeSlider.getLowValue()) * 1000);
			end = new DateTime(((long) rangeSlider.getHighValue()) * 1000);

			updateLabelRange(start, end);
			createTablePanel(start, end);
			mainPanel.revalidate();
			mainPanel.repaint();
		}
	}
}
