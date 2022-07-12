
package com.redisfront.ui.frame;

import com.redisfront.commons.constant.UI;
import com.redisfront.ui.component.MainMenuBar;
import com.redisfront.ui.form.MainWindowForm;
import org.jdesktop.swingx.JXFrame;

import javax.swing.*;
import java.awt.*;

/**
 * RedisFrontFrame
 *
 * @author Jin
 */
public class RedisFrontMainFrame extends JXFrame {

    public RedisFrontMainFrame() {
        super("RedisFront", true);
        setIconImages(UI.MAIN_FRAME_ICON_IMAGES);
        UIManager.put("TitlePane.unifiedBackground", false);
        setJMenuBar(MainMenuBar.getInstance());
        initComponents();
    }

    private void initComponents() {
        var container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(MainWindowForm.getInstance().getContentPanel(), BorderLayout.CENTER);
    }
}
