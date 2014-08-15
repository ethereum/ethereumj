package org.ethereum.gui;

import java.net.InetAddress;
import java.net.URL;
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import org.ethereum.db.IpGeoDB;
import org.ethereum.manager.WorldManager;
import org.ethereum.net.client.PeerData;
import org.ethereum.net.message.HelloMessage;
import org.ethereum.util.Utils;

import com.maxmind.geoip.Location;

/**
 * www.ethereumJ.com
 * @author: Roman Mandeleil
 * Created on: 25/04/14 07:04
 */
public class PeersTableModel extends AbstractTableModel {

	private List<PeerInfo> peerInfoList = new ArrayList<PeerInfo>();
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
            if (countryCode != null){
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
        }
        else return "";
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

            List<PeerData> peers = WorldManager.getInstance().getPeers();
            for (int i = 0; i < peers.size(); ++i) {

                PeerData peer = peers.get(i);
                InetAddress addr = peer.getInetAddress();
                Location cr = IpGeoDB.getLocationForIp(addr);
                peerInfoList.add(new PeerInfo(cr, addr, peer.isOnline(), peer.getHandshake(), peer.getLastCheckTime()));
            }
        }
    }

    private class PeerInfo {

        Location         location;
        InetAddress      ip;
        boolean          connected;
        HelloMessage     handshake;
        long lastAccessed = 0;


		private PeerInfo(Location location, InetAddress ip, boolean isConnected,
                         HelloMessage handshake, long lastAccessed) {

            if (location == null)
                this.location = new Location();
            else
                this.location = location;

            this.ip = ip;
            this.connected = isConnected;
            this.handshake = handshake;
            this.lastAccessed = lastAccessed;
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

        public HelloMessage getHandshake() {
            return handshake;
        }

        public long getLastAccessed() {
            return lastAccessed;
        }
    }
}


