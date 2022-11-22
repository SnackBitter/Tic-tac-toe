package application_1.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.text.Text;

public class Controller implements Initializable {
    private static final int PLAY_1 = 1;
    private static final int PLAY_2 = 2;
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;

    private Socket socket = new Socket("127.0.0.1",12345);
    @FXML
    private Text text;

    @FXML
    private Pane base_square;

    @FXML
    private Rectangle game_panel;

    private static boolean TURN = true;

    private static final int[][] chessBoard = new int[3][3];

    private static final boolean[][] flag = new boolean[3][3];

    private static int stage = 0;

    public Controller() throws IOException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        game_panel.setOnMouseClicked(event -> {
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            if((TURN ? PLAY_1 : PLAY_2)==PLAY_2){
                text.setText("Now is not your turn!------Wait for another player");
                stage = 0;
                update();
                return;
            }
            if (refreshBoard(x, y)) {
                TURN = !TURN;
                stage = 1;
                update();
            }
        });
        new AnimationTimer(){
            @Override
            public void handle(long now) {
                drawChess();
            }
        }.start();
    }

    private boolean refreshBoard (int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
            chessBoard[x][y] = TURN ? PLAY_1 : PLAY_2;
            return true;
        }
        return false;
    }

    private void drawChess () {
        for (int i = 0; i < chessBoard.length; i++) {
            for (int j = 0; j < chessBoard[0].length; j++) {
                if (flag[i][j]) {
                    // This square has been drawing, ignore.
                    continue;
                }
                switch (chessBoard[i][j]) {
                    case PLAY_1:
                        drawCircle(i, j);
                        break;
                    case PLAY_2:
                        drawLine(i, j);
                        break;
                    case EMPTY:
                        // do nothing
                        break;
                    default:
                        System.err.println("Invalid value!");
                }
            }
        }
    }

    private void drawCircle (int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine (int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        base_square.getChildren().add(line_a);
        base_square.getChildren().add(line_b);
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        flag[i][j] = true;
    }
    private void sendToServer(){
        StringBuilder data = new StringBuilder();
        for (int i = 0; i<3;i++){
            for (int j = 0; j<3;j++){
                data.append(chessBoard[i][j]);
                data.append(" ");
            }
            data.append("\n");
        }
        try {
            socket.getOutputStream().write(data.toString().getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();
            socket.shutdownOutput();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveFromServer(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream;
                InputStreamReader inputStreamReader;
                BufferedReader bufferedReader;
                try {
                    inputStream = socket.getInputStream();
                    inputStreamReader = new InputStreamReader(inputStream);
                    bufferedReader = new BufferedReader(inputStreamReader);

                    String data;
                    int count = 0;
                    while ((data = bufferedReader.readLine()) != null) {
                        String[] temp = data.split(" ");
                        for (int i = 0; i < 3; i++) {
                            chessBoard[count][i] = Integer.parseInt(temp[i]);
                        }
                    }
                    TURN = !TURN;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void update(){
        switch (stage) {
            case 0:
                receiveFromServer();
                break;
            case 1 :
            {
                sendToServer();
                stage = 0;
                update();
                break;
            }
            default :
                System.out.println("Invalid!!");
                break;
        }
    }
}
