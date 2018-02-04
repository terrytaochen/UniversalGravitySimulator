
public class Collisions {
	static final int mass = 10000000;

	public static void main(String[] args) {
		int numThreads = 1, numBodies = 0, bodySize = 0, type = 0;
		double timeStep = 0.0;
		/* get valid arguments */
		if (args.length == 5) {
			numThreads = Integer.parseInt(args[0]);
			numBodies = Integer.parseInt(args[1]);
			bodySize = Integer.parseInt(args[2]);
			timeStep = Double.parseDouble(args[3]);
			type = Integer.parseInt(args[4]);
		} else {
			System.err.println("Usage: java collisions numThreads numBodies bodySize numTimeStep Type");
			System.exit(0);
		}
		if (numBodies <= 0 || bodySize <= 0 || timeStep <= 0) {
			System.err.println("invalid input");
			System.exit(0);
		}
		switch (type) {
		case 0:
			CollisionsSeq cs = new CollisionsSeq(numBodies, bodySize, timeStep);
			cs.start();
			break;
		case 1:
			CollisionsMulti cm = new CollisionsMulti(numThreads, numBodies, bodySize, timeStep);
			cm.start();
			break;
		case 2:
			CollisionsGUI gui = new CollisionsGUI();
			gui.setupLayout();
			gui.setVisible(true);
			break;
		default:
			System.err.println("Type has to be 0 - 2:\n0 : Squential\n1 : MultiThread\n2 : GUI");
		}
	}
}
