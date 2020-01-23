import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("PC2 Admin Suite");
        frame.setContentPane(new MainFrame().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
