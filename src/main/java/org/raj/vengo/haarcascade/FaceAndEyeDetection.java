package org.raj.vengo.haarcascade;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class FaceAndEyeDetection {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {

		String imageFile = "Passport.jpg";
		String haarcascadeFace = "C:/Work/opencv/build/etc/haarcascades/haarcascade_frontalface_alt2.xml";
		String haarcascadeEye = "C:/Work/opencv/build/etc/haarcascades/haarcascade_eye.xml";
//		if (args.length < 3) {
//			System.out.println("Pass the below 3 arguments ");
//			System.out.println("args[0] - file path of image having a face");
//			System.out.println("args[1] - haarcascade_frontalface_alt2.xml file path");
//			System.out.println("args[2] - haarcascade_eye.xml file path");
//			return;
//		}
//
//		imageFile = args[0];
//		haarcascadeFace = args[1];
//		haarcascadeEye = args[2];

		// read the image
		Mat img = Imgcodecs.imread(imageFile);

		Mat grayFrame = new Mat();

		int absoluteFaceSize = 0;

		// convert the frame in gray scale
		Imgproc.cvtColor(img, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, grayFrame);

		// compute minimum face size (20% of the frame height, in our case)
		if (absoluteFaceSize == 0) {
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0) {
				absoluteFaceSize = Math.round(height * 0.2f);
			}
		}

		// Setup face detection. Give the full path of
		// haarcascade_frontalface_alt2.xml as arg[1]
		// e.g.
		// "C:/Work/Softwares/opencv/build/etc/haarcascades/haarcascade_frontalface_alt2.xml"
		CascadeClassifier faceCascade = new CascadeClassifier(haarcascadeFace);

		// Setup eye detection. Give the full path of
		// haarcascade_frontalface_alt2.xml as arg[2]
		// e.g.
		// "C:/Work/Softwares/opencv/build/etc/haarcascades/haarcascade_eye.xml"
		CascadeClassifier eyeCascade = new CascadeClassifier(haarcascadeEye);
		MatOfRect faces = new MatOfRect();
		// detect faces
		faceCascade.detectMultiScale(img, faces);

		// each rectangle in faces is a face
		Rect[] facesArray = faces.toArray();
		for (Rect face : facesArray) {
			Imgproc.rectangle(img, face.tl(), face.br(), new Scalar(0, 255, 0), 3);
			if (eyeCascade != null) {
				MatOfRect eyes = new MatOfRect();
				Mat roiImageGray = new Mat(grayFrame, face);
				// equalize the frame histogram to improve the result
				Imgproc.equalizeHist(roiImageGray, roiImageGray);
				Mat roiImageColour = new Mat(img, face);
				// detect eyes
				// compute minimum eye size (20% of the roi image height)
				int eyeSize = Math.round(roiImageGray.rows() * 0.2f);
				if (eyeSize > 0) {
					eyeCascade.detectMultiScale(roiImageGray, eyes, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
							new Size(eyeSize, eyeSize), new Size());

					// each rectangle in eyes is an eye!
					Rect[] eyesArray = eyes.toArray();
					for (Rect eye : eyesArray) {
						// Imgproc.rectangle(roiImageColour, eye.tl(), eye.br(),
						// new
						// Scalar(0, 0, 255), 2);
						int radius = (eye.width > eye.height ? eye.width / 2 : eye.height / 2);
						Point center = new Point((eye.x + (eye.x + eye.width)) / 2, (eye.y + (eye.y + eye.height)) / 2);
						Imgproc.circle(roiImageColour, center, radius, new Scalar(0, 0, 255), 2);
					}
				}
			}
		}
		// save result
		Imgcodecs.imwrite("face&eye.png", img);
	}
}