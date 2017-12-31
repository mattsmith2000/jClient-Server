import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.io.DataInputStream;
import java.io.IOException;

public class Server extends Application {

    protected TextArea textArea;

    public static void main(String[] args) throws SocketException, UnknownHostException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane borderPane = new BorderPane();
        textArea = new TextArea("WELCOME TO SERVER APPLICATION");
        textArea.appendText("\nSetting up TCP/IP and UDP threaded listeners...");
        textArea.setEditable(false);
        borderPane.setCenter(textArea);
        textArea.setWrapText(true);


        primaryStage.setTitle("Server Application");
        primaryStage.setScene(new Scene(borderPane, 400, 600));
        primaryStage.show();
        
        new Thread(new ListenTCP()).start();
        new Thread(new ListenUDP()).start();
    }

    class ListenTCP implements Runnable {
        public void run(){
            try{
                ServerSocket socket = new ServerSocket(9000);
                textArea.appendText("\nLISTENING FOR TCP CLIENTS...");
                textArea.appendText("\n   IP Address: " + socket.getLocalSocketAddress().toString());
                textArea.appendText("\n   Port: " + socket.getLocalPort());
                while(true){
                    Socket connectToClient = socket.accept();
                    textArea.appendText("\nTCP Client message received.");
                    new Thread(new HandleTCP(connectToClient)).start();
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    class HandleTCP implements Runnable{

        private Socket connectToClient;
        private DataInputStream fromClient;
        private DataOutputStream toClient;

        public HandleTCP(Socket connectToClient) {
            this.connectToClient = connectToClient;
        }

        public void run(){
            try{
                fromClient = new DataInputStream(connectToClient.getInputStream());
                toClient = new DataOutputStream(connectToClient.getOutputStream());
                String clientMessage = new String(fromClient.readUTF());
                textArea.appendText("\n   Client Says: " + clientMessage);
                textArea.appendText("\n   Repying to TCP Client...");
                toClient.writeUTF(clientMessage.toUpperCase());
            } catch(IOException ioe) {
                System.err.println(ioe);
                Platform.exit();
            }
        }
    }

    private class ListenUDP extends Thread {

        DatagramSocket datagramSocket;

        public ListenUDP() throws SocketException, UnknownHostException {
            datagramSocket = new DatagramSocket(9876);
        }

        public void run() {
        	textArea.appendText("\nLISTENING FOR UDP CLIENTS...");
            textArea.appendText("\n   IP Address: " + datagramSocket.getLocalSocketAddress().toString());
            textArea.appendText("\n   Port: " + datagramSocket.getLocalPort());
            while (true) {
                try {
                    DatagramPacket datagramPacket = new DatagramPacket(new byte[2000], 2000);
                    datagramSocket.receive(datagramPacket);
                    textArea.appendText("\nUDP Client message received.");
                    new HandleUDP(datagramSocket, datagramPacket).start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Platform.exit();
                }
            }
        }
    }

    private class HandleUDP extends Thread {

        DatagramSocket datagramSocket;
        DatagramPacket oldPacket;
        String clientMessage;

        public HandleUDP(DatagramSocket datagramSocket, DatagramPacket packet) {
            this.datagramSocket = datagramSocket;
            oldPacket = packet;
            clientMessage = new String(oldPacket.getData());
        }

        public void run() {
            textArea.appendText("\n   Client Says: " + clientMessage);
            byte[] returnMessage = clientMessage.toUpperCase().getBytes();
            DatagramPacket returnPacket = new DatagramPacket(returnMessage, returnMessage.length, oldPacket.getAddress(), oldPacket.getPort());
            try {
                textArea.appendText("\n   Repying to TCP Client...");
                datagramSocket.send(returnPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}