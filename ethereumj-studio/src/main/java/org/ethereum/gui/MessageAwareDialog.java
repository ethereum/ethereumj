package org.ethereum.gui;

/**
 * This interface describes the methods required 
 * for any dialog that displays info- and alert status messages.
 */
public interface MessageAwareDialog {

    public void infoStatusMsg(final String text);

    public void alertStatusMsg(final String text);
}
