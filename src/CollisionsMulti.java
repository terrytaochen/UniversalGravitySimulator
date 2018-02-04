
/*
 *Author: Tao Chen
 *Date: Nov 1, 2017
 *Description: This program will simulate collisions
 */
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class CollisionsMulti {

	private int numWorkers = 0, numBodies = 0, bodySize = 0, collisions = 0;
	private long nanoTime = 0, barrierTime = 0;
	private double timeStep = 0;
	private boolean isSeed = true;
	private ArrayList<Body> others = new ArrayList<>();
	private Thread[] worker;
	private Worker temp = null;

	public CollisionsMulti(int nw, int nb, int bz, double ts) {
		numWorkers = nw;
		numBodies = nb;
		bodySize = bz;
		timeStep = ts;
	}
	
	public void start(){
		// initialize mult-thread
		ArrayList<Body> list = new ArrayList<>();
		worker = new Thread[numWorkers];
		double[] bouding = new double[4];
		bouding[0] = 0;
		bouding[1] = 20000;
		bouding[2] = 0;
		bouding[3] = 20000;
		int xPos = 0, yPos = 0;
		int listID = 0;
		int listSize = (int) numBodies / numWorkers;

		// set up semaphore used for barriers
		Semaphore finish[][] = null;
		finish = new Semaphore[(int) Math.ceil((Math.log(numWorkers) / Math.log(2)))][numWorkers];
		for (int i = 0; i < finish.length; i++) {
			for (int j = 0; j < finish[i].length; j++)
				finish[i][j] = new Semaphore(0);
		}

		Random rand = new Random();
		if (isSeed) {
			rand.setSeed(1);
		}
		for (int i = 0; i < numBodies; i++) {
			// Initialize the position of planet, and make sure each of them not
			// be out of bound and not be overlapped
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
			// set up ever planet
			Body planet = new Body(i, bodySize, Collisions.mass, xPos, yPos, timeStep, bouding);
			others.add(planet);
			// divided planets into every threads
			if (i % listSize == 0 && listID < numWorkers) {
				list = new ArrayList<>();
				temp = new Worker(listID, numWorkers, finish, list, others, 10000, null);
				worker[listID] = new Thread(temp, listID + "");
				listID++;
			}
			list.add(planet);
		}

		// start timing
		nanoTime = System.nanoTime();
		// start ever thread
		for (int i = 0; i < numWorkers; i++) {
			worker[i].start();
		}
		// wait for every thread terminating
		for (int i = 0; i < numWorkers; i++) {
			try {
				worker[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Calculate execution duration
		nanoTime = System.nanoTime() - nanoTime;
		barrierTime = temp.getBarrierTime();
		// Calculate collision numbers
		String str = new String();
		for (Body pt : others) {
			collisions += pt.getCollisionNum();
			str += pt + "\n";
		}
		try (Writer writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream("finalBodyStatus.txt"), "utf-8"))) {
				writer.write(str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Print out the results
		System.out.printf(
				"Report\nThe Number of Threads:\t\t%d\nThe Number of Planets:\t\t%d\nSize:\t\t\t\t%d\nMass:\t\t\t\t%d\nTime Stamp:\t\t\t%f\nThe Number of Collision:\t%d\nBarrier Cost:\t\t\t%d seconds,%d microseconds\nTotal Time Cost:\t\t%d seconds,%d microseconds\nBarrier Time / Totaol Time:\t%.2f%s\n",
				numWorkers, numBodies, bodySize, Collisions.mass, timeStep, collisions,
				TimeUnit.NANOSECONDS.toSeconds(barrierTime), +TimeUnit.NANOSECONDS.toMicros(barrierTime),
				TimeUnit.NANOSECONDS.toSeconds(nanoTime), +TimeUnit.NANOSECONDS.toMicros(nanoTime),
				barrierTime * 100.0 / nanoTime, "%");
	}
}
