import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import javax.swing.*;

@SuppressWarnings("serial")
public class GraphicalView extends JPanel implements Observer {
	private boolean startPaint;
	private ArrayList<Body> list;
	private ArrayList<Color> colors;
	private double radius;

	public GraphicalView() {
		startPaint = false;
	}

	public void initialBodyColor(ArrayList<Body> list) {
		startPaint = true;
		this.list = list;
		colors = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Random rand = new Random();
			float r = rand.nextFloat();
			float g = rand.nextFloat();
			float b = rand.nextFloat();
			colors.add(new Color(r, g, b));
		}
		radius = list.get(0).getRadius();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		repaint();
	}

	public void clear() {
		startPaint = false;
		repaint();
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		super.paintComponent(g2);
		// g2.setColor(Color.BLACK);

		// Ellipse2D.Double shape2 = new Ellipse2D.Double(450, 450, 50, 50);
		// g2.draw(shape2);
		/* Enable anti-aliasing and pure stroke */
		// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		// g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
		// RenderingHints.VALUE_STROKE_PURE);

		if (startPaint) {
			// g2.setColor(Color.BLACK);
			CollisionsGUI.numCollisionsResult.setText("number of collision: " + CollisionsGUI.totalCollision / 2);
			CollisionsGUI.endTime = System.currentTimeMillis();
			CollisionsGUI.timeResult.setText((CollisionsGUI.endTime - CollisionsGUI.startTime) + " microseconds");
			for (int i = 0; i < list.size(); i++) {
				Body temp = list.get(i);
				// g2.setColor(colors.get(i));
				double[] position = temp.getPosition();

				Point2D center = new Point2D.Float((float) position[0], (float) position[1]);
				float radius = (float) this.radius;
				float[] dist = { 0.0f, 1.0f };
				Color[] color = { Color.WHITE, colors.get(i) };
				RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, color);

				g2.setPaint(p);
				Ellipse2D.Double shape = new Ellipse2D.Double(position[0] - radius, position[1] - radius,
						temp.getBodySize() * 2, temp.getBodySize() * 2);
				g2.fill(shape);
				g2.draw(shape);
			}
		}
		// start = false;
	}
}
