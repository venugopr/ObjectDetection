package org.raj.vengo.dl4j;

import java.awt.Graphics;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.zoo.model.TinyYOLO;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class ObjectPrediction {

	private ComputationGraph preTrained;
	private List<DetectedObject> predictedObjects;
	private HashMap<Integer, String> map;

	private ObjectPrediction() {
		try {
			// Using IMAGENET as pretrained model which is default
			preTrained = (ComputationGraph) TinyYOLO.builder().build().initPretrained();
			prepareLabels();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static final ObjectPrediction INSTANCE = new ObjectPrediction();

	public static ObjectPrediction getINSTANCE() {
		return INSTANCE;
	}

	public ComputationGraph getPretrainedObjects() {
		return this.preTrained;
	}

	public void markWithBoundingBox(Mat frame, Graphics graphics, boolean newBoundingBOx, String winName)
			throws Exception {
		int width = 416;
		int height = 416;
		int gridWidth = 13;
		int gridHeight = 13;
		double detectionThreshold = 0.5;
		double imageWidth = frame.size().width;
		double imageHeight = frame.size().height;

		Yolo2OutputLayer outputLayer = (Yolo2OutputLayer) preTrained.getOutputLayer(0);
		if (newBoundingBOx) {
			INDArray indArray = prepareImage(frame, width, height);
			INDArray results = preTrained.outputSingle(indArray);
			predictedObjects = outputLayer.getPredictedObjects(results, detectionThreshold);
			markWithBoundingBox(frame, gridWidth, gridHeight, imageWidth, imageHeight);
		} else {
			markWithBoundingBox(frame, gridWidth, gridHeight, imageWidth, imageHeight);
		}
	}

	private INDArray prepareImage(Mat file, int width, int height) throws IOException {
		NativeImageLoader loader = new NativeImageLoader(height, width, 3);
		ImagePreProcessingScaler imagePreProcessingScaler = new ImagePreProcessingScaler(0, 1);
		INDArray indArray = loader.asMatrix(file);
		imagePreProcessingScaler.transform(indArray);
		return indArray;
	}

	private void prepareLabels() {
		if (map == null) {
			String s = "aeroplane\n" + "bicycle\n" + "bird\n" + "boat\n" + "bottle\n" + "bus\n" + "car\n" + "cat\n"
					+ "chair\n" + "cow\n" + "diningtable\n" + "dog\n" + "horse\n" + "motorbike\n" + "person\n"
					+ "pottedplant\n" + "sheep\n" + "sofa\n" + "train\n" + "tvmonitor";
			String[] split = s.split("\\n");
			int i = 0;
			map = new HashMap<>();
			for (String s1 : split) {
				map.put(i++, s1);
			}
		}
	}

	private void markWithBoundingBox(Mat file, int gridWidth, int gridHeight, double imageWidth, double imageHeight) {

		if (predictedObjects == null) {
			return;
		}
		ArrayList<DetectedObject> detectedObjects = new ArrayList<>(predictedObjects);

		while (!detectedObjects.isEmpty()) {
			Optional<DetectedObject> max = detectedObjects.stream()
					.max((o1, o2) -> ((Double) o1.getConfidence()).compareTo(o2.getConfidence()));
			if (max.isPresent()) {
				DetectedObject maxObjectDetect = max.get();
				removeObjectsIntersectingWithMax(detectedObjects, maxObjectDetect);
				detectedObjects.remove(maxObjectDetect);
				markWithBoundingBox(file, gridWidth, gridHeight, imageWidth, imageHeight, maxObjectDetect);
			}
		}
	}

	private static void removeObjectsIntersectingWithMax(ArrayList<DetectedObject> detectedObjects,
			DetectedObject maxObjectDetect) {
		double[] bottomRightXY1 = maxObjectDetect.getBottomRightXY();
		double[] topLeftXY1 = maxObjectDetect.getTopLeftXY();
		List<DetectedObject> removeIntersectingObjects = new ArrayList<>();
		for (DetectedObject detectedObject : detectedObjects) {
			double[] topLeftXY = detectedObject.getTopLeftXY();
			double[] bottomRightXY = detectedObject.getBottomRightXY();
			double iox1 = Math.max(topLeftXY[0], topLeftXY1[0]);
			double ioy1 = Math.max(topLeftXY[1], topLeftXY1[1]);

			double iox2 = Math.min(bottomRightXY[0], bottomRightXY1[0]);
			double ioy2 = Math.min(bottomRightXY[1], bottomRightXY1[1]);

			double inter_area = (ioy2 - ioy1) * (iox2 - iox1);

			double box1_area = (bottomRightXY1[1] - topLeftXY1[1]) * (bottomRightXY1[0] - topLeftXY1[0]);
			double box2_area = (bottomRightXY[1] - topLeftXY[1]) * (bottomRightXY[0] - topLeftXY[0]);

			double union_area = box1_area + box2_area - inter_area;
			double iou = inter_area / union_area;

			if (iou > 0.5) {
				removeIntersectingObjects.add(detectedObject);
			}

		}
		detectedObjects.removeAll(removeIntersectingObjects);
	}

	private void markWithBoundingBox(Mat file, int gridWidth, int gridHeight, double imageWidth, double imageHeight,
			DetectedObject obj) {

		double[] xy1 = obj.getTopLeftXY();
		double[] xy2 = obj.getBottomRightXY();
		int predictedClass = obj.getPredictedClass();
		int x1 = (int) Math.round(imageWidth * xy1[0] / gridWidth);
		int y1 = (int) Math.round(imageHeight * xy1[1] / gridHeight);
		int x2 = (int) Math.round(imageWidth * xy2[0] / gridWidth);
		int y2 = (int) Math.round(imageHeight * xy2[1] / gridHeight);
		Imgproc.rectangle(file, new Point(x1, y1), new Point(x2, y2), new Scalar(0, 255, 0), 2);

		Imgproc.putText(file, map.get(predictedClass), new Point(x1 + 2, y2 - 2), Imgproc.FONT_HERSHEY_PLAIN, 2,
				new Scalar(0, 0, 255));
	}

}