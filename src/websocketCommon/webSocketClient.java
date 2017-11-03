package websocketCommon;


import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ProgressBar;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;

@ClientEndpoint
public class webSocketClient {
    public Session userSession = null;
    private ProgressBar progress=null;
    long total=0;

    private MessageHandler messageHandler= null;

    public webSocketClient(URI endpointUri, ObservableList list){
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this,endpointUri);

        } catch (DeploymentException e) {
            list.add("connection error");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session userSession, Throwable t){
        System.out.println("error\n"+t.getMessage());
    }
    @OnOpen
    public void onOpen(Session userSession)
    {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession,CloseReason closeReason){
        System.out.println("closing socket");
        this.userSession = null;

    }
    public void setProgressBar(ProgressBar progress,long total){
        this.progress = progress;
        this.total = total;
    }
    public void sendMessage(String message,double cur){
        Future<Void> deliveryTracker = this.userSession.getAsyncRemote().sendText(message);
        if(deliveryTracker.isDone()){
            Platform.runLater(()-> progress.setProgress(cur/total));

        }


    }

    @OnMessage
    public void onMessage(String message){
       System.out.println("message recieved!! \n"+message);
    }

}

