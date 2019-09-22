This application uses IMAGENET model, pretrained TinyYOLO and deeplearning4j to do object detection.

Steps to configure the application
1. 	Configure eclipse to use opencv with java. I have used opencv-411 jar but the steps are similar.
	https://docs.opencv.org/2.4/doc/tutorials/introduction/java_eclipse/java_eclipse.html
2.	Download the project
3. 	Import the project as maven project
4.	Add the opencv library configured in step 1.
5.	Run maven build which will download the dependencies
6 	Run ObjectDetection.java file
7.	By default, the application is set to use the webcam.
	To test with your own video, comment the line 137 in ObjectDetection.java
	capture = new VideoCapture(0);
	and uncomment lines 139 an 140 in ObjectDetection.java. Give the video file path in line 145.

References

Thanks to the below people for providing the sample implementation. 

http://ramok.tech/tag/real-time-video-object-detection/

https://github.com/tahaemara/real-time-face-detection-using-opencv-with-java

Linkedin Profile
https://www.linkedin.com/in/rajesh-venugopal-37786a11/
