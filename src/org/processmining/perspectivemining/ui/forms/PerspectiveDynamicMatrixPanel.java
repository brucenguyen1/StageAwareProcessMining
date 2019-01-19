package org.processmining.perspectivemining.ui.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableColumn;

import org.processmining.perspectivemining.datasource.PerspectiveInputObject;
import org.processmining.perspectivemining.graph.settings.PerspectiveSettingObject;

import example.matrix.renderers.ColourRenderer;
import example.matrix.renderers.StringHeaderRenderer;
import model.graph.GraphModel;
import model.matrix.DefaultMatrixTableModel;
import swingPlus.matrix.JHeaderRenderer;
import swingPlus.matrix.JMatrix;
import util.colour.ColorUtilities;
import util.ui.VerticalLabelUI;

/**
 * This panel displays a row of perspective matrices, one for each window 
 * @author Bruce
 *
 */
public class PerspectiveDynamicMatrixPanel extends JPanel {
	private PerspectiveInputObject input1;
	private PerspectiveInputObject input2;
	private PerspectiveSettingObject setting;

	public PerspectiveDynamicMatrixPanel (List<GraphModel> graphModels, PerspectiveInputObject input1, PerspectiveInputObject input2, PerspectiveSettingObject setting) {
		this.input1 = input1;
		this.input2 = input2;
		this.setting = setting;
		
		List<JMatrix> matrices = new ArrayList<>();
		
		for (GraphModel gm : graphModels) {
			matrices.add(new JMatrix(new DefaultMatrixTableModel(gm)));
		}
		
		JFrame.setDefaultLookAndFeelDecorated (true);
			
		for (JMatrix matrix : matrices) {
			this.setupMatrix(matrix);
		}
		
		List<JScrollPane> panes = new ArrayList<>();
		for (JMatrix matrix : matrices) {
			panes.add(new JScrollPane(matrix));
		}
		
		JPanel panelLabel = new JPanel (new GridLayout (1, matrices.size()));
		for (GraphModel gm : graphModels) {
			panelLabel.add (new JLabel ("Testing"));
		}
		
		JPanel panelMatrix = new JPanel (new GridLayout (1, matrices.size()));
		for (JScrollPane pane : panes) {
			panelMatrix.add (pane);
		}
		JScrollPane mainScrollPane = new JScrollPane(panelMatrix);
		
		this.setLayout(new BorderLayout());
		this.add(panelLabel, BorderLayout.NORTH);
		this.add(mainScrollPane, BorderLayout.CENTER);
	}	
	
	private void setupMatrix(JMatrix table) {
		for (int i = 0; i < table.getColumnCount(); i++) {
		    final TableColumn column = table.getColumnModel().getColumn(i);
		    column.setMinWidth (1); 
		    column.setMaxWidth (1000); 
		    column.setPreferredWidth (table.getRowHeight()); 
		}

		table.setDefaultRenderer (Color.class, new ColourRenderer ());
		table.setGridColor (ColorUtilities.mixColours (table.getBackground(), Color.black, 0.8f));
		final JHeaderRenderer stringHeader = new StringHeaderRenderer ();
		final JHeaderRenderer stringHeader2 = new StringHeaderRenderer ();
		table.getRowHeader().setDefaultRenderer (Object.class, stringHeader);
		table.getRowHeader().setDefaultRenderer (String.class, stringHeader);
		table.getColumnHeader().setDefaultRenderer (Object.class, stringHeader2);
		table.getColumnHeader().setDefaultRenderer (String.class, stringHeader2);
		((JLabel)stringHeader2).setUI (new VerticalLabelUI (false));
		stringHeader.setSelectionBackground (table.getRowHeader());
		stringHeader2.setSelectionBackground (table.getColumnHeader());
		table.setDefaultRenderer (String.class, stringHeader);
		table.getColumnHeader().setRowHeight (150);
	}
}
