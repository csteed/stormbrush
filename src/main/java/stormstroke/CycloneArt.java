package stormstroke;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Created by csg on 5/5/15.
 */
public class CycloneArt implements ActionListener {
    private JFrame frame;
    private VisPanel visPanel;
    private ArrayList<ForecastAdvisoryCollection> advisoryCollectionList;
    private BufferedImage backgroundImage;

    public CycloneArt(BufferedImage backgroundImage, ArrayList<ForecastAdvisoryCollection> advisoryCollectionList) {
        this.backgroundImage = backgroundImage;
        this.advisoryCollectionList = advisoryCollectionList;
        initialize();
        frame.setVisible(true);
    }

    private void initialize() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();
        frame.setBounds(10, 10, (int)width - 20, (int)height - 20);

        initializePanel();
        initializeMenu();
    }

    private void initializePanel() {
        JSlider advisorySlider = new JSlider();

        visPanel = new VisPanel(backgroundImage, -105., -40., 10., 45.);
        JScrollPane visPanelScroller = new JScrollPane(visPanel);

        JPanel mainPanel = (JPanel)frame.getContentPane();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(visPanelScroller, BorderLayout.CENTER);
        mainPanel.add(advisorySlider, BorderLayout.SOUTH);
    }

    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        menu.addSeparator();

        JMenuItem mi = new JMenuItem("Exit", KeyEvent.VK_X);
        mi.addActionListener(this);
        mi.setActionCommand("exit");
        menu.add(mi);
    }

    public void actionPerformed(ActionEvent e) {

    }

    public static void main(String args[]) throws Exception {
        File backgroundImageFile = new File(args[0]);
        final BufferedImage image = ImageIO.read(backgroundImageFile);

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[1]));
        final ArrayList collectionList = (ArrayList)ois.readObject();
        ois.close();

        final File advisoryData = new File(args[1]);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                CycloneArt window = new CycloneArt(image, collectionList);
            }
        });
    }
}
