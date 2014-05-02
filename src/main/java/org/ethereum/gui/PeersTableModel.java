package org.ethereum.gui;

import com.maxmind.geoip.Location;
import com.maxmind.geoip2.model.CityResponse;
import org.ethereum.geodb.IpGeoDB;
import org.ethereum.util.Utils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 25/04/14 07:04
 */
public class PeersTableModel  extends AbstractTableModel {

    List<PeerInfo> peerInfoList = new ArrayList<PeerInfo>();

    public PeersTableModel() {

        generateRandomData();
    }



    public String getColumnName(int column) {

        if (column == 0) return "Location";
        if (column == 1) return "IP";
        if (column == 2) return "Live";
        else return "";
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public Class getColumnClass(int column) {
        if (column == 0) return ImageIcon.class;
        if (column == 1) return String.class;
        if (column == 2) return ImageIcon.class;
        else return String.class;
    }

    public Object getValueAt(int row, int column) {

        PeerInfo peerInfo = peerInfoList.get(row);

        if (column == 0){

            String countryCode = peerInfo.getLocation().countryCode;
            URL flagURL = ClassLoader.getSystemResource("flags/" + countryCode + ".png");
            ImageIcon flagIcon = new ImageIcon(flagURL);


            return flagIcon;
        }

        if (column == 1) return peerInfo.getIp().getHostAddress();
        if (column == 2) {

            Random random = new Random();
            boolean isConnected = random.nextBoolean();

            ImageIcon flagIcon = null;
            if (peerInfo.connected){

                flagIcon = Utils.getImageIcon("connected.png");
            } else {

                flagIcon = Utils.getImageIcon("disconnected.png");
            }


            return flagIcon;
        }

        else return "";
    }

    public int getRowCount() {
        return this.peerInfoList.size();
    }

    public int getColumnCount() {
        return 3;
    }



    // todo: delete it when stabilized
    private void generateRandomData(){

        List<String> ips = new ArrayList<String>();
        ips.add("206.223.168.190");
        ips.add("94.210.200.192");
        ips.add("88.69.198.198");
        ips.add("62.78.198.208");
        ips.add("71.202.162.40");
        ips.add("78.55.236.218");
        ips.add("94.197.120.80");
        ips.add("85.65.126.45");

        ips.add("110.77.217.185");
        ips.add("64.231.9.30");
        ips.add("162.243.203.121");
        ips.add("82.217.72.169");

        ips.add("99.231.80.166");
        ips.add("131.104.252.4");
        ips.add("54.204.10.41");
        ips.add("54.201.28.117");
        ips.add("82.240.16.5");
        ips.add("74.79.23.119");


        for (String peer : ips){

            try {

                InetAddress addr = InetAddress.getByName(peer);
                Location cr = IpGeoDB.getLocationForIp(addr);

                peerInfoList.add(new PeerInfo(cr, addr));

            } catch (UnknownHostException e) {e.printStackTrace(); }
        }
    }

    private class PeerInfo{

        Location         location;
        InetAddress      ip;
        boolean          connected;

        private PeerInfo(Location location, InetAddress ip) {
            this.location = location;
            this.ip = ip;

            Random random = new Random();
            connected = random.nextBoolean();
        }

        private InetAddress getIp() {
            return ip;
        }


        private Location getLocation() {
            return location;
        }

        private boolean isConnected() {
            return connected;
        }
    }
}


