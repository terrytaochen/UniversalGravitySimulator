import java.util.ArrayList;

public class Body {
	public static final double G = 6.67 * Math.pow(10, -11);

	private int index, collision, size;
	private double timeStamp;
	private double mass;
	private double[] velocity, position, force, _velocity;
	private double[] velocity_, position_, force_;
	private double[] bouding;
	private boolean isHit;

	public Body(int number, int s, double m, double x, double y, double duration, double[] boud) {
		index = number;
		timeStamp = duration;
		isHit = false;
		mass = m;
		size = s;
		velocity = new double[2];
		position = new double[2];
		force = new double[2];
		velocity[0] = 0;
		velocity[1] = 0;
		position[0] = x;
		position[1] = y;
		force[0] = 0;
		force[1] = 0;
		_velocity = new double[2];
		_velocity[0] = 0;
		_velocity[1] = 0;
		velocity_ = new double[2];
		position_ = new double[2];
		force_ = new double[2];
		bouding = boud;
		collision = 0;
	}

	/* calculate sum of the forces */
	public void calculateForce(ArrayList<Body> planets) {
		backUp();
		double distance, magnitude;
		double[] direction = new double[2];
		force[0] = 0;
		force[1] = 0;
		for (Body planet : planets) {
			if (this == planet)
				continue;
			else {
				distance = distance(planet);
				magnitude = G * mass * planet.mass / distance(planet);
				direction[0] = planet.position[0] - position[0];
				direction[1] = planet.position[1] - position[1];
				this.force[0] += magnitude * direction[0] / distance;
				this.force[1] += magnitude * direction[1] / distance;
			}
		}
	}

	/* Based on the sum of force, calculate the final velocity and postion */
	public void calculateVelocity(ArrayList<Body> planets) {
		double[] deltaV = new double[2], deltaP = new double[2];
		deltaV[0] = force[0] / mass * timeStamp;
		deltaV[1] = force[1] / mass * timeStamp;
		deltaP[0] = (velocity[0] + deltaV[0] / 2) * timeStamp;
		deltaP[1] = (velocity[1] + deltaV[1] / 2) * timeStamp;
		_velocity[0] += deltaV[0];
		_velocity[1] += deltaV[1];
		position[0] += deltaP[0];
		position[1] += deltaP[1];
	}

	/* Based on the prediction position, check if overlap happened */
	public boolean overlap(ArrayList<Body> planets) {
		boolean flag = false;
		double[] direction;
		for (Body planet : planets) {
			if (this == planet)
				continue;
			direction = new double[2];
			direction[0] = planet.position[0] - position[0];
			direction[1] = planet.position[1] - position[1];
			// flag |= (direction[0] * velocity[0] < 0) && (velocity[0] *
			// velocity_[0] > 0);
			// flag |= (direction[1] * velocity[1] < 0) && (velocity[1] *
			// velocity_[1] > 0);
			flag |= distance(planet) < (size + planet.size) * 4 / 5.0;
			flag |= position[0] < bouding[0] + size * 4 / 5.0;
			flag |= position[0] > bouding[1] - size * 4 / 5.0;
			flag |= position[1] < bouding[2] + size * 4 / 5.0;
			flag |= position[1] > bouding[3] - size * 4 / 5.0;
		}
		return flag;
	}

	/* Based on the prediction position, check if collision happened */
	public boolean calculateCollision(ArrayList<Body> planets) {
		boolean flag = false;
		double[] __velocity = new double[2];
		__velocity = velocity_;
		for (Body planet : planets) {
			if (this == planet)
				continue;
			else if (distance(planet) >= size + planet.size)
				continue;
			else {
				flag |= true;
				_velocity = velocityAfterCollision(planet);
				// _velocity[1] = velocityAfterCollision(planet, 'Y');
			}
		}
		velocity = _velocity;
		_velocity = __velocity;
		isHit = flag;
		collision += flag ? 1 : 0;
		return flag;
	}

	/* Based on the prediction position, check if hit the bounding */
	public void calculateBounding() {
		if (position[0] < bouding[0] + size || position[0] > bouding[1] - size) {
			velocity[0] = -velocity[0];
			isHit = true;
		}
		if (position[1] < bouding[2] + size || position[1] > bouding[3] - size) {
			velocity[1] = -velocity[1];
			isHit = true;
		}
	}

	/* find real position when collision happened or hit the bounding */
	public void calculatePosition() {
		if (isHit) {
			position[0] += velocity[0] * timeStamp;
			position[1] += velocity[1] * timeStamp;
		}
		isHit = false;
	}

	/* if overlap happened, back to last time stamp */
	public void reset() {
		velocity[0] = velocity_[0];
		velocity[1] = velocity_[1];
		position[0] = position_[0];
		position[1] = position_[1];
		force[0] = force_[0];
		force[1] = force_[1];
		timeStamp = timeStamp / 2.0;
	}

	/* store the initial data used for reset */
	private void backUp() {
		velocity_[0] = velocity[0];
		velocity_[1] = velocity[1];
		position_[0] = position[0];
		position_[1] = position[1];
		force_[0] = force[0];
		force_[1] = force[1];
	}

	public double distance(Body planets) {
		return Math.hypot(this.position[0] - planets.position[0], this.position[1] - planets.position[1]);
	}
	
	public double distance(double x, double y) {
		return Math.hypot(this.position[0] - x, this.position[1] - y);
	}

	public double[] getPosition() {
		return position;
	}

	public int getBodySize() {
		return size;
	}

	private double[] velocityAfterCollision(Body planet) {
		double v2x = planet._velocity[0];
		double v2y = planet._velocity[1];
		double x2 = planet.position[0];
		double y2 = planet.position[1];

		double v1x = _velocity[0];
		double v1y = _velocity[1];
		double x1 = position[0];
		double y1 = position[1];

		double[] v = new double[2];
		v[0] = (v2x * Math.pow(x2 - x1, 2) + v2y * (x2 - x1) * (y2 - y1) + v1x * Math.pow(y2 - y1, 2)
				- v1y * (x2 - x1) * (y2 - y1)) / (Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
		// v[1] = (v2y * Math.pow(y2 - y1, 2) + v2x * (x2 - x1) * (y2 - y1) -
		// v1y * Math.pow(x2 - x1, 2)
		// + v1x * (x2 - x1) * (y2 - y1)) / (Math.pow(x2 - x1, 2) + Math.pow(y2
		// - y1, 2));
		v[1] = (v2x * (x2 - x1) * (y2 - y1) + v2y * Math.pow(y2 - y1, 2) - v1x * (y2 - y1) * (x2 - x1)
				+ v1y * Math.pow(x2 - x1, 2)) / (Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
		return v;
	}

	public int getCollisionNum() {
		return collision;
	}

	@Override
	public String toString() {
		String str = new String();
		str += "Planet #" + index + ":\n";
		str += "Position " + "(x = " + position[0] + ", y = " + position[1] + ")\n";
		str += "Velosity " + "(Vx = " + velocity[0] + ", Vy = " + velocity[1] + ")\n";
		return str;
	}

	public double getTimeStamp() {
		return this.timeStamp;
	}
	
	public double getRadius(){
		return size;
	}

}
