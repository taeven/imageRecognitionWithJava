package uploadServer;


import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class Controller {
    VideoCapture cam = new VideoCapture(0);
    Mat image = new Mat();
    boolean tr = cam.read(image);

}