import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.Semaphore;

import javax.swing.*;

@SuppressWarnings("serial")
public class CollisionsGUI extends JFrame {
	public static Boolean overlaped = new Boolean(false);
	public static Boolean collision = new Boolean(false);
	public static Integer totalCollision = new Integer(0);
	public static JLabel numCollisionsResult;
	public static JLabel timeResult;
	public static boolean collisionEnd;
	public static JTextField numWorkersInput = new JTextField("");
	public static JTextField numBodiesInput = new JTextField("");
	public static JTextField bodySizeInput = new JTextField("");
	public static JTextField iterationInput = new JTextField("");
	public static JTextField timeStepInput = new JTextField("");
	public static JButton startButton = new JButton("start");
	public static JButton stopButton = new JButton("stop");
	public static JButton clearButton = new JButton("clear");
	public static long startTime, endTime;
	public static int iteration = 0;
	public static JCheckBoxMenuItem checkSeed = new JCheckBoxMenuItem("isSeed");
	public static boolean isSeed = false;

	private int mass = 100000000;
	private int numWorkers;
	private int numBodies;
	private int bodySize;
	private double timeStep;
	private JPanel inputPanel, resultPanel, viewPanel;
	private ButtonListener buttonListener;
	private GraphicalView view;

	private ArrayList<Body> others;
	private double[] bouding;
	private Random rand;
	private Thread[] worker;

	public static void main(String[] args) {
		CollisionsGUI gui = new CollisionsGUI();
		gui.setupLayout();
		gui.setVisible(true);
	}

	public CollisionsGUI() {

	}

	public void setupLayout() {
		inputPanel = new JPanel();
		resultPanel = new JPanel();
		viewPanel = new JPanel();
		buttonListener = new ButtonListener();
		view = new GraphicalView();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1800, 1020);
		setLocation(0, 0);
		setTitle("collisions");
		this.setLayout(null);

		inputPanel.setSize(1440, 20);
		inputPanel.setLocation(0, 950);
		inputPanel.setLayout(new GridLayout(1, 12, 1, 1));
		inputPanel.add(new JLabel("numWorkers", SwingConstants.CENTER));
		inputPanel.add(numWorkersInput);
		inputPanel.add(new JLabel("numBodies", SwingConstants.CENTER));
		inputPanel.add(numBodiesInput);
		inputPanel.add(new JLabel("bodySize", SwingConstants.CENTER));
		inputPanel.add(bodySizeInput);
		inputPanel.add(new JLabel("iteration", SwingConstants.CENTER));
		inputPanel.add(iterationInput);
		inputPanel.add(new JLabel("timeStep", SwingConstants.CENTER));
		inputPanel.add(timeStepInput);
		inputPanel.add(checkSeed);
		startButton.addActionListener(buttonListener);
		inputPanel.add(startButton);
		stopButton.addActionListener(buttonListener);
		inputPanel.add(stopButton);
		clearButton.addActionListener(buttonListener);
		inputPanel.add(clearButton);
		stopButton.setEnabled(false);
		clearButton.setEnabled(false);

		resultPanel.setSize(360, 20);
		resultPanel.setLocation(1440, 950);
		resultPanel.setLayout(new GridLayout(2, 1, 1, 1));

		numCollisionsResult = new JLabel("number of collision: 0", SwingConstants.CENTER);
		timeResult = new JLabel("0 microseconds", SwingConstants.CENTER);
		resultPanel.add(timeResult);
		resultPanel.add(numCollisionsResult);

		viewPanel.setSize(1800, 950);
		viewPanel.setLocation(0, 0);

		view.setBackground(Color.white);
		view.setPreferredSize(new Dimension(1800, 950));
		viewPanel.add(view);

