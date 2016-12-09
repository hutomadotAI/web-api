package KPI.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mauriziocibelli on 01/12/16.
 */
public class merge_QA_files {


    // Small utility class to merge a QA file into a single training file
    public static void main(String[] args) throws IOException {
        String q_file = args[0]; // path to Q file
        String a_file = args[1]; // path to A file
        String mergred_file = args[2]; // path to Merged file

        BufferedReader in = new BufferedReader(new FileReader(q_file));
        String str;
        List<String> q = new ArrayList<String>();
        while ((str = in.readLine()) != null) {
            q.add(str);
        }

        in = new BufferedReader(new FileReader(a_file));
        List<String> a = new ArrayList<String>();
        while ((str = in.readLine()) != null) {
            a.add(str);
        }

        List<String> c = new ArrayList<>();
        FileWriter writer = new FileWriter(mergred_file, true);

        for (int i = 0; i < q.size(); i++) {
            writer.write(q.get(i) + "\n");
            writer.write(a.get(i) + "\n");
            writer.write("\n");
        }

        writer.close();
    }

}
