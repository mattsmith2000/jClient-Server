import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class Client extends Application {

    private Text text;
    private TextArea textArea;
    private TextField textField;
    private HBox hBox;

    private String protocol;
    private String serverName;
    private String userMessage;
    private int counter;
    private boolean firstTime;

    private Socket connectTCP;
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;
    private byte[] input;
    private byte[] output;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        BorderPane borderPane = new BorderPane();

        hBox = new HBox();

        text = new Text("Type \"--quit\" to quit the application.");
        hBox.setPadding(new Insets(5, 5, 5, 5));
        hBox.getChildren().add(text);
        borderPane.setTop(hBox);

        textArea = new TextArea("TCP or UDP?");
        borderPane.setCenter(textArea);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.textProperty().addListener(new ChangeListener() {
            public void changed(ObservableValue ov, Object oldValue, Object newValue) {
                textArea.setScrollTop(Double.MAX_VALUE);
            }

        });

        firstTime = true;
        textField = new TextField("Write messages here...");
        textField.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (firstTime) {
                    textField.clear();
                    firstTime = false;
                }
            }
        });
        borderPane.setBottom(textField);
        textField.requestFocus();

        borderPane.setPadding(new Insets(5, 5, 5, 5));

        counter = 0;

        primaryStage.setTitle("Client Application");
        primaryStage.setScene(new Scene(borderPane, 400, 250));
        primaryStage.show();

        textField.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent key)
            {
                if (key.getCode().equals(KeyCode.ENTER))
                {
                    userMessage = textField.getText();
                    boolean isEnd = false;

                    if (userMessage.compareTo("--quit") == 0) {
                        Platform.exit();
                        isEnd = true;
                    }

                    if (!isEnd) {

                        if (counter == 0) {
                            protocol = userMessage;
                            textField.clear();
                            textArea.appendText("\nYou have chosen the following protocol: " + protocol);
                            textArea.appendText("\nPlease provide the name of the Server to which you want to connect: ");
                            counter++;
                        } else if (counter == 1) {
                            serverName = userMessage;
                            textField.clear();
                            textArea.appendText("\nServer Name: " + serverName);
                            textArea.appendText("\nType a message to send and press ENTER to submit:");
                            counter++;
                        } else if (counter == 2) {
                            textArea.appendText("\n\nYour message: " + userMessage);
                            textField.clear();

                            if (protocol.compareTo("TCP") == 0) {
                                runTCP();
                            } else if (protocol.compareTo("UDP") == 0) {
                                try {
                                    runUDP();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void runUDP() throws IOException {

        datagramSocket = new DatagramSocket();

        output = userMessage.getBytes();
        datagramPacket = new DatagramPacket(output, output.length, InetAddress.getLocalHost(), 9876);
        textArea.appendText("\nEstablishing UDP connection with Server " + datagramPacket.getAddress().getHostName() + " at IP address " +
                    datagramPacket.getAddress().getHostAddress() + " and Port " + datagramPacket.getPort() + "...");
        datagramSocket.send(datagramPacket);
        textArea.appendText("\nMessage sent.");
        input = new byte[2000];

        final DatagramPacket finalPacket = datagramPacket;

        Platform.runLater(() -> {
            try {
                datagramSocket.receive(finalPacket);
                textArea.appendText("\nReceiving communications from Server " + datagramPacket.getAddress().getHostName() + " at IP address " +
                    datagramPacket.getAddress().getHostAddress() + " and Port " + datagramPacket.getPort() + ".");
                textArea.appendText("\nServer says: " + new String(finalPacket.getData(), 0, finalPacket.getLength()));
                textArea.appendText("\nType a message to send and press ENTER to submit:");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void runTCP() {
        try {
            connectTCP = new Socket(serverName, 9000);
            textArea.appendText("\nEstablishing TCP connection with Server " + connectTCP.getLocalAddress().getHostName() + " at IP address " +
                    connectTCP.getLocalAddress().getHostAddress() + " and Port " + connectTCP.getPort() + "...");
            fromServer = new DataInputStream(connectTCP.getInputStream());
            toServer = new DataOutputStream(connectTCP.getOutputStream());
            toServer.writeUTF(userMessage);
            textArea.appendText("\nMessage sent.");
            textArea.appendText("\nReceiving communications from Server " + connectTCP.getLocalAddress().getHostName() + " at IP address " +
                    connectTCP.getLocalAddress().getHostAddress() + " and Port " + connectTCP.getPort() + ".");
            textArea.appendText("\nServer says: " + fromServer.readUTF());
            connectTCP.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        textArea.appendText("\nType a message to send and press ENTER to submit:");
    }
}
