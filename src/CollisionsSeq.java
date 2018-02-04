import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class CollisionsSeq {

	private int numBodies = 0, bodySize = 0, collisionNum = 0;
	private double timeStep = 0.0;
	private ArrayList<Body> list = new ArrayList<>();
	private boolean overlaped = false, collision = false;
	private long nanoTime = 0;

	public CollisionsSeq(int nb, int bz, double ts) {
		numBodies = nb;
		bodySize = bz;
		timeStep = ts;
	}

	public void start() {
		double[] bouding = new double[4];
		bouding[0] = 0;
		bouding[1] = 20000;
		bouding[2] = 0;
		bouding[3] = 20000;
		int xPos = 0, yPos = 0;

		Random rand = new Random(1);
		for (int i = 0; i < numBodies; i++) {
			// Initialize the position of planet, and make sure each of them not
			// be out of bound and not be overlapped
			boolean flag = false;
			xPos = bodySize * 2 + Math.abs(rand.nextInt()) % (1790 - bodySize * 4);
			yPos = bodySize * 2 + Math.abs(rand.nextInt()) % (950 - bodySize * 4);
			for (Body pt : list) {
				if (pt.distance(xPos, yPos) <= bodySize * 2) {
					flag = true;
					break;
				}
			}
			if (flag) {
				i--;
				continue;
			}
			// set up ever planet
			Body planet = new Body(i, bodySize, Collisions.mass, xPos, yPos, timeStep, bouding);
			list.add(planet);
		}

		nanoTime = System.nanoTime();
		for (int i = 0; i < 10000; i++) {
			overlaped = false;
			collision = false;
			for (Body planet : list)
				planet.calculateForce(list);
			for (Body planet : list)
				planet.calculateVelocity(list);
			for (Body planet : list)
				overlaped |= planet.overlap(list);
			if (overlaped) {
				for (Body planet : list)
					planet.reset();
				continue;
			}
			for (Body planet : list)
				collision |= planet.calculateCollision(list);
			if (collision) {
				collisionNum = 0;
			}
			for (Body planet : list)
				planet.calculateBounding();
			for (Body planet : list)
				planet.calculatePosition();
			if (collision) {
				for (Body planet : list)
					collisionNum += planet.getCollisionNum();
			}
		}
		nanoTime = System.nanoTime() - nanoTime;

		String str = new String();
		for(Body pt : list)
			str += pt + "\n";

		try (Writer writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream("finalBodyStatus.txt"), "utf-8"))) {
			writer.write(str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.printf(
				"Report\nThe Number of Threads:\t\t1\nThe Number of Planets:\t\t%d\nSize:\t\t\t\t%d\nMass:\t\t\t\t%d\nTime Stamp:\t\t\t%f\nThe Number of Collision:\t%d\nTotal Time Cost:\t\t%d seconds,%d microseconds\n",
				numBodies, bodySize, Collisions.mass, timeStep, collisionNum, TimeUnit.NANOSECONDS.toSeconds(nanoTime),
				+TimeUnit.NANOSECONDS.toMicros(nanoTime));
	}
}
