/**
 * 
 */
package wota.graphics;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import wota.gamemaster.StatisticsLogger;
import wota.gameobjects.Caste;
import wota.gameobjects.GameWorld;
import wota.gameobjects.GameWorld.Player;

/**
 * View class for the statistics. Draws an JTable and puts in
 * StatisticsView.StatisticsTableModel.
 */
public class StatisticsView implements Runnable {

	private JFrame frame;
	private GameWorld gameWorld;
	private StatisticsLogger logger;
	private StatisticsTableModel statisticsTableModel;

	public StatisticsView(GameWorld gameWorld, StatisticsLogger logger) {
		setGameWorld(gameWorld, logger);
	}

	public void run() {
		//lazy initialization of frame
		if (frame == null) {
			frame = new JFrame("Wota Statistics");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// set logo as icon
			try {
				frame.setIconImage(ImageIO.read(getClass().getResource("logo.png")));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		frame.setLayout(new FlowLayout());

		statisticsTableModel = new StatisticsTableModel(logger);
		JTable table = new JTable(statisticsTableModel);
		table.setDefaultRenderer(Object.class, new CellRenderer());

		// table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);

		JScrollPane scrollPane = new JScrollPane(table);
		frame.getContentPane().add(scrollPane);

		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * The StatisticTableModel gets passed to the JTable. It implements basic
	 * methods like getValueAt() which are used by the JTable to obtain the cell
	 * values.
	 */
	public class StatisticsTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -2557558732669501033L;
		private StatisticsLogger logger;
		private final String[] playerNames;
		/**
		 * representing the table data, 1st index = row, 2nd = column, 0 = row
		 * name, 1.. players
		 */
		private Object[][] data;

		public StatisticsTableModel(StatisticsLogger logger) {
			this.logger = logger;
			String[] rowNames = new String[] { "", "# Ants", "# Gatherer",
					"# Soldiers", "# Scouts", "# created ants", "# lost ants",
					"collected food" };
			data = new Object[rowNames.length][gameWorld.getPlayers().size() + 1];
			for (int i = 0; i < rowNames.length; i++) {
				data[i][0] = rowNames[i];
			}

			playerNames = new String[gameWorld.getPlayers().size()];
			for (Player player : gameWorld.getPlayers()) {
				playerNames[player.id()] = player.name;
			}

		}

		public int getRowCount() {
			return data.length;
		}

		public int getColumnCount() {
			return data[0].length;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "";
			}
			return playerNames[column - 1];
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}

		/** updates all of the data 
		 * 
		 * @throws NullPointerException from player.numAnts
		 */
		public synchronized void refresh() throws NullPointerException{
			for (int column = 1; column < getColumnCount(); column++) {
				int playerId = column - 1;
				Player player = gameWorld.getPlayers().get(playerId);

				data[0][column] = PlayerColors.get(player.id());
				data[1][column] = player.getAntObjects().size();
				data[2][column] = player.numAnts(Caste.Gatherer);
				data[3][column] = player.numAnts(Caste.Soldier);
				data[4][column] = player.numAnts(Caste.Scout);
				data[5][column] = logger.createdAnts()[playerId];
				data[6][column] = logger.diedAnts()[playerId];
				data[7][column] = logger.collectedFood()[playerId];
			}
		}
		
		@Override
		public String toString() {
			String out = new String(), format = new String();
			String columnFormat = "%18s";
			String firstColumnFormat = "%18s";
			for (int i=1; i<getColumnCount(); i++) {
				format = format + columnFormat;
			}
			format = format + "\n";
			
			out = out + String.format(firstColumnFormat, new Object[] {""});
			out = out + String.format(format, (Object[]) playerNames);
			for (int row = 1; row < getRowCount(); row++) {
				out = out + String.format(firstColumnFormat + format, data[row]);
			}
			return out;
		}

	}

	/** call this when the table should grab the information
	 * 
	 * @throws NullPointerException
	 */
	public void refresh() throws NullPointerException{
		statisticsTableModel.refresh();
		
		// tell the JTable to update graphics
		statisticsTableModel.fireTableDataChanged();		
	}

	public class CellRenderer extends JLabel implements TableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4207895084707571884L;
		DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();

		CellRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object object, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (object instanceof Color) {
				setBackground((Color) object);
				return this;
			} else {
				return defaultTableCellRenderer.getTableCellRendererComponent(
						table, object, isSelected, hasFocus, row, column);
			}
		}
	}
	
	public void setGameWorld(GameWorld gameWorld, StatisticsLogger logger) {
		this.gameWorld = gameWorld;
		this.logger = logger;
	}
	
	/**
	 * Removes everything from the frame
	 */
	public void destroyContents(){
		frame.getContentPane().removeAll();
	}
	
	
	@Override
	public String toString() {
		return statisticsTableModel.toString();
	}
	
}
