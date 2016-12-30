package hello.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ndb338 on 12/29/16.
 */
public class OutputCatcher extends Thread {
    public List<String> getLines() {
        return Lines;
    }

    private final List<String> Lines =new ArrayList<>();
    private InputStream inputStream;
    private String label;

    OutputCatcher(InputStream is)
    {
        this.inputStream = is;
    }

    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null) {
                Lines.add(line);
            }
                //System.out.println(label + ">" + line);
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
}
