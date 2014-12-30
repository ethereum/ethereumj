package org.ethereum.gui;

import org.ethereum.geo.IpGeoDB;
import org.ethereum.net.eth.StatusMessage;
import org.ethereum.net.p2p.HelloMessage;
import org.ethereum.util.Utils;

import com.maxmind.geoip.Location;

import java.net.InetAddress;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 * @author Roman Mandeleil
 * @since 25.04.14
 */
public class PeersTableModel extends AbstractTableModel {

    private List<PeerInfo> peerInfoList = new ArrayList<>();
    Timer updater = new Timer();

    public PeersTableModel() {
        updater.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                updateModel();
                            }
                        }
                );
			}
		}, 0, 100);
	}

    public String getColumnName(int column) {
        if (column == 0) return "Location";
        if (column == 1) return "IP";
        if (column == 2) return "Environment";
        if (column == 3) return "Live";
        else return "";
    }

    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public Class<?> getColumnClass(int column) {
        if (column == 0) return ImageIcon.class;
        if (column == 1) return String.class;
        if (column == 2) return String.class;
        if (column == 3) return ImageIcon.class;
        else return String.class;
    }

    public Object getValueAt(int row, int column) {

        PeerInfo peerInfo = peerInfoList.get(row);

        if (column == 0) {
            String countryCode = peerInfo.getLocation().countryCode;

            ImageIcon flagIcon = null;
            if (countryCode != null) {
                URL flagURL = ClassLoader.getSystemResource("flags/" + countryCode.toLowerCase() + ".png");
                flagIcon = new ImageIcon(flagURL);
            }
            return flagIcon;
        }
        if (column == 1)
            return peerInfo.getIp().getHostAddress();

        if (column == 2) {

            if (peerInfo.getLastAccessed() == 0)
                return "?";
            else
                return (System.currentTimeMillis() - peerInfo.getLastAccessed()) / 1000 + " seconds ago";
        }

        if (column == 3) {

            ImageIcon flagIcon = null;
            if (peerInfo.isConnected()) {
                flagIcon = Utils.getImageIcon("connected.png");
            } else {
                flagIcon = Utils.getImageIcon("disconnected.png");
            }
            return flagIcon;
        } else return "";
    }

    public int getRowCount() {
        return this.peerInfoList.size();
    }

    public int getColumnCount() {
        return 4;
    }

    public void updateModel() {
        synchronized (peerInfoList) {
            peerInfoList.clear();

            final Set<org.ethereum.net.peerdiscovery.PeerInfo> peers = UIEthereumManager.ethereum.getPeers();

            synchronized (peers) {

                for (org.ethereum.net.peerdiscovery.PeerInfo peer : peers) {
                    InetAddress addr = peer.getAddress();
                    Location cr = IpGeoDB.getLocationForIp(addr);
                    peerInfoList.add(new PeerInfo(cr, addr, peer.isOnline(), peer.getLastCheckTime(),
                            peer.getHandshakeHelloMessage(), peer.getStatusMessage()));
                }
            }
        }
    }

    public PeerInfo getPeerInfo(int index) {
        return peerInfoList.get(index);
    }


    public class PeerInfo {

        Location location;
        InetAddress ip;
        boolean connected;
        long lastAccessed = 0;

        HelloMessage handshakeHelloMessage;
        StatusMessage handshakeStatusMessage;

        private PeerInfo(Location location, InetAddress ip,
                         boolean isConnected, long lastAccessed, HelloMessage helloMessage, StatusMessage
                statusMessage) {

            if (location == null)
                this.location = new Location();
            else
                this.location = location;

            this.ip = ip;
            this.connected = isConnected;
            this.lastAccessed = lastAccessed;
            this.handshakeHelloMessage = helloMessage;
            this.handshakeStatusMessage = statusMessage;
        }

        private Location getLocation() {
            return location;
        }

        private InetAddress getIp() {
            return ip;
        }

        private boolean isConnected() {
            return connected;
        }

        public long getLastAccessed() {
            return lastAccessed;
        }

        @Override
        public String toString() {
            return "PeerInfo{" +
                    "location=" + location.countryName +
                    ", geo= [ " + location.latitude + "/" + location.longitude + " ]" +
                    ", ip=" + ip +
                    ", connected=" + connected +
                    ", lastAccessed=" + lastAccessed +
                    '}' + "\n -->" + (handshakeHelloMessage == null ? "" : handshakeHelloMessage.toString())
                    + "\n -->" + (handshakeStatusMessage == null ? "" : handshakeStatusMessage.toString());
        }
    }
}


