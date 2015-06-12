package plugGUI;

import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;

import javax.swing.event.TableModelListener;


public class DrawableList extends JTable{
	private DefaultTableModel tableModel;
	private ArrayList<DrawableItem> drawables;

	public DrawableList() {
		String[] colHeads = new String[]{"Draw","Color", "Name"};
		drawables = new ArrayList<DrawableItem>();

		tableModel = new DefaultTableModel(colHeads, 4) {
			public int getColumnCount() {return 3;}
			public int getRowCount() { return drawables.size(); }

			public Class getColumnClass(int c) {
		        return getValueAt(0, c).getClass();
		    }
			public Object getValueAt(int row,int col) {
				DrawableItem item = drawables.get(row);
				if (col == 2) {
					return item.getName();
				}
				else if (col == 1) {
					return item.getColor();
				}
				else {
					return new Boolean(item.isDrawn());
				}

			}

			public boolean isCellEditable(int row, int col) {
		        //Note that the data/cell address is constant,
		        //no matter where the cell appears onscreen.
		        return true;
		    }

		    public void setValueAt(Object value, int row, int col) {
		        if (col == 0) {
		        	drawables.get(row).isDrawn(((Boolean)value).booleanValue());
		        }

		        else if( col == 2) {
		        	drawables.get(row).setName((String)value);
		        }
		        fireTableCellUpdated(row, col);
		    }
		};
		setModel(tableModel);
		setDefaultRenderer(Color.class, new ColorRenderer());
		setDefaultEditor(Color.class, new ColorEditor());
		getColumnModel().getColumn(1).setPreferredWidth(10);
	}

	public void addItem(DrawableItem item) {
		drawables.add(item);
		tableModel.fireTableDataChanged();
	}

	public void removeItem(int rowIndex) {
		if (rowIndex < drawables.size() && drawables.size() != 0) {
			drawables.remove(rowIndex);
		}
		tableModel.fireTableDataChanged();
	}

	public int getLength() {
		return drawables.size();
	}

	public ArrayList<DrawableItem> getList(){
		return drawables;
	}

	public void addListener(TableModelListener tml) {
		getModel().addTableModelListener(tml);
	}

	private class ColorRenderer extends JLabel implements TableCellRenderer {
 
	    public ColorRenderer() {
	    	setOpaque(true);
	    }
	 
	    public Component getTableCellRendererComponent(
	                            JTable table, Object value,
	                            boolean isSelected, boolean hasFocus,
	                            int row, int column) {
	    	Color col = drawables.get(row).getColor();
	    	
	        setBackground(col);
	        setToolTipText("RGB value: " + col.getRed() + ", "
                                     + col.getGreen() + ", "
                                     + col.getBlue());
	        return this;
	    }
	}
	private class ColorEditor extends AbstractCellEditor
	                         implements TableCellEditor,
	                        ActionListener {
	    Color currentColor;
	    JButton button;
	    JColorChooser colorChooser;
	    JDialog dialog;
	    int row = 0;
	    protected static final String EDIT = "edit";
	 
	    public ColorEditor() {
	        //Set up the editor (from the table's point of view),
	        //which is a button.
	        //This button brings up the color chooser dialog,
	        //which is the editor from the user's point of view.
	        button = new JButton();
	        button.setActionCommand(EDIT);
	        button.addActionListener(this);
	        button.setBorderPainted(false);
	 
	        //Set up the dialog that the button brings up.
	        colorChooser = new JColorChooser();
	        dialog = JColorChooser.createDialog(button,
	                                        "Pick a Color",
	                                        true,  //modal
	                                        colorChooser,
	                                        this,  //OK button handler
	                                        null); //no CANCEL button handler
	    }
	 
	    /**
	     * Handles events from the editor button and from
	     * the dialog's OK button.
	     */
	    public void actionPerformed(ActionEvent e) {
	        if (EDIT.equals(e.getActionCommand())) {
	            //The user has clicked the cell, so
	            //bring up the dialog.
	            button.setBackground(currentColor);
	            colorChooser.setColor(currentColor);
	            dialog.setVisible(true);
	 
	            //Make the renderer reappear.
	            fireEditingStopped();
	 
	        } else { //User pressed dialog's "OK" button.
	            currentColor = colorChooser.getColor();
	            drawables.get(row).setColor(currentColor);
	        }
	    }
	 
	    //Implement the one CellEditor method that AbstractCellEditor doesn't.
	    public Object getCellEditorValue() {
	        return currentColor;
	    }
	 
	    //Implement the one method defined by TableCellEditor.
	    public Component getTableCellEditorComponent(JTable table,
	                                                 Object value,
	                                                 boolean isSelected,
	                                                 int _row,
	                                                 int column) {
	        currentColor = (Color)value;
	        row = _row;
	        return button;
	    }
	}
}