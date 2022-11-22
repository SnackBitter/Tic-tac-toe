import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Server {

    ServerSocket player_1;
    ServerSocket player_2;

    private static int[][] chessBoard = new int[3][3];
    private static boolean turn = true;

    private static boolean stepIn = false;

    public Server() throws IOException {
        player_1 = new ServerSocket(12345);
        player_2 = new ServerSocket(6789);
    }

    public void CloseServer() throws IOException {
        player_1.close();
        player_2.close();
    }

    public synchronized static void main(String[] args) throws IOException {
        Server server = new Server();
        Socket client1 = server.player_1.accept();
        System.out.println("Player1 connected");
        Socket client2 = server.player_2.accept();
        System.out.println("Player2 connected");

        Thread thread1_1 = new Thread(() -> {
            stepIn = false;
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = client1.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                String data;
                int count = 0;
                while ((data = bufferedReader.readLine()) != null) {
                    String[] temp = data.split(" ");
                    for (int i = 0; i < 3; i++) {
                        chessBoard[count][i] = Integer.parseInt(temp[i]);
                    }
                    count++;
                }
                turn = false;
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        Thread thread2_1 = new Thread(() -> {
            while (turn) {
            }
            StringBuilder data = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    data.append(chessBoard[i][j]);
                    data.append(" ");
                }
                data.append("\n");
            }
            try {
                client2.getOutputStream().write(data.toString().getBytes(StandardCharsets.UTF_8));
                client2.getOutputStream().flush();
                client2.shutdownOutput();
                stepIn = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Thread thread1_2 = new Thread(() -> {
            while (!turn) {
            }
            StringBuilder data = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    data.append(chessBoard[i][j]);
                    data.append(" ");
                }
                data.append("\n");
            }
            try {
                client1.getOutputStream().write(data.toString().getBytes(StandardCharsets.UTF_8));
                client1.getOutputStream().flush();
                client1.shutdownOutput();
                stepIn = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Thread thread2_2 = new Thread(() -> {
            stepIn = false;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            BufferedReader bufferedReader;
            try {
                inputStream = client2.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                String data = null;
                int count = 0;
                while ((data = bufferedReader.readLine()) != null) {
                    String[] temp = data.split(" ");
                    for (int i = 0; i < 3; i++) {
                        chessBoard[count][i] = Integer.parseInt(temp[i]);
                    }
                }
                turn = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        while (true) {
            if (turn) {
                thread1_1.start();
                thread2_1.start();
            } else {
                thread2_2.start();
                thread1_2.start();
            }
            while (!stepIn){
            }
            thread1_1.interrupt();
            thread2_1.interrupt();
            thread1_2.interrupt();
            thread2_2.interrupt();
            outputChess();
        }
    }

    static void outputChess(){
        System.out.println(Arrays.deepToString(chessBoard));
    }
}
