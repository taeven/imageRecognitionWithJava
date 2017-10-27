package enroll;


import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_32SC1;
import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imread;

public class Controller {

    @FXML
    ImageView imageView ;
    @FXML
    Label captureCount;
    @FXML
    Button captureButton;
    @FXML
    Label status;
    @FXML
    ProgressBar progress;
    @FXML
    Button clearButton;
    @FXML
    Button enrollButton;

    @FXML
    TextField eId;

    private Boolean isSaving;
    private VideoCapture cam;
    private int capturedNo;
    private boolean isCapture;

    private ArrayList<Image> capturedImages;

    public Controller(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        capturedNo =0;
        isSaving = false;
//        progress.setVisible(false);
        capturedImages = new ArrayList<Image>();
        isCapture=false;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                cameraDispImage();
            }
        });
        t.start();

//        trainImageSet();


    }

    private Image matToImageConv(Mat frame){
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", frame, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }

    private void cameraDispImage(){
         cam = new VideoCapture(0);
        Mat colored = new Mat();
        if(!cam.isOpened())
        {
            System.out.println("cannot open camera");

        }
        else{
            System.out.println(" opening camera");
            while (true){
                if(cam.read(colored))
                {

                    Mat bwframe = new Mat();
                    Imgproc.cvtColor(colored,bwframe,Imgproc.COLOR_RGB2GRAY);
                    //function will return the cropped bw image
                    bwframe = drawRactangle(bwframe,colored);

                    Image img = matToImageConv(colored);

                    imageView.setImage(img);
                    //check whether user clicked the capture button
                    if(isCapture){
                        if(bwframe==null)
                        {


                            Platform.runLater(()->{
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Capture Error");
                                alert.setContentText("cannot detect face!! \n Try again!");
                                alert.showAndWait();
                            });

                        }
                        else{
                            Imgproc.equalizeHist(bwframe,bwframe);

                            Image bwimg = matToImageConv(bwframe);

                            capturedNo++;
                            capturedImages.add(bwimg);
                            Platform.runLater(()->captureCount.setText(capturedImages.size()+""));
                        }
                        isCapture=false;
                    }
                }
            }
        }
    }

    public void trainImageSet(){

        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(()->{
                    status.setText("Loading images to train");
                    progress.setVisible(true);
                });
                File folder = new File("Images/");
                //filter to choose only required images
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        name = name.toLowerCase();
                        return name.startsWith("user") && (name.endsWith("jpg")||name.endsWith("bmp")||name.endsWith("png"));
                    }
                };

                File listFiles[] = folder.listFiles(filter);
                List<Mat> images = new ArrayList<Mat>();

                int label[]=new int[listFiles.length];
                int counter =0;

                for(File file:listFiles){
                    final int loc = counter;
                    Platform.runLater(()->{
                        progress.setProgress(loc/listFiles.length);
                    });
                    Mat image = imread(file.getAbsolutePath(),CV_LOAD_IMAGE_GRAYSCALE);
                    //extracting id;
                    label[counter]=Integer.parseInt(file.getName().split("\\.")[1]);
                    images.add(image);
                    counter++;

                }
                Platform.runLater(()->{
                    status.setText(" training data");
                });
                Mat labels = new Mat(listFiles.length, 1, CV_32SC1);
                labels.put(0,0,label);

                FaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
                faceRecognizer.train(images,labels);
                Platform.runLater(()->{
                    progress.setProgress(1);
                });

                faceRecognizer.save("trainedSet.yml");

                Platform.runLater(()->{
                    status.setText(" process done");
                    progress.setVisible(false);
                });


            }
        });
        th.start();



    }
    private Mat drawRactangle(Mat bwframe, Mat colored){
        CascadeClassifier faceCascade = new CascadeClassifier();
        faceCascade.load("cascades/haarcascade_frontalface_default.xml");
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(bwframe, faces, 1.1, 2, Objdetect.CASCADE_SCALE_IMAGE,
                new Size(bwframe.height()*0.2f, bwframe.height()*0.2f), new Size());
        if(!faces.empty()){
            Rect faceArray[]=faces.toArray();

            for (int i = 0; i < faceArray.length; i++){
                Imgproc.rectangle(colored, faceArray[i].tl(), faceArray[i].br(), new Scalar(0, 255, 0), 2);
                Rect crop = new Rect(faceArray[i].tl(),faceArray[i].br());
                bwframe = new Mat(bwframe,crop);

            }


        }
        else{

//                System.out.println("cannot detect face");
                return null;
        }


        return bwframe;
    }

    public void captureClick(){
        if(capturedNo<20)
            isCapture=true;
        else {
            captureButton.setDisable(true);
        }
    }
    public void clearSelection(){
        capturedImages.clear();
        capturedNo=0;
        captureButton.setDisable(false);
        captureCount.setText(capturedImages.size()+"");

    }

    @Override
    protected void finalize() throws Throwable {
        cam.release();
    }


    public void enrollClick(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if(eId.getText().isEmpty())
        {

            alert.setTitle("Incorrect Configuration");
//            alert.setHeaderText("Look, an Information Dialog");
            alert.setContentText("Enter the enrollment ID before hitting enroll!!");

            alert.showAndWait();
        }
        else if(capturedNo!=20){
            alert.setTitle("Selection Error");
            alert.setContentText("capture at least 20 images!!");
            alert.showAndWait();
        }else {
            try{
                int t=Integer.parseInt(eId.getText());
                if(t<0)
                    throw new Exception();
                imagesSaveLocal();
            }catch(Exception ex){
                alert.setTitle("E-ID fromat error");
                alert.setContentText("E-ID should be an unsigned integer!!");
                alert.showAndWait();
            }


        }

    }



    private void imagesSaveLocal(){
        if(!isSaving){
            String eid;
            eid = eId.getText();
            eId.setText("");
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    isSaving = true;



                    int count =0;

                    Platform.runLater(()->{
                        clearButton.setDisable(true);
                        progress.setVisible(true);
                    });

                    while(count<capturedImages.size()){
                        final int loc = count;
                        Platform.runLater(()->{

                            status.setText("saving Images to local" + (double)(loc/19*100)+"%");
                        });
                        //check whether directory of image exists
                        File dir = new File("Images");
                        if(!dir.exists()){
                            dir.mkdir();
                        }
                        File image = new File("Images/user."+eid+"."+count+".jpg");
                        BufferedImage bImage = SwingFXUtils.fromFXImage(capturedImages.get(count), null);
                        try {
                            ImageIO.write(bImage ,"jpg",image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Platform.runLater(()->{
                            progress.setProgress(0.05*loc);
                        });

                        count++;


                    }
                    isSaving = false;


                    System.out.println("finished saving");


                    //calling train function to train the facerecognizer
//                    trainImageSet();
                    Platform.runLater(()->{
                        clearSelection();
                        clearButton.setDisable(false);
                        progress.setVisible(false);
                    });
                }
            });
            th.start();



        }

    }
}