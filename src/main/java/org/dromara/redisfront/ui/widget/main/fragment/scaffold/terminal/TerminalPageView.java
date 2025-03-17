package org.dromara.redisfront.ui.widget.main.fragment.scaffold.terminal;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.redisfront.model.context.RedisConnectContext;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

import javax.swing.*;

@Slf4j
public class TerminalPageView extends QSPageItem<RedisFrontWidget> {
    @Getter
    private final RedisConnectContext redisConnectContext;
    @Getter
    private final RedisFrontWidget owner;

    private RedisFrontTerminal terminal;

    public TerminalPageView(RedisConnectContext redisConnectContext, RedisFrontWidget owner) {
        this.redisConnectContext = redisConnectContext;
        this.owner = owner;
        this.setupUI();
    }

    @Override
    public void onLoad() {
        this.terminal = new RedisFrontTerminal(redisConnectContext);
    }

    @Override
    public void onChange() {
        super.onChange();
        terminal.ping();
    }

    @Override
    public RedisFrontWidget getApp() {
        return owner;
    }

    @Override
    protected JComponent getContentPanel() {
        return terminal;
    }

}
