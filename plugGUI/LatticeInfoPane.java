package plugGUI;

import plugComm.*;
import plugGUI.*;
import layout.SpringUtilities;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;


public class LatticeInfoPane extends JPanel implements TableModelListener{
	private Comm2 comm;
	private DrawableList pointlist;
	private DrawableList linelist;
	private DrawableList pointsetlist;
	private DrawableHandler drawHandler;
	private JComboBox<String> pointsetCB = new JComboBox<String>();
	private JComboBox<String> linelistCB = new JComboBox<String>();;
	private JTextField sigmafield = new JTextField("4");
	private JTextField kernelfield = new JTextField("17");
	private JCheckBox autoCheckBox = new JCheckBox();


	/**
	 * Default constructor
	 * Not really used.
	 */
	public LatticeInfoPane(){
		setLayout(new SpringLayout());
		setupFirstStep();
		setupSecondStep();
		SpringUtilities.makeCompactGrid(this,
										2, 2, 6, 6, 6, 6);
	}

	/**
	 * Constructor for LatticeInfoPane
	 * @param pointlist			the container for the list of points
	 * @param linelist			the container for the list of lines
	 * @param pointsetlist	the container for the list of pointsets
	 */
	public LatticeInfoPane(DrawableHandler drawHandler,
											   DrawableList pointlist,
											   DrawableList linelist,
											   DrawableList pointsetlist){
		this.comm = Comm2.getInstance();
		this.pointlist = pointlist;
		this.linelist = linelist;
		this.pointsetlist = pointsetlist;

		linelist.addListener(this);
		pointsetlist.addListener(this);

		this.drawHandler = drawHandler;
		setLayout(new SpringLayout());
		setupAutomatic();
		setupFirstStep();
		setupSecondStep();
		SpringUtilities.makeCompactGrid(this,
										3, 2, 6, 6, 6, 6);
	}

	private void setupAutomatic() {
		JLabel autolabel = new JLabel("Automatic Detection");
		JPanel autopane = new JPanel();
		autopane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;

		c.gridx = 0; c.gridy = 0;
		autopane.add(new JLabel("Learn Parameters for Detection Automatically"), c);

		c.gridx = 1; c.gridy = 0;
		autopane.add(autoCheckBox, c);
		autoCheckBox.addItemListener(new ItemListener() {
	    public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {//checkbox has been selected
          sigmafield.setEnabled(false);
					kernelfield.setEnabled(false);
					pointsetCB.setEnabled(false);
					linelistCB.setEnabled(false);
        } else {//checkbox has been deselected
					sigmafield.setEnabled(true);
					kernelfield.setEnabled(true);
					pointsetCB.setEnabled(true);
					linelistCB.setEnabled(true);
        };
	    }
	});


		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 1;
		JButton autoDetectButton = new JButton("Automatic Detection");
		autopane.add(autoDetectButton, c);

		add(autolabel);
		add(autopane);
	}

	private void setupFirstStep() {
		JLabel firstlabel = new JLabel("Step 1: Initial Detections");
		JPanel firstpane = new JPanel();
		firstpane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;

		c.gridx = 0; c.gridy = 0;
		firstpane.add(new JLabel("Requirements"), c);

		c.gridwidth = 4;
		c.gridx = 0; c.gridy = 1;
		firstpane.add(new JLabel("Parameters for Gaussian Smoothing"), c);

		c.gridwidth = 1;
		c.gridx = 0; c.gridy = 2;
		firstpane.add(new JLabel("Sigma"), c);

		c.gridwidth = 3;
		c.gridx = 1; c.gridy = 2;
		firstpane.add(sigmafield, c);

		c.gridwidth = 1;
		c.gridx = 0; c.gridy = 3;
		firstpane.add(new JLabel("Kernel Size"), c);

		c.gridwidth = 3;
		c.gridx = 1; c.gridy = 3;
		firstpane.add(kernelfield, c);


		c.gridwidth = 4;
		c.gridx = 0; c.gridy = 4;
		JButton calc = new JButton("Detect");
		firstpane.add(calc, c);

		calc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double sigma = Double.parseDouble(sigmafield.getText());
				int blocksize = Integer.parseInt(kernelfield.getText());
				if(blocksize < 3 || blocksize%2==0){
					//We will need a popup saying the exact needed specs of the kernel
					return; //need to output stuff
				}
				comm.calculatePoints(drawHandler.getGrayScaleOriginal(),
										null,
										null,
										sigma,
										blocksize);
			}
		});

		add(firstlabel);
		add(firstpane);
	}

	private void setupSecondStep() {
		JPanel secondpane = new JPanel();
		secondpane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;

		c.gridx = 0; c.gridy = 0;
		secondpane.add(new JLabel("Requirements"), c);

		c.gridx = 0; c.gridy = 1;
		secondpane.add(new JLabel("Set of Points"), c);

		c.gridx = 1; c.gridy = 1;
		secondpane.add(pointsetCB, c);

		c.gridx = 0; c.gridy = 2;
		secondpane.add(new JLabel("Bond Length"), c);

		c.gridx = 1; c.gridy = 2;
		secondpane.add(linelistCB, c);

		c.gridwidth = 2;
		c.gridx = 0; c.gridy = 3;
		JButton calc = new JButton("Constrain");
		secondpane.add(calc,c);

		calc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double sigma = Double.parseDouble(sigmafield.getText());
				int blocksize = Integer.parseInt(kernelfield.getText());
				if(blocksize < 3 || blocksize%2==0){
					//We will need a popup saying the exact needed specs of the kernel
					return; //need to output stuff
				}
				comm.constrainPoints(
					drawHandler.getGrayScaleOriginal(),
					null,
					(DrawableLine)linelist.getList().get(linelistCB.getSelectedIndex()),
					(DrawablePointSet)pointsetlist.getList().get(pointsetCB.getSelectedIndex()),
					sigma,
					blocksize
				);
			}
		});

		add(new JLabel("Step 2: Spatially Contrain Detections"));
		add(secondpane);
	}

	private void setupThirdStep() {
		JLabel thirdlabel = new JLabel("");
	}


	/**
	 * Resets the items within the comboboxes so they stay current.
	 * @param e 		the event that triggered the change.
	 */
	public void tableChanged(TableModelEvent e) {
		pointsetCB.removeAllItems();
		for (DrawableItem i : pointsetlist.getList())
			pointsetCB.addItem(i.getName());

		linelistCB.removeAllItems();
		for (DrawableItem i : linelist.getList())
			linelistCB.addItem(i.getName());
	}
}
