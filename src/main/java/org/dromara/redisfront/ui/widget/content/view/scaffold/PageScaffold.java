package org.dromara.redisfront.ui.widget.content.view.scaffold;

import org.dromara.quickswing.ui.app.page.QSPageItem;
import org.dromara.quickswing.ui.app.page.QSPageScaffold;
import org.dromara.redisfront.ui.widget.MainWidget;

public class PageScaffold extends QSPageScaffold<QSPageItem<MainWidget>> {
    public PageScaffold(QSPageItem<MainWidget> pageItem) {
        super(pageItem);
    }

    @Override
    public void onChange() {
        super.onChange();
    }
}
