package application_1.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
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

    @FXML
    private Button Room1;

    @FXML
    private Button Room2;

    @FXML
    private Button Circle;

    @FXML
    private Button Line;

    @FXML
    private Button closeButton;

    @FXML
    private Button startButton;

    private Socket socket;
    @FXML
    private Text text;

    @FXML
    private Pane base_square;

    @FXML
    private Rectangle game_panel;

    private static boolean TURN = true;

    private static boolean serverTurn = false;

    private static final int[][] chessBoard = new int[3][3];

    private static int port = 1234;

    private static final boolean[][] flag = new boolean[3][3];


    private AnimationTimer timer;

    private int room = 0;

    public Controller() throws IOException {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        text.setText("Choose your turn");

        //服务器断开异常抛出
        Thread thread = new Thread(() -> {
            int count = 0;
            while (true) {
                try {
                    Thread.sleep(200);
                    socket.getOutputStream().write("M\n".getBytes(StandardCharsets.UTF_8));
                    count = 0;
                }catch (Exception e){
                    count++;
                    if(count>5) {
                        text.setText("Lost Server");
                        timer.stop();
                    }
                }
            }
        });
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawChess();
            }
        };
        timer.start();
        game_panel.setOnMouseClicked(event -> {
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            if (!(TURN == serverTurn)) {
                text.setText("Now is not your turn!------Wait for another player");
            } else {
                if (refreshBoard(x, y)) {
                    try {
                        sendToServer();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    serverTurn = !serverTurn;
                }
            }
        });

        Room1.setOnMouseClicked(event -> {
            room = 1;
            text.setText("You are in room 1");
        });

        Room2.setOnMouseClicked(event -> {
            room = 2;
            text.setText("You are in room 2");
        });

        startButton.setOnMouseClicked(event -> {
            try {
                socket = new Socket("127.0.0.1", port);
                thread.start();
                receiveFromServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            receiveFromServer();
        });

        closeButton.setOnMouseClicked(event -> {
            try {
                socket.close();
                text.setText("Connect closed");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Circle.setOnMouseClicked(event -> {
            if (room == 1) {
                port = 1345;
                TURN = true;
            } else if (room == 2) {
                port = 2456;
                TURN = true;
            } else {
                text.setText("Choose your room first");
                return;
            }
            text.setText("You are now in Circle");
        });

        Line.setOnMouseClicked(event -> {
            if (room == 1) {
                port = 6890;
                TURN = false;
            } else if (room == 2) {
                port = 5789;
                TURN = false;
            } else {
                text.setText("Choose your room first");
                return;
            }
            text.setText("You are now in Line");
        });
    }

    private boolean refreshBoard(int x, int y) {
        if (chessBoard[x][y] == EMPTY) {
            chessBoard[x][y] = serverTurn ? PLAY_1 : PLAY_2;
            return true;
        }
        return false;
    }

    private void drawChess() {
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

    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        base_square.getChildren().add(circle);
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        flag[i][j] = true;
    }

    private void drawLine(int i, int j) {
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

    private void sendToServer() throws IOException {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                data.append(chessBoard[i][j]);
                data.append(" ");
            }
            data.append("\n");
        }
        data.append("bye").append("\n");
        socket.getOutputStream().write(data.toString().getBytes(StandardCharsets.UTF_8));
        receiveFromServer();
    }

    private void receiveFromServer() {
        Thread thread = new Thread(() -> {
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            BufferedReader bufferedReader;
            try {
                inputStream = socket.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                String data;
                int count = 0;
                while (!((data = bufferedReader.readLine()).contains("bye"))) {
                    if(data.startsWith("M")){
                        continue;
                    }
                    String[] temp = data.split(" ");
                    if (count < 3) {
                        for (int i = 0; i < 3; i++) {
                            chessBoard[count][i] = Integer.parseInt(temp[i]);
                        }
                    }
                    if (count == 3) {
                        serverTurn = data.equals("true");
                    }
                    if (count == 4) {
                        text.setText(data);
                        if (data.contains("Wins")||data.contains("Tied")) {
                            Thread thread1 = new Thread(() -> {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                timer.stop();
                            });
                            thread1.start();
                            socket.close();
                        }
                    }
                    count++;
                }
            } catch (Exception e) {
                System.out.println("Lost Server");
            }
        });
        thread.start();
    }
}
