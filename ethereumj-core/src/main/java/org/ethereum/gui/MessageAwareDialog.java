package org.ethereum.gui;

import javax.swing.*;
import java.awt.*;

/**
 * www.ethereumJ.com
 * User: Roman Mandeleil
 * Created on: 26/05/2014 12:29
 */

public interface MessageAwareDialog {
    public void infoStatusMsg(final String text);
    public void alertStatusMsg(final String text);

}
