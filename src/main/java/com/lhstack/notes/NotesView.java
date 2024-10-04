package com.lhstack.notes;

import com.lhstack.tools.plugins.Logger;

import javax.swing.*;
import java.awt.*;

public class NotesView extends JPanel implements Runnable {

    private static final String HOME_PAGE = "HOME";

    private static final String CONTENT_PAGE = "CONTENT";

    private final HomeView homeView;

    private final ContentView contentView;

    private final CardLayout cardLayout;

    private String currentView;

    public NotesView(String locationHash, Logger logger) {
        //笔记主页视图
        this.homeView = new HomeView(locationHash, this, logger);
        //笔记内容视图
        this.contentView = new ContentView(locationHash, this, logger, homeView::getDatas);
        //创建卡片布局
        this.cardLayout = new CardLayout();
        //添加主页视图到布局
        cardLayout.addLayoutComponent(homeView, HOME_PAGE);
        //添加内容视图到布局
        cardLayout.addLayoutComponent(contentView, CONTENT_PAGE);
        //添加视图到容器
        this.add(homeView);
        this.add(contentView);
        //为容器设置卡片布局
        this.setLayout(cardLayout);
        //显示主页视图
        this.cardLayout.show(this, HOME_PAGE);
        //缓存当前显示的视图
        this.currentView = HOME_PAGE;
    }

    public void switchHomeView() {
        //显示主页
        cardLayout.show(this, HOME_PAGE);
        //设置当前显示的视图为主页
        currentView = HOME_PAGE;
    }

    public boolean switchContentView() {
        //切换内容面板,需要判断是否切换成功
        //获取视图面板当前选中的节点,没有就是false
        return this.homeView.getSelectedData().map(data -> {
            //获取到,则将当前节点放入内容视图
            this.contentView.onShow(data);
            //切换到内容视图
            cardLayout.show(this, CONTENT_PAGE);
            //修改当前缓存视图为内容视图
            currentView = CONTENT_PAGE;
            return true;
        }).orElse(false);
    }

    /**
     * 判断当前是否为内容视图,用于按钮选中效果
     *
     * @return
     */
    public boolean isContentView() {
        return currentView.equals(CONTENT_PAGE);
    }

    /**
     * @return
     */
    public boolean isHomeView() {
        return currentView.equals(HOME_PAGE);
    }

    /**
     * 卸载,项目关闭回调
     */
    @Override
    public void run() {
        this.contentView.run();
    }
}
