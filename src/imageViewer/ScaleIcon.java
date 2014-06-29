package imageViewer;

import java.awt.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ScaleIcon implements Icon {

	ImageIcon icon = null;
	double scale = 1;

	public ScaleIcon(String string) {
		this.icon = new ImageIcon(string);
	}

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public int getIconOriginalHeight() {
		return icon.getIconHeight();
	}

	public int getIconOriginalWidth() {
		return icon.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return (int) (icon.getIconHeight() * scale);
	}

	@Override
	public int getIconWidth() {
		return (int) (icon.getIconWidth() * scale);
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		g2d.scale(scale, scale);
		icon.paintIcon(c, g2d, x, y);
		// System.out.println(c.getSize().width + " " + c.getSize().height);
		// System.out.println(this.getIconWidth() + ", " +
		// this.getIconHeight());
	}
}
