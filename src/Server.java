import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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

  private static boolean isTied1 = false;

  public Server() throws IOException {
    player_1 = new ServerSocket(1345);
    player_2 = new ServerSocket(6890);
    player_3 = new ServerSocket(2456);
    player_4 = new ServerSocket(5789);
  }

  public synchronized static void main(String[] args) throws IOException {
    Server server = new Server();
    Thread room1 = new Thread(() -> {
      try {
        Socket client1;
        client1 = player_1.accept();
        System.out.println("Player1 connected");
        sendToClient(client1, "Waiting for another player");

        Socket client2;
        client2 = player_2.accept();
        System.out.println("Player2 connected");
        sendToClient(client1, "Game Start!");
        sendToClient(client2, "Game Start!");
        Thread thread1 = new Thread(() -> {
          int count = 0;
          while (true) {
            try {
              Thread.sleep(200);
              client1.getOutputStream().write("M\n".getBytes(StandardCharsets.UTF_8));
              count = 0;
            } catch (Exception e) {
              count++;
              if (count > 5) {
                sendToClient(client2, "Another player is disconnected");
              }
            }
          }
        });
        Thread thread2 = new Thread(() -> {
          int count = 0;
          while (true) {
            try {
              Thread.sleep(200);
              client2.getOutputStream().write("M\n".getBytes(StandardCharsets.UTF_8));
              count = 0;
            } catch (Exception e) {
              count++;
              if (count > 5) {
                sendToClient(client1, "Another player is disconnected");
              }
            }
          }
        });

        thread1.start();
        thread2.start();
        while (true) {
          if (turn) {
            receiveFromClient(client1);
            if (checkIfWin(chessBoard, 0)) {
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
            if (checkIfWin(chessBoard, 0)) {
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
        thread1.interrupt();
        thread2.interrupt();
        client1.close();
        client2.close();
      } catch (IOException e) {
        System.out.println("Connect close");
      }
    });

    Thread room2 = new Thread(() -> {
      try {
        Socket client1;
        client1 = player_3.accept();
        System.out.println("Player3 connected");
        sendToClient1(client1, "Waiting for another player");

        Socket client2;
        client2 = player_4.accept();
        System.out.println("Player4 connected");
        sendToClient1(client1, "Game Start!");
        sendToClient1(client2, "Game Start!");
        Thread thread1 = new Thread(() -> {
          int count = 0;
          while (true) {
            try {

              Thread.sleep(200);
              client1.getOutputStream().write("M\n".getBytes(StandardCharsets.UTF_8));
              count = 0;

            } catch (Exception e) {
              count++;
              if (count > 5) {
                sendToClient1(client2, "Another player is disconnected");
              }
            }
          }
        });
        Thread thread2 = new Thread(() -> {
          int count = 0;
          while (true) {
            try {

              Thread.sleep(200);
              client2.getOutputStream().write("M\n".getBytes(StandardCharsets.UTF_8));
              count = 0;

            } catch (Exception e) {
              count++;
              if (count > 5) {
                sendToClient1(client1, "Another player is disconnected");
              }
            }
          }
        });
        thread1.start();
        thread2.start();
        while (true) {

          if (turn1) {
            receiveFromClient1(client1);
            if (checkIfWin(chessBoard1, 1)) {
              sendToClient1(client1, "Circle Wins!!");
              sendToClient1(client2, "Circle Wins!!");
              break;
            }
            if (isTied1) {
              sendToClient1(client1, "Tied");
              sendToClient1(client2, "Tied");
              break;
            }
            turn1 = !turn1;
            sendToClient1(client2, "");
          } else {
            receiveFromClient1(client2);
            if (checkIfWin(chessBoard1, 1)) {
              sendToClient1(client1, "Line Wins!!");
              sendToClient1(client2, "Line Wins!!");
              break;
            }
            if (isTied1) {
              sendToClient1(client1, "Tied");
              sendToClient1(client2, "Tied");
              break;
            }
            turn1 = !turn1;
            sendToClient1(client1, "");
          }
        }
        thread1.interrupt();
        thread2.interrupt();
        client1.close();
        client2.close();

      } catch (IOException e) {
        System.out.println("Connect close");
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
      System.out.println("Connect close");
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
        if (data.equals("M")) {
          continue;
        }
        String[] temp = data.split(" ");
        if (count < 3) {
          for (int i = 0; i < 3; i++) {
            chessBoard[count][i] = Integer.parseInt(temp[i]);
          }
        }
        count++;
      }
    } catch (Exception e) {
      System.out.println("Connect close");
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
        if (data.equals("M")) {
          continue;
        }
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

  static boolean checkIfWin(int[][] chessBoard, int k) {
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
    if (k == 1) {
      isTied1 = true;
    } else {
      isTied = true;
    }
    return false;
  }
}
