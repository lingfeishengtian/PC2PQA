import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.File;

public class MainFrame {
    public JPanel panel1;
    private JButton pc2RootDiagOpen;
    private JButton inoutRootDiagOpen;
    private JButton LETSGOButton;
    private JTextArea log;
    private JTree folderSelectPC2;
    private JFileChooser fileChooser;

    private Highlighter.HighlightPainter redPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.red);
    private Highlighter.HighlightPainter cyan = new DefaultHighlighter.DefaultHighlightPainter(Color.cyan);

    public MainFrame(){
        LETSGOButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int n = JOptionPane.showConfirmDialog(
                        panel1,
                        "This operation will wipe all problems that already exist in the contest. The contest profile has to be already initialized with a contest password to continue. If you haven't set up a contest password, please do so.",
                        "WARNING",
                        JOptionPane.YES_NO_OPTION
                );
                if(n == 0){
                    logSomething("Operation addProblemsFromFolder started.", 1);
                }
            }
        });
    }

    private boolean checkFiles(){
        return false;
    }

    private void logSomething(String data, int color){
        Highlighter.HighlightPainter paint;
        switch (color){
            case 0:
                paint = redPainter;
                break;
            case 1:
                paint = cyan;
                break;
            default:
                logSomething("Integer " + color + " is not a valid color!", 0);
                return;
        }
        try {
            log.append("\n" + data);
            log.getHighlighter().addHighlight(log.getText().length() - data.length(), log.getText().length() + data.length() - 1, paint);
        }catch (BadLocationException e){
            logSomething("\n"+e.getMessage(), 0);
        }
    }
}