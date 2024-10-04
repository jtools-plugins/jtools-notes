package com.lhstack.notes;

import com.lhstack.tools.plugins.Action;
import com.lhstack.tools.plugins.Helper;
import com.lhstack.tools.plugins.IPlugin;
import com.lhstack.tools.plugins.Logger;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginImpl implements IPlugin {


    /**
     * 缓存笔记视图,key=project locationHash
     */
    private final Map<String, NotesView> viewMap = new HashMap<>();


    /**
     * 缓存logger,key=project locationHash
     */
    private final Map<String, Logger> loggerMap = new HashMap<>();

    /**
     * 创建笔记视图
     *
     * @param locationHash
     * @return
     */
    @Override
    public JComponent createPanel(String locationHash) {
        return viewMap.computeIfAbsent(locationHash, key -> {
            return new NotesView(locationHash, loggerMap.get(locationHash));
        });
    }

    /**
     * 缓存logger
     *
     * @param projectHash
     * @param logger
     * @param openThisPage
     */
    @Override
    public void openProject(String projectHash, Logger logger, Runnable openThisPage) {
        loggerMap.put(projectHash, logger);
    }

    /**
     * 项目关闭时,清理相关缓存
     *
     * @param projectHash
     */
    @Override
    public void closeProject(String projectHash) {
        NotesView notesView = viewMap.remove(projectHash);
        if (notesView != null) {
            notesView.run();
        }
        loggerMap.remove(projectHash);
    }

    /**
     * 插件卸载,清理缓存
     */
    @Override
    public void unInstall() {
        viewMap.values().forEach(Runnable::run);
        viewMap.clear();
        loggerMap.clear();
    }


    /**
     * 插件面板icon
     *
     * @return
     */
    @Override
    public Icon pluginIcon() {
        return Helper.findIcon("logo.svg", PluginImpl.class);
    }

    /**
     * 插件打开,顶部的tab icon
     *
     * @return
     */
    @Override
    public Icon pluginTabIcon() {
        return Helper.findIcon("logo_tab.svg", PluginImpl.class);
    }

    /**
     * 插件名称
     *
     * @return
     */
    @Override
    public String pluginName() {
        return "笔记";
    }

    /**
     * 插件描述
     *
     * @return
     */
    @Override
    public String pluginDesc() {
        return "这是一个笔记插件";
    }

    /**
     * 插件版本
     *
     * @return
     */
    @Override
    public String pluginVersion() {
        return "0.0.1";
    }


    /**
     * 插件内容tab右侧的按钮
     *
     * @param locationHash
     * @return
     */
    @Override
    public List<Action> swingTabPanelActions(String locationHash) {
        return Arrays.asList(new Action() {
            @Override
            public Icon icon() {
                return Helper.findIcon("icons/home.svg", PluginImpl.class);
            }

            @Override
            public String title() {
                return "主页";
            }

            @Override
            public void actionPerformed() {
                //如果未选中,点击则打开主页面板
                if (!isSelected()) {
                    viewMap.get(locationHash).switchHomeView();
                }
            }

            /**
             * 按钮是否需要选中
             * @return
             */
            @Override
            public boolean isSelected() {
                return viewMap.get(locationHash).isHomeView();
            }
        }, new Action() {
            @Override
            public Icon icon() {
                return Helper.findIcon("icons/content.svg", PluginImpl.class);
            }

            @Override
            public String title() {
                return "内容";
            }

            @Override
            public void actionPerformed() {
                //按钮未选中,则触发
                if (!isSelected()) {
                    //如果不能切换到内容视图,则激活日志面板,打印提示日志
                    if (!viewMap.get(locationHash).switchContentView()) {
                        loggerMap.get(locationHash).activeConsolePanel();
                        loggerMap.get(locationHash).warn("请先选择对应的节点");
                    }
                }
            }

            /**
             * 按钮是否需要选中
             * @return
             */
            @Override
            public boolean isSelected() {
                return viewMap.get(locationHash).isContentView();
            }

        });
    }
}
