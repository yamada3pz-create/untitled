package org.example;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or

import org.example.block.BlockEntityType;
import org.example.block.Blocks;
import org.example.core.ResourceManager;
import org.example.data.DataLoader;
import org.example.item.Items;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        ResourceManager.initFolderStructure();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Game game = new Game();
        frame.add(game);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                game.shutdown();
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}