		add(viewPanel);
		add(resultPanel);
		add(inputPanel);

	}

	private void start() {
		collisionEnd = false;
		int xPos = 0;
		int yPos = 0;
		int listID = 0;
		int listSize = (int) numBodies / numWorkers;
		others = new ArrayList<>();
		bouding = new double[4];
		bouding[0] = 0;
		bouding[1] = 1790;
		bouding[2] = 0;
		bouding[3] = 950;

		/* create the semaphore list */
		Semaphore finish[][] = null;
		finish = new Semaphore[(int) Math.ceil((Math.log(numWorkers) / Math.log(2)))][numWorkers];
		for (int i = 0; i < finish.length; i++) {
			for (int j = 0; j < finish[i].length; j++)
				finish[i][j] = new Semaphore(0);
		}

		rand = new Random();
		worker = new Thread[numWorkers];
		ArrayList<Body> list = null;
		if (isSeed) {
			rand.setSeed(1);
		}
		for (int i = 0; i < numBodies; i++) {
			boolean flag = false;
			if (isSeed) {
				xPos = bodySize * 2 + Math.abs(rand.nextInt()) % (1790 - bodySize * 4);
				yPos = bodySize * 2 + Math.abs(rand.nextInt()) % (950 - bodySize * 4);
			} else {
				xPos = bodySize * 2 + rand.nextInt(1790 - bodySize * 4);
				yPos = bodySize * 2 + rand.nextInt(950 - bodySize * 4);
			}
			for (Body pt : others) {
				if (pt.distance(xPos, yPos) <= bodySize * 2) {
					flag = true;
					break;
				}
			}
			if (flag) {
				i--;
				continue;
			}
			Body planet = new Body(i, bodySize, mass, xPos, yPos, timeStep, bouding);
			others.add(planet);
			if (i % listSize == 0 && listID < numWorkers) {
				list = new ArrayList<>();
				Worker temp = new Worker(listID, numWorkers, finish, list, others, iteration, view);
				temp.addObserver(view);
				worker[listID] = new Thread(temp, listID + "");
				listID++;
			}
			list.add(planet);
		}

		view.initialBodyColor(others);

		for (int i = 0; i < numWorkers; i++) {
			worker[i].start();
		}

	}

	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == startButton) {
				/* receive input from text field */
				numWorkers = Integer.parseInt(numWorkersInput.getText());
				numBodies = Integer.parseInt(numBodiesInput.getText());
				bodySize = Integer.parseInt(bodySizeInput.getText());
				if (!iterationInput.getText().equals(""))
					iteration = Integer.parseInt(iterationInput.getText());
				timeStep = Double.parseDouble(timeStepInput.getText());
				isSeed = checkSeed.isSelected();
				/* check input */
				if (numWorkers <= 0 || numBodies <= 0 || bodySize <= 0 || iteration < 0 || timeStep <= 0) {
					JOptionPane.showMessageDialog(null, "invalid input");
				} else {
					totalCollision = 0;
					startTime = System.currentTimeMillis();
					start();
					numWorkersInput.setEditable(false);
					numBodiesInput.setEditable(false);
					bodySizeInput.setEditable(false);
					iterationInput.setEditable(false);
					timeStepInput.setEditable(false);
					checkSeed.setEnabled(false);
					startButton.setEnabled(false);
					stopButton.setEnabled(true);
				}
			}
			if (event.getSource() == stopButton) {
				collisionEnd = true;
				stopButton.setEnabled(false);
				clearButton.setEnabled(true);
			}
			if (event.getSource() == clearButton) {
				iteration = 0;
				numWorkersInput.setEditable(true);
				numBodiesInput.setEditable(true);
				bodySizeInput.setEditable(true);
				iterationInput.setEditable(true);
				timeStepInput.setEditable(true);
				checkSeed.setEnabled(true);
				startButton.setEnabled(true);
				clearButton.setEnabled(false);
				numCollisionsResult.setText("number of collision: 0");
				timeResult.setText("0 microseconds");
				view.clear();
				for (int i = 0; i < numWorkers; i++) {
					try {
						worker[i].join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}
	}
}
