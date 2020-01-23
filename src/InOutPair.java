import java.io.File;

public class InOutPair {
    public File infile;
    public File outfile;

    public InOutPair(File in, File out){
        infile = in;
        outfile = out;
    }

    public InOutPair(){

    }

    public void putFile(File file){
        if(file.getName().endsWith(".in") || file.getName().endsWith(".dat")) infile = file;
        else if (file.getName().endsWith(".out")) outfile = file;
    }

    @Override
    public String toString() {
        return "InOutPair{" +
                "infile=" + infile +
                ", outfile=" + outfile +
                '}';
    }
}
