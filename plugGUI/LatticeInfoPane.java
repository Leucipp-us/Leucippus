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
	private String[] testArray = {"BondLength","BondLength","BondLength"};
	private JPanel secondpane;
	private Communicator comm;
	private DrawableList pointlist;
	private DrawableList linelist;
	private DrawableList pointsetlist;
	private DrawableHandler drawHandler;
	private JComboBox<String> pointsetCB;
	private JComboBox<String> linelistCB;

	public LatticeInfoPane(){
		setLayout(new SpringLayout());
		setupFirstStep();
		setupSecondStep();
		SpringUtilities.makeCompactGrid(this, 
										2, 2, 6, 6, 6, 6);
	}

	public LatticeInfoPane(Communicator comm,
						   DrawableHandler drawHandler,
						   DrawableList pointlist,
						   DrawableList linelist,
						   DrawableList pointsetlist){
		this.comm = comm;
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

		c.gridx = 0;
		c.gridy = 0;
		JLabel requirements = new JLabel("Requirements");
		firstpane.add(requirements, c);

		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		JTextArea reqList = new JTextArea();
		reqList.setEditable(false);
		reqList.setText("Open Image of a TEM");
		requirements.setLabelFor(reqList);
		firstpane.add(reqList, c);

		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		JButton calc = new JButton("Calculate");
		firstpane.add(calc, c);

		calc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				comm.calculatePoints(drawHandler.getGrayScaleOriginal(),
										null,
										null);
			}
		});

		add(firstlabel);
		firstlabel.setLabelFor(firstpane);
		add(firstpane);
	}

	private void setupSecondStep() {
		JLabel secondlabel = new JLabel("Step 2: Spatially Contrain Detections");
		secondpane = new JPanel();
		secondpane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;

		c.gridx = 0;
		c.gridy = 0;
		JLabel requirements = new JLabel("Requirements");
		secondpane.add(requirements, c);

		c.gridx = 0;
		c.gridy = 1;
		JLabel reqPointset = new JLabel("Set of Points");
		secondpane.add(reqPointset, c);

		ArrayList<String> tlist = new ArrayList<String>(pointsetlist.getList().size());
		for (DrawableItem i : pointsetlist.getList()) {
			tlist.add(i.getName());
		}
		String[] arr = new String[tlist.size()];

		pointsetCB = new JComboBox<String>(tlist.toArray(arr));
		c.gridx = 1;
		c.gridy = 1;
		secondpane.add(pointsetCB, c);

		JLabel reqbondlength = new JLabel("Bond Length");
		tlist = new ArrayList<String>(linelist.getList().size());
		for (DrawableItem i : linelist.getList()) {
			tlist.add(i.getName());
		}
		arr = new String[tlist.size()];

		
		c.gridx = 0;
		c.gridy = 2;
		secondpane.add(reqbondlength, c);

		linelistCB = new JComboBox<String>(tlist.toArray(arr));
		c.gridx = 1;
		c.gridy = 2;
		secondpane.add(linelistCB, c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		JButton calc = new JButton("Calculate");
		secondpane.add(calc,c);

		calc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedBondIndex = linelistCB.getSelectedIndex();
				DrawableLine l = (DrawableLine)linelist.getList().get(selectedBondIndex);
				comm.calculatePoints(drawHandler.getGrayScaleOriginal(),
										l);
			}
		});

		add(secondlabel);
		secondlabel.setLabelFor(secondpane);
		add(secondpane);
	}

	private void setupThirdStep() {
		JLabel thirdlabel = new JLabel("");
	}

	public void tableChanged(TableModelEvent e) {
		refreshPointSetComboBox();
		refreshLineSetComboBox();
		secondpane.revalidate();
	}

	private void refreshPointSetComboBox() {
		secondpane.remove(pointsetCB);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		ArrayList<String> pointsets = new ArrayList<String>(pointsetlist.getList().size());
		for (DrawableItem i : pointsetlist.getList()) {
			pointsets.add(i.getName());
		}
		String[] arr = new String[pointsets.size()];
		pointsetCB = new JComboBox<String>(pointsets.toArray(arr));
		c.gridx = 1;
		c.gridy = 1;
		secondpane.add(pointsetCB, c);
	}

	private void refreshLineSetComboBox() {
		secondpane.remove(linelistCB);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		ArrayList<String> lines = new ArrayList<String>(linelist.getList().size());
		for (DrawableItem i : linelist.getList()) {
			lines.add(i.getName());
		}
		String[] arr = new String[lines.size()];
		linelistCB = new JComboBox<String>(lines.toArray(arr));
		c.gridx = 1;
		c.gridy = 2;
		secondpane.add(linelistCB, c);
	}
}