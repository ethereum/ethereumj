package org.ethereum.crypto.jce;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;
import java.security.Security;

public final class SpongyCastleProvider {

  public static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;

  private static class Holder {
    private static final BouncyCastleProvider INSTANCE;

    static {
      Provider provider = Security.getProvider(PROVIDER_NAME);

      if (provider == null) {
        INSTANCE = new BouncyCastleProvider();
        Security.addProvider(INSTANCE);
      } else {
        assert provider instanceof BouncyCastleProvider;
        INSTANCE = (BouncyCastleProvider) provider;
      }
    }
  }

  public static Provider getInstance() {
    return Holder.INSTANCE;
  }
}
