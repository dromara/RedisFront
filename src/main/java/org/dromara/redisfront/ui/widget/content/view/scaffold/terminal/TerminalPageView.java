package org.dromara.redisfront.ui.widget.content.view.scaffold.terminal;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.redisfront.model.context.ConnectContext;
import org.dromara.redisfront.ui.components.panel.NonePanel;
import org.dromara.redisfront.ui.widget.MainWidget;
import org.dromara.redisfront.ui.widget.content.view.scaffold.index.IndexSearchFragment;
import org.dromara.redisfront.ui.widget.content.view.scaffold.index.IndexViewFragment;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class TerminalPageView extends QSPageItem<MainWidget> {
    @Getter
    private final ConnectContext connectContext;

    private final RedisFrontTerminal terminal ;
    @Getter
    private final MainWidget owner;

    public TerminalPageView(ConnectContext connectContext, MainWidget owner) {
        this.connectContext = connectContext;
        this.owner = owner;
        this.terminal = new RedisFrontTerminal(connectContext);
    }

    @Override
    public MainWidget getApp() {
        return owner;
    }

    @Override
    protected JComponent getContentPanel() {
        return terminal;
    }

}
