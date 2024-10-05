package com.lhstack.notes;

import com.lhstack.tools.plugins.Logger;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TextEditorPane;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class ContentView extends JPanel implements DocumentListener, Runnable {

    private final TextEditorPane textEditorPane;
    private final JLabel title;
    private final Logger logger;
    private final String locationHash;
    private final Supplier<List<Data>> datas;
    private Data data;

    public ContentView(String locationHash, NotesView notesView, Logger logger, Supplier<List<Data>> datas) {
        this.setLayout(new BorderLayout());
        this.setBorder(null);
        this.logger = logger;
        this.textEditorPane = initTextEditorPane();
        this.title = new JLabel();
        this.title.setFont(new Font("", Font.PLAIN, 16));
        this.add(title, BorderLayout.NORTH);
        RTextScrollPane rTextScrollPane = new RTextScrollPane(this.textEditorPane);
        rTextScrollPane.setBorder(null);
        this.add(rTextScrollPane, BorderLayout.CENTER);
        this.datas = datas;
        this.locationHash = locationHash;
    }

    private TextEditorPane initTextEditorPane() {
        TextEditorPane pane = new TextEditorPane();
        pane.setTabSize(2);
        pane.setLineWrap(true);
        pane.setHighlightCurrentLine(true);
        pane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
        pane.setCodeFoldingEnabled(true);
        return pane;
    }


    public void onShow(Data data) {
        this.data = data;
        this.title.setText(data.getName());
        this.title.setHorizontalAlignment(JLabel.CENTER);
        Document document = this.textEditorPane.getDocument();
        document.removeDocumentListener(this);
        this.textEditorPane.setText(data.getText());
        document.addDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.data.setText(textEditorPane.getText());
        DataManager.storeData(datas.get(), locationHash);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.data.setText(textEditorPane.getText());
        DataManager.storeData(datas.get(), locationHash);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.data.setText(textEditorPane.getText());
        DataManager.storeData(datas.get(), locationHash);
    }

    @Override
    public void run() {
        this.textEditorPane.resetKeyboardActions();
        this.textEditorPane.clearParsers();
        this.textEditorPane.clearMarkAllHighlights();
    }
}
