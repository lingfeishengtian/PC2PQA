import com.lingfeishengtian.security.Extractor;
import edu.csus.ecs.pc2.core.model.Profile;
import edu.csus.ecs.pc2.core.security.FileSecurity;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Hashtable;

public class MainFrame {
    public JPanel panel1;
    private JButton pc2RootDiagOpen;
    private JButton inoutRootDiagOpen;
    private JButton LETSGOButton;
    private JTextArea log;
    private JButton fixFromWindows;
    private JLabel pc2Loc;
    private JLabel inoutRootLoc;
    private JFileChooser fileChooser;

    private Highlighter.HighlightPainter redPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.red);
    private Highlighter.HighlightPainter cyan = new DefaultHighlighter.DefaultHighlightPainter(Color.cyan);

    private Extractor extractor;
    private FileSecurity secure;

    File pc2RootSelected;
    File inoutRootSelected;

    public MainFrame(){
        DefaultCaret caret = (DefaultCaret)log.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        final File user = new File(System.getProperty("user.dir"));
        fileChooser = new JFileChooser(user);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        fixFromWindows.addActionListener(e -> {
            if(pc2RootSelected != null){
                if(checkPC2Root()){
                    modifyFile(pc2RootSelected.getAbsolutePath() + File.separator + "bin" + File.separator + "profiles.properties", "profiles\\\\", "profiles/");
                    logSomething("Finished modify profiles.properties fix.", -1);
                    Hashtable config = extractor.getConfigHashTable(secure);
                    Profile profile = ((Profile) config.get("PROBLEM"));
                    profile.setProfilePath(profile.getProfilePath().replaceAll("\\\\", "/"));
                    extractor.writeConfigurationToDisk(config, secure);
                }else{
                    logSomething("\nPC2 root folder is invalid!", 0);
                }
            }else{
                logSomething("You haven't selected a pc2 root folder yet!", 0);
            }
        });

        pc2RootDiagOpen.addActionListener(e -> {
            int i = fileChooser.showOpenDialog(panel1);
            if(i == JFileChooser.APPROVE_OPTION){
                pc2RootSelected = fileChooser.getSelectedFile();
                pc2Loc.setText(pc2RootSelected.getName());

                if(checkPC2Root()){
                    extractor = new Extractor(new File(pc2RootSelected.getAbsolutePath() + File.separator + "bin" + File.separator + "profiles").listFiles()[0].getAbsolutePath());
                    secure = extractor.getFileSecurity(JOptionPane.showInputDialog(panel1, "Contest password?"));
                }
            }
        });

        inoutRootDiagOpen.addActionListener(e -> {
            int i = fileChooser.showOpenDialog(panel1);
            if(i == JFileChooser.APPROVE_OPTION){
                inoutRootSelected = fileChooser.getSelectedFile();
                inoutRootLoc.setText(inoutRootSelected.getName());
            }
        });

        LETSGOButton.addActionListener(e -> {
            int n = JOptionPane.showConfirmDialog(
                    panel1,
                    "This operation will wipe all problems that already exist in the contest. The contest profile has to be already initialized with a contest password to continue. If you haven't set up a contest password, please do so.",
                    "WARNING",
                    JOptionPane.YES_NO_OPTION
            );
            if(n == 0){
                logSomething("Operation addProblemsFromFolder started.", 1);
            }
        });
    }

    private void modifyFile(String filePath, String oldString, String newString)
    {
        File fileToBeModified = new File(filePath);
        String oldContent = "";
        BufferedReader reader;
        FileWriter writer;
        try
        {
            reader = new BufferedReader(new FileReader(fileToBeModified));
            String line = reader.readLine();
            while (line != null)
            {
                oldContent = oldContent + line + System.lineSeparator();
                line = reader.readLine();
            }
            String newContent = oldContent.replaceAll(oldString, newString);
            writer = new FileWriter(fileToBeModified);
            writer.write(newContent);
            reader.close();
            writer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkPC2Root() {
        boolean preliminaryCheck = new File(pc2RootSelected.getAbsolutePath() + File.separator + "bin").exists() && new File(pc2RootSelected.getAbsolutePath() + File.separator + "lib").exists();
        if (!preliminaryCheck) {
            logSomething("Preliminary tests failed.\nbin and lib files do not exist in pc2 contest folder.", 0);
            return false;
        } else if (new File(pc2RootSelected.getAbsolutePath() + File.separator + "bin" + File.separator + "profiles").exists()) {
            return true;
        } else {
            logSomething("Your PC2 has not been initialized yet. Please run the contest!", 0);
            return false;
        }
    }

    private void logSomething(String data, int color){
        Highlighter.HighlightPainter paint = null;
        switch (color){
            case 0:
                paint = redPainter;
                break;
            case 1:
                paint = cyan;
                break;
            default:
                break;
        }
        try {
            log.append("\n" + data);
            if (paint != null) {
                log.getHighlighter().addHighlight(log.getText().length() - data.length(), log.getText().length() + data.length() - 1, paint);
            }
        }catch (BadLocationException e){
            logSomething("\n"+e.getMessage(), 0);
        }
    }
}