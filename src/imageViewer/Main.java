package imageViewer;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.drew.metadata.Tag;

public class Main implements ActionListener, Runnable, MouseListener,
		MouseMotionListener, MouseWheelListener {
	JFrame frame;

	JMenuBar menuBar;

	JScrollPane scrollPane;
	JLabel label;
	ScaleIcon imageIcon;

	Point dragStart;
	double scale = 1;
	static double scaleIncrement = 0.1;

	JPanel panel;
	JButton exif, prev, slideShow, next, fullscreen;

	JFileChooser fileChooser;
	File[] files;
	int index;

	Thread threadSlideShow;
	boolean flagSlideShow;
	static int DELAY_SLIDESHOW = 1000;

	void initMenu() {
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		// File
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);
		// Open
		JMenuItem openItem = new JMenuItem("Open", KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		menu.add(openItem);
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int o = fileChooser.showOpenDialog(frame);
				if (o == JFileChooser.APPROVE_OPTION) {
					files = fileChooser.getSelectedFile().listFiles(
							new FilenameFilter() {
								String[] suf = { ".PNG", ".GIF", ".JPG" };

								public boolean accept(File dir, String name) {
									name = name.toUpperCase();
									for (int i = 0; i < suf.length; i++)
										if (name.endsWith(suf[i]))
											return true;
									return false;
								}
							});
					if (files.length > 0) {
						index = 0;
						showPicture();
					}
				}
			}
		});
		// Exit
		JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_E);
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				ActionEvent.CTRL_MASK));
		menu.add(exitItem);
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});

		// Help
		JMenu help = new JMenu("Help");
		help.setMnemonic(KeyEvent.VK_H);
		menuBar.add(help);
		// About
		JMenuItem about = new JMenuItem("About", KeyEvent.VK_A);
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
				ActionEvent.CTRL_MASK));
		help.add(about);
		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(frame,
						"CopyRight 2014 LiJiancheng", "About",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	void initScrollPanel() {
		label = new JLabel();
		label.setHorizontalAlignment(JLabel.CENTER);
		scrollPane = new JScrollPane(label);

		scrollPane.addMouseListener(this);
		scrollPane.addMouseMotionListener(this);
		scrollPane.addMouseWheelListener(this);

		frame.getContentPane().add(scrollPane, "Center");
	}

	void initPanel() {
		panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		exif = new JButton("Exif");
		prev = new JButton("<-");
		slideShow = new JButton("Play");
		next = new JButton("->");
		fullscreen = new JButton("Full Screen");

		panel.add(exif);
		panel.add(prev);
		panel.add(slideShow);
		panel.add(next);
		panel.add(fullscreen);

		exif.addActionListener(this);
		prev.addActionListener(this);
		slideShow.addActionListener(this);
		next.addActionListener(this);

		fullscreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (fullscreen.getText() == "Full Screen") {
					frame.getGraphicsConfiguration().getDevice()
							.setFullScreenWindow(frame);
					menuBar.setVisible(false);
					fullscreen.setText("Normal Screen");
				} else {
					frame.getGraphicsConfiguration().getDevice()
							.setFullScreenWindow(null);
					menuBar.setVisible(true);
					fullscreen.setText("Full Screen");
				}
			}
		});

		frame.getContentPane().add(panel, "South");
	}

	void prev() {
		index = --index < 0 ? files.length - 1 : index;
		showPicture();
	}

	void next() {
		index = ++index > files.length - 1 ? 0 : index;
		showPicture();
	}

	void startSlideShow() {
		flagSlideShow = true;
	}

	void stopSlideShow() {
		flagSlideShow = false;
	}

	void showPicture() {
		if (files == null || files.length == 0)
			return;
		long startTime = System.currentTimeMillis();
		imageIcon = new ScaleIcon(files[index].getAbsolutePath());
		label.setIcon(imageIcon);
		// System.out.println(file[index].getAbsolutePath());
		long endTime = System.currentTimeMillis();
		System.out.println("Run time�� " + (endTime - startTime) + "ms");
	}

	void showExif() {
		String fileName = files[index].getAbsolutePath();
		Collection<Tag> tags = MetaDataReader.getTags(fileName);
		String result = "";
		if (tags == null) {
			result = "No exif!";
		} else {
			Iterator<Tag> iter = tags.iterator();
			while (iter.hasNext()) {
				Tag tag = (Tag) iter.next();
				result += tag + "\n";
			}
		}
		JOptionPane.showMessageDialog(frame, result, "Exif - " + files[index],
				JOptionPane.INFORMATION_MESSAGE);
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (files == null || files.length == 0) {
			return;
		}
		if (src == exif) {
			showExif();
		} else if (src == slideShow) {
			if (slideShow.getText() == "Play") {
				startSlideShow();
				slideShow.setText("Stop");
			} else {
				stopSlideShow();
				slideShow.setText("Play");
			}
		} else if (src == prev) {
			prev();
		} else if (src == next) {
			next();
		}
	}

	public void run() {
		while (true) {
			if (files != null && files.length > 0 && flagSlideShow) {
				try {
					Thread.sleep(DELAY_SLIDESHOW);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				next();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		if (imageIcon == null) {
			return;
		}
		dragStart = new Point(event.getX(), event.getY());
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		if (imageIcon == null) {
			return;
		}
		Point dragEnd = new Point(event.getX(), event.getY());

		JViewport parent = (JViewport) label.getParent();

		int x = label.getX() + (dragEnd.x - dragStart.x);
		x = Math.max(x, parent.getExtentSize().width
				- label.getIcon().getIconWidth());
		x = Math.min(x, 0);

		int y = label.getY() + (dragEnd.y - dragStart.y);
		y = Math.max(y, parent.getExtentSize().height
				- label.getIcon().getIconHeight());
		y = Math.min(y, 0);

		label.setLocation(x, y);
		// System.out.println(x + "  " + y);
		label.repaint();

		dragStart = dragEnd;
	}

	@Override
	public void mouseMoved(MouseEvent event) {
	}

	@Override
	public void mouseExited(MouseEvent event) {
	}

	@Override
	public void mouseClicked(MouseEvent event) {
	}

	@Override
	public void mouseEntered(MouseEvent event) {
	}

	@Override
	public void mouseReleased(MouseEvent event) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		if (imageIcon == null) {
			return;
		}
		double scaleRatio = scale;

		JViewport parent = (JViewport) label.getParent();

		scale -= event.getWheelRotation() * scaleIncrement;
		scale = Math.min(scale, 5.0);
		scale = Math.max(
				scale,
				Math.min(
						parent.getExtentSize().height * 1.0
								/ imageIcon.getIconOriginalHeight(), 1.0));
		scale = Math.max(
				scale,
				Math.min(
						parent.getExtentSize().width * 1.0
								/ imageIcon.getIconOriginalWidth(), 1.0));
		// System.out.println(scale);

		imageIcon.setScale(scale);

		scaleRatio = scale / scaleRatio;

		Rectangle viewRect = parent.getViewRect();
		parent.setViewPosition(new Point(
				(int) ((viewRect.x + viewRect.width / 2) * scaleRatio - viewRect.width / 2),
				(int) ((viewRect.y + viewRect.height / 2) * scaleRatio - viewRect.height / 2)));

		label.repaint();
	}

	public Main() {
		frame = new JFrame("ImageViewer");

		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		initMenu();
		initScrollPanel();
		initPanel();

		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(3);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		threadSlideShow = new Thread(this);
		threadSlideShow.start();
	}

	public static void main(String[] args) {
		new Main();
	}

}