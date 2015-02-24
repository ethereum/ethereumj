package org.ethereum.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.ethereum.config.KeysDefaultsConstants.*;

public final class CLIInterfaceConfig {
    public static class Frozen {
	Map<String,Object> map;

	private Frozen( Map<String,Object> map ) { this.map = Collections.unmodifiableMap( map ); }

	public Map<String,Object> getMap() { return this.map; }
    }

    String  activePeerIP   = null;
    Integer activePeerPort = null;
    String  dataBaseDir    = null;
    Boolean databaseReset  = null;
    Integer listenPort     = null;

    public void setActivePeerIP(String host)         { this.activePeerIP   = host; }
    public void setActivePeerPort(int port)          { this.activePeerPort = port; } 
    public void setDataBaseDir(String dir)           { this.dataBaseDir    = dir; } 
    public void setDatabaseReset(boolean reset)      { this.databaseReset  = reset; }
    public void setListenPort(int port)              { this.listenPort     = port; }

    public Frozen freeze() {
	Map<String,Object> map = new HashMap<>();

	if ( activePeerIP != null )   map.put( K_PEER_ACTIVE_IP,   activePeerIP   );
	if ( activePeerPort != null ) map.put( K_PEER_ACTIVE_PORT, activePeerPort );
	if ( dataBaseDir != null)     map.put( K_DATABASE_DIR,     dataBaseDir    );
	if ( databaseReset != null )  map.put( K_DATABASE_RESET,   databaseReset  );
	if ( listenPort != null )     map.put( K_PEER_LISTEN_PORT, listenPort     );

	return new Frozen( map );
    }
}

