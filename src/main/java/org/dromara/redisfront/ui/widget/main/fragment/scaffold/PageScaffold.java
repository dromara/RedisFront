package org.dromara.redisfront.ui.widget.main.fragment.scaffold;

import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.quickswing.ui.app.page.QSPageScaffold;
import org.dromara.redisfront.ui.widget.RedisFrontWidget;

public class PageScaffold extends QSPageScaffold<QSPageItem<RedisFrontWidget>> {
    public PageScaffold(QSPageItem<RedisFrontWidget> pageItem) {
        super(pageItem);
    }

    @Override
    public void onChange() {
        super.onChange();
    }

    public void onClose() {
        super.pageItem.onClose();
    }
}
