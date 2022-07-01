
package com.redisfront.ui.frame;

import com.formdev.flatlaf.extras.FlatSVGUtils;
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
public class RedisFrontFrame extends JXFrame {

    public RedisFrontFrame() {
        super(" RedisFront ", true);
        setIconImages(FlatSVGUtils.createWindowIconImages("/svg/redis.svg"));
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
