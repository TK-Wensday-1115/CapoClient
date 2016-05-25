import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.roboclaw.MotorsCurrentSpeed;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;
import pl.edu.agh.student.smialek.tk.communications.client.CommunicationsClient;

import java.io.IOException;
import java.util.TimerTask;

public class RoboclawTask extends TimerTask {
    private final CommunicationsClient clientFL;
    private final CommunicationsClient clientFR;
    private final CommunicationsClient clientRL;
    private final CommunicationsClient clientRR;
    private final RoboclawProxy proxy;

    public RoboclawTask(String amberIp, String serverIp, int port) {
        AmberClient client = null;
        try {
            client = new AmberClient(amberIp, 26233);
        } catch (IOException e) {
            System.out.println("Unable to connect to robot: " + e);
        } finally {
            proxy = new RoboclawProxy(client, 0);
        }

        clientFL = new CommunicationsClient(serverIp, port, "roboclawFL");
        clientFR = new CommunicationsClient(serverIp, port, "roboclawFR");
        clientRL = new CommunicationsClient(serverIp, port, "roboclawRL");
        clientRR = new CommunicationsClient(serverIp, port, "roboclawRR");
    }

    @Override
    public void run() {
        try {
            executeTask();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void executeTask() throws Exception {
        MotorsCurrentSpeed motorsSpeed = proxy.getCurrentMotorsSpeed();
        clientFL.sendUpdate(String.valueOf(motorsSpeed.getFrontLeftSpeed()), "#ff8800");
        clientFR.sendUpdate(String.valueOf(motorsSpeed.getFrontRightSpeed()), "#0088ff");
        clientRL.sendUpdate(String.valueOf(motorsSpeed.getRearLeftSpeed()), "#ff2200");
        clientRR.sendUpdate(String.valueOf(motorsSpeed.getRearRightSpeed()), "#0022ff");
        System.out.println("Update sent");
    }
}
