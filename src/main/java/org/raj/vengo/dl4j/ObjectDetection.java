package org.raj.vengo.dl4j;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

public class ObjectDetection extends JFrame {

	private static final long serialVersionUID = -9145376575902730219L;
	private DaemonThread myThread = null;

	private javax.swing.JButton startButton;
	private javax.swing.JButton pauseButton;
	private javax.swing.JPanel jPanel1;

	VideoCapture capture = null;
	Mat frame = new Mat();

	class DaemonThread implements Runnable {

		protected volatile boolean runnable = false;

		@Override
		public void run() {

			ObjectPrediction tinyYoloPrediction = ObjectPrediction.getINSTANCE();
			synchronized (this) {
				while (runnable) {
					if (capture.grab()) {
						try {
							capture.retrieve(frame);
							Graphics graphics = jPanel1.getGraphics();

							tinyYoloPrediction.markWithBoundingBox(frame, graphics, true, "Test Window");
							MatOfByte mem = new MatOfByte();
							Imgcodecs.imencode(".png", frame, mem);
							BufferedImage buff = ImageIO.read(new ByteArrayInputStream(mem.toArray()));
							graphics.drawImage(buff, 0, 0, getWidth(), getHeight(), 0, 0, buff.getWidth(),
									buff.getHeight(), null);

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * Creates new form FaceDetection
	 */
	public ObjectDetection() {
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {

		jPanel1 = new javax.swing.JPanel();
		startButton = new javax.swing.JButton();
		pauseButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 0, Short.MAX_VALUE));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGap(0, 512, Short.MAX_VALUE));

		startButton.setText("Start");
		startButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				startButtonActionPerformed(evt);
			}
		});

		pauseButton.setText("Pause");
		pauseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pauseButtonActionPerformed(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addGap(24)
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addContainerGap())
				.addGroup(layout.createSequentialGroup().addGap(255).addComponent(startButton).addGap(86)
						.addComponent(pauseButton).addContainerGap(258, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(startButton).addComponent(pauseButton))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		pack();
	}

	private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// stop thread
		myThread.runnable = false;
		// activate start button
		pauseButton.setEnabled(false);
		// deactivate stop button
		startButton.setEnabled(true);
		// stop caturing fron cam
		capture.release();

	}

	private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {

		// video capture from default cam
		capture = new VideoCapture(0);
		// video capture from video file
		//String videoFileName = "resources/sample-video.mp4";
		// capture = new VideoCapture(videoFileName);

		// create object of threat class
		myThread = new DaemonThread();
		Thread t = new Thread(myThread);
		t.setDaemon(true);
		myThread.runnable = true;
		// start thrad
		t.start();
		// deactivate start button
		startButton.setEnabled(false);
		// activate stop button
		pauseButton.setEnabled(true);
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		// Load the opencv_java411.dll
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.setProperty("org.apache.logging.log4j.simplelog.StatusLogger.level", "INFO");

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			Logger.getLogger(ObjectDetection.class.getName()).log(Level.SEVERE, null, ex);
		}

		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new ObjectDetection().setVisible(true);
			}
		});
	}

}
