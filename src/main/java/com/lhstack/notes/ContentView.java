package com.lhstack.notes;

import com.lhstack.tools.plugins.Helper;
import com.lhstack.tools.plugins.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ContentView extends JPanel implements Runnable {

    private final JComponent languageTextField;
    private final JLabel title;
    private final Logger logger;
    private final String locationHash;
    private final Supplier<List<Data>> datas;
    private Data data;

    private Runnable disable;

    private Consumer<String> setValueConsumer;

    private boolean isChanged = false;

    public ContentView(String locationHash, NotesView notesView, Logger logger, Supplier<List<Data>> datas) {
        this.setLayout(new BorderLayout());
        this.setBorder(null);
        this.locationHash = locationHash;
        this.logger = logger;
        this.datas = datas;
        this.languageTextField = initTextEditorPane();
        this.title = new JLabel();
        this.title.setFont(new Font("", Font.PLAIN, 16));
        this.add(title, BorderLayout.NORTH);
        this.add(new JScrollPane(this.languageTextField), BorderLayout.CENTER);
    }

    private JComponent initTextEditorPane() {
        return Helper.languageTextField(
                "Markdown",
                locationHash,
                setValueConsumer1 -> {
                    this.setValueConsumer = setValueConsumer1;
                },
                run -> {
                    this.disable = run;
                },
                str -> {
                    if(isChanged){
                        ContentView.this.data.setText(str);
                        store();
                    }
                });
    }


    public void onShow(Data data) {
        isChanged = false;
        this.data = data;
        this.title.setText(data.getName());
        this.title.setHorizontalAlignment(JLabel.CENTER);
        this.setValueConsumer.accept(data.getText() == null ? "" : data.getText());
        isChanged = true;
    }


    public void store() {
        if (Config.getInstance().isGlobal()) {
            DataManager.store(datas.get(), System.getProperty("user.home") + "/.jtools/notes/data.json");
        } else {
            DataManager.storeData(datas.get(), locationHash);
        }
    }

    @Override
    public void run() {
        disable.run();
    }
}
