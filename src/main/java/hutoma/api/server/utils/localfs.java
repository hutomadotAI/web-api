package hutoma.api.server.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by mauriziocibelli on 28/04/16.
 */
public class localfs {




    public static void createFolder (String folder) throws IOException {

        // check if the user directory exists
        File f = new File(folder);
        if (!f.isDirectory()) {
            f.mkdir();
            Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
            //add owners permission
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            //add group permissions
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_WRITE);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            //add others permissions
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(Paths.get(f.getPath()), perms);
        }

    }


    public static ArrayList<String> enumerateFolder (String folder) {
        ArrayList<String> res= new ArrayList<String>();

        try {
            File f = new File(folder);
            List<File> files = (List<File>) FileUtils.listFiles(f, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            for (File file : files) {
                 res.add(file.getName());
            }
        }
        catch (Exception ex) {System.out.println(ex.getMessage());}

        return res;

    }



    public static boolean folderExists (String folder) {
        try {
            File f = new File(folder);
            return f.isDirectory();
        }
        catch (Exception e) {return false;}

    }


    public static boolean removeFolder (String folder) throws IOException {
        try {
            File f = new File(folder);
            if (!f.isDirectory()) return false;
            FileUtils.deleteDirectory(f);
            return true;
        }
        catch (Exception e) {}
        return false;
    }

    public static boolean removeFile (String folder) throws IOException {
        try {
            File f = new File(folder);
            FileUtils.forceDelete(f);
            return true;
        }
        catch (Exception e) {}
        return false;
    }


    public static boolean copyFile (String source,String dest) throws IOException {
        try {

            File a = new File(source);
            File b = new File(dest);
            FileUtils.copyFile(a, b);
            return true;
        }
        catch (Exception e) { return false;}
    }


}
