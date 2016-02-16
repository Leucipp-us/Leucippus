package plugGUI;

import plugComm.*;
import plugGUI.*;
import layout.SpringUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
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

	public LatticeInfoPane(){
		setLayout(new SpringLayout());
		setupFirstStep();
		setupSecondStep();
		SpringUtilities.makeCompactGrid(this,
										2, 2, 6, 6, 6, 6);
	}

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
		setupFirstStep();
		setupSecondStep();
		SpringUtilities.makeCompactGrid(this,
										2, 2, 6, 6, 6, 6);
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

	public void tableChanged(TableModelEvent e) {
		pointsetCB.removeAllItems();
		for (DrawableItem i : pointsetlist.getList())
			pointsetCB.addItem(i.getName());

		linelistCB.removeAllItems();
		for (DrawableItem i : linelist.getList())
			linelistCB.addItem(i.getName());
	}
}
