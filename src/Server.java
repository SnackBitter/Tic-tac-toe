import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Server {

    static ServerSocket player_1;
    static ServerSocket player_2;

    static ServerSocket player_3;

    static ServerSocket player_4;

    private static int[][] chessBoard = new int[3][3];

    private static int[][] chessBoard1 = new int[3][3];
    private static boolean turn = true;

    private static boolean turn1 = true;
    private static boolean isTied = false;

    public Server() throws IOException {
        player_1 = new ServerSocket(1345);
        player_2 = new ServerSocket(6890);
        player_3 = new ServerSocket(2456);
        player_4 = new ServerSocket(5789);
    }

    public synchronized static void main(String[] args) throws IOException {
        Server server = new Server();
        Thread room1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Socket client1;
                try {
                    client1 = player_1.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Player1 connected");
                sendToClient(client1, "Waiting for another player");
                Socket client2;
                try {
                    client2 = player_2.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Player2 connected");
                sendToClient(client1, "Game Start!");
                sendToClient(client2, "Game Start!");
                Thread thread1 = new Thread(() -> {
                    try {
                        while (true) {
                            Thread.sleep(1000);
                            client1.sendUrgentData(0xFF);
                            client1.setSoTimeout(10000);
                        }
                    } catch (IOException | InterruptedException e) {
                        sendToClient(client2, "Another player is disconnected");
                    }
                });
                Thread thread2 = new Thread(() -> {
                    try {
                        while (true) {
                            Thread.sleep(1000);
                            client2.sendUrgentData(0xFF);
                            client2.setSoTimeout(10000);
                        }
                    } catch (IOException | InterruptedException e) {
                        sendToClient(client1, "Another player is disconnected");
                    }
                });

                try {
                    thread1.start();
                    thread2.start();
                    while (true) {
                        if (turn) {
                            receiveFromClient(client1);
                            if (checkIfWin(chessBoard)) {
                                sendToClient(client1, "Circle Wins!!");
                                sendToClient(client2, "Circle Wins!!");
                                break;
                            }
                            if (isTied) {
                                sendToClient(client1, "Tied");
                                sendToClient(client2, "Tied");
                                break;
                            }
                            turn = !turn;
                            sendToClient(client2, "");
                        } else {
                            receiveFromClient(client2);
                            if (checkIfWin(chessBoard)) {
                                sendToClient(client1, "Line Wins!!");
                                sendToClient(client2, "Line Wins!!");
                                break;
                            }
                            if (isTied) {
                                sendToClient(client1, "Tied");
                                sendToClient(client2, "Tied");
                                break;
                            }
                            turn = !turn;
                            sendToClient(client1, "");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    thread1.interrupt();
                    thread2.interrupt();
                    try {
                        client1.close();
                        client2.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        Thread room2 = new Thread(new Runnable() {
            @Override
            public void run() {
                Socket client1;
                try {
                    client1 = player_3.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Player3 connected");
                sendToClient1(client1, "Waiting for another player");
                Socket client2;

                try {
                    client2 = player_4.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Player4 connected");
                sendToClient1(client1, "Game Start!");
                sendToClient1(client2, "Game Start!");
                Thread thread1 = new Thread(() -> {
                    try {
                        while (true) {
                            Thread.sleep(1000);
                            client1.sendUrgentData(0xFF);
                            client1.setSoTimeout(10000);
                        }
                    } catch (SocketTimeoutException e) {
                        sendToClient1(client2, "Another player is disconnected");
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                Thread thread2 = new Thread(() -> {
                    try {
                        while (true) {
                            Thread.sleep(1000);
                            client2.sendUrgentData(0xFF);
                            client2.setSoTimeout(10000);
                        }
                    } catch (SocketTimeoutException e) {
                        sendToClient1(client1, "Another player is disconnected");
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

                try {
                    thread1.start();
                    thread2.start();
                    while (true) {
                        if (turn1) {
                            receiveFromClient1(client1);
                            if (checkIfWin(chessBoard1)) {
                                sendToClient1(client1, "Circle Wins!!");
                                sendToClient1(client2, "Circle Wins!!");
                                break;
                            }
                            if (isTied) {
                                sendToClient1(client1, "Tied");
                                sendToClient1(client2, "Tied");
                                break;
                            }
                            turn1 = !turn1;
                            sendToClient1(client2, "");
                        } else {
                            receiveFromClient1(client2);
                            if (checkIfWin(chessBoard1)) {
                                sendToClient1(client1, "Line Wins!!");
                                sendToClient1(client2, "Line Wins!!");
                                break;
                            }
                            if (isTied) {
                                sendToClient1(client1, "Tied");
                                sendToClient1(client2, "Tied");
                                break;
                            }
                            turn1 = !turn1;
                            sendToClient1(client1, "");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    thread1.interrupt();
                    thread2.interrupt();
                    try {
                        client1.close();
                        client2.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        room1.start();
        room2.start();
    }

    private static void sendToClient(Socket client, String message) {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                data.append(chessBoard[i][j]);
                data.append(" ");
            }
            data.append("\n");
        }
        data.append(turn).append("\n").append(message).append("\n").append("bye").append("\n");
        try {
            client.getOutputStream().write(data.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendToClient1(Socket client, String message) {
        StringBuilder data = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                data.append(chessBoard1[i][j]);
                data.append(" ");
            }
            data.append("\n");
        }
        data.append(turn1).append("\n").append(message).append("\n").append("bye").append("\n");
        try {
            client.getOutputStream().write(data.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void receiveFromClient(Socket client) {
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        try {
            inputStream = client.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String data;
            int count = 0;
            while (!((data = bufferedReader.readLine()).contains("bye"))) {
                String[] temp = data.split(" ");
                if (count < 3) {
                    for (int i = 0; i < 3; i++) {
                        chessBoard[count][i] = Integer.parseInt(temp[i]);
                    }
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void receiveFromClient1(Socket client) {
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        try {
            inputStream = client.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String data;
            int count = 0;
            while (!((data = bufferedReader.readLine()).contains("bye"))) {
                String[] temp = data.split(" ");
                if (count < 3) {
                    for (int i = 0; i < 3; i++) {
                        chessBoard1[count][i] = Integer.parseInt(temp[i]);
                    }
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean checkIfWin(int[][] chessBoard) {
        //判断列
        for (int i = 0; i < 3; i++) {
            if (chessBoard[i][0] == chessBoard[i][1] && chessBoard[i][1] == chessBoard[i][2]
                && chessBoard[i][0] != 0) {
                return true;
            }
        }
        //判断行
        for (int i = 0; i < 3; i++) {
            if (chessBoard[0][i] == chessBoard[1][i] && chessBoard[1][i] == chessBoard[2][i]
                && chessBoard[0][i] != 0) {
                return true;
            }
        }
        //判断斜线
        if (chessBoard[0][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[2][2]
            && chessBoard[0][0] != 0) {
            return true;
        }
        //判断反斜线
        if (chessBoard[2][0] == chessBoard[1][1] && chessBoard[1][1] == chessBoard[0][2]
            && chessBoard[1][1] != 0) {
            return true;
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (chessBoard[i][j] == 0) {
                    return false;
                }
            }
        }
        isTied = true;
        return false;
    }
}
