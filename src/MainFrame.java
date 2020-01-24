import com.lingfeishengtian.security.Extractor;
import com.lingfeishengtian.utils.DefaultContest;
import com.lingfeishengtian.utils.ProblemModifier;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Profile;
import edu.csus.ecs.pc2.core.security.FileSecurity;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Scanner;

public class MainFrame {
    public JPanel panel1;
    private JButton pc2RootDiagOpen;
    private JButton inoutRootDiagOpen;
    private JButton LETSGOButton;
    private JTextArea log;
    private JButton fixFromWindows;
    private JLabel pc2Loc;
    private JLabel inoutRootLoc;
    private JButton selectProblemListButton;
    private JLabel problemListName;
    private JButton setupDefaultCompetition;
    private JFileChooser fileChooser;

    private Highlighter.HighlightPainter redPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.red);
    private Highlighter.HighlightPainter cyan = new DefaultHighlighter.DefaultHighlightPainter(Color.cyan);

    private Extractor extractor;
    private FileSecurity secure;

    File pc2RootSelected;
    File inoutRootSelected;
    File problemListFile;

    Hashtable<String, InOutPair> inouts = new Hashtable();

    public MainFrame(){
        JFileChooser chooseTxt = new JFileChooser();

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
                    Profile profile = ((Profile) config.get("PROFILE"));
                    profile.setProfilePath(profile.getProfilePath().replaceAll("\\\\", "/"));
                    extractor.writeConfigurationToDisk(config, secure);
                    logSomething("Fixed profile path.", -1);
                }else{
                    logSomething("\nPC2 root folder is invalid!", 0);
                }
            }else{
                logSomething("You haven't selected a pc2 root folder yet!", 0);
            }
        });

        setupDefaultCompetition.addActionListener(e -> {
            if(pc2RootSelected != null){
                Hashtable config = extractor.getConfigHashTable(secure);
                DefaultContest.modifyContestToDefaultSettings(config);
                extractor.writeConfigurationToDisk(config, secure);
                logSomething("Default contest set succeeded", 1);
            }else{
                logSomething("PC2 root has not been initialized!", -1);
            }
        });

        pc2RootDiagOpen.addActionListener(e -> {
            int i = fileChooser.showOpenDialog(panel1);
            if(i == JFileChooser.APPROVE_OPTION){
                pc2RootSelected = fileChooser.getSelectedFile();
                pc2Loc.setText(pc2RootSelected.getName());

                if(checkPC2Root()){
                    extractor = new Extractor(new File(pc2RootSelected.getAbsolutePath() + File.separator + "bin" + File.separator + "profiles").listFiles()[0].getAbsolutePath() + File.separator + "db.1");
                    secure = extractor.getFileSecurity(JOptionPane.showInputDialog(panel1, "Contest password?"));
                    if(secure == null)
                    {
                        logSomething("Contest Password Incorrect", 0);
                        extractor = null;
                        secure = null;
                        pc2RootSelected = null;
                        pc2Loc.setText("");
                    }else{
                        logSomething("Loaded PC2 directory", -1);
                    }
                }else{
                    pc2RootSelected = null;
                    pc2Loc.setText("");
                }
            }
        });

        inoutRootDiagOpen.addActionListener(e -> {
            int i = fileChooser.showOpenDialog(panel1);
            if(i == JFileChooser.APPROVE_OPTION){
                inoutRootSelected = fileChooser.getSelectedFile();
                inoutRootLoc.setText(inoutRootSelected.getName());

                if(!checkAndSaveInOuts()){
                    inoutRootSelected = null;
                    inoutRootLoc.setText("");
                }
            }
        });

        selectProblemListButton.addActionListener(e -> {
            chooseTxt.showOpenDialog(panel1);
            problemListFile = chooseTxt.getSelectedFile();
            problemListName.setText(problemListFile.getName());
        });

        LETSGOButton.addActionListener(e -> {
            int n = JOptionPane.showConfirmDialog(
                    panel1,
                    "This operation will wipe all problems that already exist in the contest.\nThe contest must be initialized with a contest password before you start.",
                    "WARNING",
                    JOptionPane.YES_NO_OPTION
            );
            if(n == 0){
                try {
                    Scanner scan = null;
                    if(problemListFile != null)
                        scan = new Scanner(problemListFile);

                    logSomething("Operation addProblemsFromFolder started.", 1);

                    if(pc2RootSelected != null && inoutRootSelected != null){
                        logSomething("Passed preliminary check.", 1);
                        Hashtable hash = extractor.getConfigHashTable(secure);
                        Problem[] problems = new Problem[0];
                        hash.put("PROBLEMS", problems);

                        if(scan != null)
                            while(scan.hasNext()){
                                String name = scan.nextLine().trim();
                                InOutPair pair = inouts.get(name);
                                ProblemModifier.addProblemWithDefaultSettings(name, pair.infile, pair.outfile, hash);
                            }
                        else
                            inouts.forEach((String name, InOutPair pair) -> {
                                ProblemModifier.addProblemWithDefaultSettings(name, pair.infile, pair.outfile, hash);
                            });

                        for (Problem a :
                                (Problem[]) hash.get("PROBLEMS")) {
                            logSomething(a.toStringDetails(), 1);
                        }

                        extractor.writeConfigurationToDisk(hash, secure);
                    }else{
                        logSomething("PC2 Root or In Out Root haven't been selected yet!", 0);
                    }
                } catch (FileNotFoundException ex) {
                    logSomething("File getting failed while attempting to order problems.", 0);
                    ex.printStackTrace();
                }
            }
        });
    }

    private boolean checkAndSaveInOuts(){
        for (File file :
                inoutRootSelected.listFiles()) {
            if(file.getName().startsWith(".")) continue;
            if(file.getName().endsWith(".in") || file.getName().endsWith(".out") || file.getName().endsWith(".dat")){
                InOutPair pair = inouts.get(parseName(file.getName()));
                if(pair != null){
                    pair.putFile(file);
                    inouts.put(parseName(file.getName()), pair);
                }else{
                    InOutPair tmp = new InOutPair();
                    tmp.putFile(file);
                    inouts.put(parseName(file.getName()), tmp);
                }
                logSomething(file.getName() + " set successfully.", -1);
            }else{
                logSomething(file.getAbsolutePath() + " is not a valid in or out or dat file.", 0);
                return false;
            }
        }
        return true;
    }

    /**
     * Prerequisites REQUIRE name to be either .in or .out
     * @param name of the file
     * @return Name of the problem
     */
    public static String parseName(String name){
        boolean isIn = name.endsWith(".in");
        name = name.substring(0, name.length() - (isIn ? ".in" : ".out").length());
        String[] splitName = name.split("_");
        String newString = "";
        for (int i = 0; i < splitName.length; i++) {
            newString += (i == 0 ? "" : " ") + Character.toUpperCase(splitName[i].charAt(0)) + splitName[i].substring(1);
        }
        return newString;
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