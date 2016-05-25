import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ClientApp {
    public static void main(String[] args) {
        String serverIp;
        String amberIp;
        int port = 3737;

        Scanner keyboard = new Scanner(System.in);
        System.out.print("Server IP: ");
        String input = keyboard.nextLine();

        if ("".equals(input)) {
            serverIp = "127.0.0.1";
        } else {
            String[] parts = input.split(":");
            serverIp = parts[0];
            if (parts.length > 1) {
                port = Integer.parseInt(parts[1]);
            }
        }

        System.out.print("Amber IP: ");
        input = keyboard.nextLine();

        if ("".equals(input)) {
            amberIp = "127.0.0.1";
        } else {
            amberIp = input;
        }
        TimerTask roboclawTask = new RoboclawTask(amberIp, serverIp, port);
        Timer timer = new Timer(true);
        timer.schedule(roboclawTask, 0, 250);
    }
}
