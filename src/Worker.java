
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.Semaphore;

public class Worker extends Observable implements Runnable {
	private Semaphore finish[][];
	private int threadNum, index, iteration;
	private ArrayList<Body> list, others;
	private long duration = 0, total = 0;

	public Worker(int index, int threadNum, Semaphore[][] sem, ArrayList<Body> list, ArrayList<Body> others,
			int iteration, GraphicalView view) {
		finish = sem;
		this.list = list;
		this.others = others;
		this.index = index;
		this.threadNum = threadNum;
		this.iteration = iteration;

	}

	public void run() {

		for (int i = 0; !CollisionsGUI.collisionEnd && !(i > iteration && iteration != 0); i++) {
			duration = System.nanoTime();
			barrier();
			total += System.nanoTime() - duration;
			if (index == 0) {
				CollisionsGUI.overlaped = false;
				CollisionsGUI.collision = false;
			}
			this.setChanged();
			this.notifyObservers();
			for (Body planet : list)
				planet.calculateForce(others);
			duration = System.nanoTime();
			barrier();
			total += System.nanoTime() - duration;
			for (Body planet : list)
				planet.calculateVelocity(others);
			duration = System.nanoTime();
			barrier();
			total += System.nanoTime() - duration;
			for (Body planet : list) {
				synchronized (CollisionsGUI.overlaped) {
					CollisionsGUI.overlaped |= planet.overlap(others);
				}
			}
			duration = System.nanoTime();
			barrier();
			total += System.nanoTime() - duration;
			if (CollisionsGUI.overlaped) {
				System.out.println(list.get(0).getTimeStamp());
				for (Body planet : list)
					planet.reset();
				continue;
			}
			duration = System.nanoTime();
			barrier();
			total += System.nanoTime() - duration;
			for (Body planet : list) {
				synchronized (CollisionsGUI.collision) {
					CollisionsGUI.collision |= planet.calculateCollision(others);
				}
			}
			duration = System.nanoTime();
			barrier();
			total += System.nanoTime() - duration;
			if (CollisionsGUI.collision) {
				CollisionsGUI.totalCollision = 0;
			}
			for (Body planet : list)
				planet.calculateBounding();
			duration = System.nanoTime();
			barrier();
			total += System.nanoTime() - duration;
			for (Body planet : list)
				planet.calculatePosition();
			duration = System.nanoTime();
			barrier();
			total += System.nanoTime() - duration;
			if (CollisionsGUI.collision) {
				synchronized (CollisionsGUI.totalCollision) {
					for (Body planet : list)
						CollisionsGUI.totalCollision += planet.getCollisionNum();
				}
			}
		}
	}

	private void barrier() {
		int stage_t = (int) Math.ceil((Math.log(threadNum) / Math.log(2)));
		for (int stage = 0, next = 1; stage < stage_t; stage++, next *= 2) {
			finish[stage][index].release();
			try {
				finish[stage][(index + next) % threadNum].acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	
	public long getBarrierTime(){
		return total;
	}
}
