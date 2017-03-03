package org.ethereum.crypto.jce;

import org.spongycastle.jcajce.provider.config.ConfigurableProvider;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;

public final class SpongyCastleProvider {

  private static class Holder {
    private static final Provider  INSTANCE = new BouncyCastleProvider();
    static{
    	INSTANCE.put("MessageDigest.ETH-KECCAK-256", "org.ethereum.crypto.cryptohash.Keccak256");
    	INSTANCE.put("MessageDigest.ETH-KECCAK-512", "org.ethereum.crypto.cryptohash.Keccak512");
    }
  }

  public static Provider getInstance() {
    return Holder.INSTANCE;
  }
}
