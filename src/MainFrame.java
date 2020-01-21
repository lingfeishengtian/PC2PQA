import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;

public class MainFrame {
    public JPanel panel1;
    private JTree folderSelectPC2;
    private JTree folderSelectInOut;
    private JFileChooser fileChooser;

    public MainFrame(){
        File fileRoot = new File(System.getProperty("user.home"));

        folderSelectPC2.setModel(treeModel);
        //folderSelectInOut.setModel(treeModel);

    }
}