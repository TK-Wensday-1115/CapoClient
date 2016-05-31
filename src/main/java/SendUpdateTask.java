import pl.edu.agh.amber.common.AmberClient;
import pl.edu.agh.amber.hokuyo.HokuyoProxy;
import pl.edu.agh.amber.hokuyo.MapPoint;
import pl.edu.agh.amber.hokuyo.Scan;
import pl.edu.agh.amber.ninedof.NinedofData;
import pl.edu.agh.amber.ninedof.NinedofProxy;
import pl.edu.agh.amber.roboclaw.MotorsCurrentSpeed;
import pl.edu.agh.amber.roboclaw.RoboclawProxy;
import pl.edu.agh.student.smialek.tk.communications.client.CommunicationsClient;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class SendUpdateTask extends TimerTask {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter
            .ofLocalizedTime(FormatStyle.LONG)
            .withZone(ZoneId.systemDefault());

    private final CommunicationsClient clientFL;
    private final CommunicationsClient clientFR;
    private final CommunicationsClient clientRL;
    private final CommunicationsClient clientRR;

    private final CommunicationsClient clientAcceleration;
    private final CommunicationsClient clientGyro;
    private final CommunicationsClient clientMagnet;

    private final CommunicationsClient clientLaser;

    private final RoboclawProxy roboclawProxy;
    private final NinedofProxy ninedofProxy;
    private final HokuyoProxy hokuyoProxy;

    public SendUpdateTask(String amberIp, String serverIp, int port) {
        AmberClient client = null;
        try {
            client = new AmberClient(amberIp, 26233);
        } catch (IOException e) {
            System.out.println("Unable to connect to robot: " + e);
        } finally {
            roboclawProxy = new RoboclawProxy(client, 0);
            ninedofProxy = new NinedofProxy(client, 0);
            hokuyoProxy = new HokuyoProxy(client, 0);
        }

        clientFL = new CommunicationsClient(serverIp, port, "roboclawFL");
        clientFR = new CommunicationsClient(serverIp, port, "roboclawFR");
        clientRL = new CommunicationsClient(serverIp, port, "roboclawRL");
        clientRR = new CommunicationsClient(serverIp, port, "roboclawRR");

        clientAcceleration = new CommunicationsClient(serverIp, port, "accel");
        clientGyro = new CommunicationsClient(serverIp, port, "gyro");
        clientMagnet = new CommunicationsClient(serverIp, port, "magnet");

        clientLaser = new CommunicationsClient(serverIp, port, "laser");
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
        MotorsCurrentSpeed motorsSpeed = roboclawProxy.getCurrentMotorsSpeed();
        NinedofData ninedofData = ninedofProxy.getAxesData(true, true, true);
        Scan laserScan = hokuyoProxy.getSingleScan();

        clientFL.sendUpdate(String.valueOf(motorsSpeed.getFrontLeftSpeed()), "#ff8800");
        clientFR.sendUpdate(String.valueOf(motorsSpeed.getFrontRightSpeed()), "#0088ff");
        clientRL.sendUpdate(String.valueOf(motorsSpeed.getRearLeftSpeed()), "#ff4488");
        clientRR.sendUpdate(String.valueOf(motorsSpeed.getRearRightSpeed()), "#2266dd");

        clientAcceleration.sendUpdate(axesString(ninedofData.getAccel()), "#22ff00");
        clientGyro.sendUpdate(axesString(ninedofData.getGyro()), "#00dd00");
        clientMagnet.sendUpdate(axesString(ninedofData.getMagnet()), "#22bb00");

        clientLaser.sendUpdate(pointListString(laserScan.getPoints()), "#ff2200");

        System.out.println("Update sent " + dateFormatter.format(Instant.now()));
    }

    private String axesString(NinedofData.AxesData axesData) {
        return String.format("%d;%d;%d", axesData.xAxis, axesData.yAxis, axesData.zAxis);
    }

    private String pointListString(List<MapPoint> list) {
        if (list.isEmpty()) {
            return "";
        }

        List<String> mapPoints = new ArrayList<>(list.size());
        for (MapPoint point : list) {
            mapPoints.add(mapPointString(point));
        }

        StringBuilder builder = new StringBuilder(mapPoints.get(0));
        mapPoints.remove(0);
        for (String pointString : mapPoints) {
            builder.append("|").append(pointString);
        }

        return builder.toString();
    }

    private String mapPointString(MapPoint mapPoint) {
        return String.format("dist:%f;angle:%f", mapPoint.getDistance(), mapPoint.getAngle());
    }
}
