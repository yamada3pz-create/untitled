package org.example;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or

import org.example.block.BlockEntityType;
import org.example.block.Blocks;
import org.example.core.ResourceManager;
import org.example.item.Items;

import javax.swing.*;

// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        ResourceManager.initFolderStructure();
        Blocks.register();
        Items.register();
        BlockEntityType.init();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Game game = new Game();
        frame.add(game);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}