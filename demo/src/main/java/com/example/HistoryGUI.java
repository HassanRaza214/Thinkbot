package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class HistoryGUI extends JFrame {
    public HistoryGUI(ChatbotGUI parent, List<String[]> entries) {
        setTitle("Chat History");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (String[] qa : entries) {
            String q = qa[0], a = qa[1];
            String pq = q.length() > 10 ? q.substring(0,10) + "..." : q;
            String pa = a.length() > 10 ? a.substring(0,10) + "..." : a;
            model.addElement("Q: " + pq + " | A: " + pa);
        }

        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int idx = list.getSelectedIndex();
                    if (idx >= 0) {
                        String[] selected = entries.get(idx);
                        parent.loadConversation(selected[0], selected[1]);
                        dispose();
                    }
                }
            }
        });

        add(new JScrollPane(list), BorderLayout.CENTER);
        setVisible(true);
    }
}
