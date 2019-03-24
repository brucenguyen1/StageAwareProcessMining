package org.processmining.sapm.perspectivemining.ui.forms;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;

import com.fluxicon.slickerbox.factory.SlickerFactory;


public class ProgressBarDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JProgressBar progressBar;
	private JTextArea textArea;
	private JButton btnCancel;
	private JScrollPane scroll;
	
	public ProgressBarDialog(int max)	
	{				
		setSize(355,260);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		
		SlickerFactory factory = SlickerFactory.instance();
		
		JPanel panel = factory.createGradientPanel();
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		panel.setBounds(0,0,355,260);
		panel.setLayout(null);
		
		getContentPane().add(panel);
		
		JLabel lblProgress = factory.createLabel("Progress...");
		lblProgress.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblProgress.setBounds(12, 0, 123, 41);
		panel.add(lblProgress);
		
		progressBar = factory.createProgressBar(1);
		progressBar.setMinimum(0);
		progressBar.setMaximum(max);
		progressBar.setBounds(12, 41, 326, 28);
		progressBar.setValue(0);
		panel.add(progressBar);
		
		textArea = new JTextArea("START process comparison...\n");
		textArea.setBackground(Color.LIGHT_GRAY);
		textArea.setBounds(12, 82, 326, 117);
		textArea.setEditable(false);
		
		scroll = new JScrollPane(textArea);
		scroll.setBounds(12, 82, 326, 117);
		panel.add(scroll);
		
		btnCancel = factory.createButton("Cancel");
		btnCancel.setBounds(125, 212, 97, 38);
		panel.add(btnCancel);		
		
	}
	
	public int getProgress()
	{
		return progressBar.getValue();
	}
	public void setProgress(int progress)
	{
		progressBar.setValue(progress);
	}
	
	public void increment()
	{
		if(progressBar.getValue() < progressBar.getMaximum())
			progressBar.setValue(progressBar.getValue() + 1);
	}
	
	public void appendText(String newline)
	{
		textArea.append(newline + "...\n"); 
		scroll.validate();
		JScrollBar vertical = scroll.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());
	}
	
	public void addButtonListener(ActionListener a)
	{
		btnCancel.addActionListener(a);
	}
	
	public void setMaximum(int i)
	{
		progressBar.setMaximum(i);
	}
	
	public void refresh()
	{
		this.validate();
	}
	
	
}
