
package org.dromara.redisfront.ui.frame;

import org.dromara.redisfront.commons.constant.UI;
import org.dromara.redisfront.ui.component.MainMenuBar;
import org.dromara.redisfront.ui.form.MainWindowForm;
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
