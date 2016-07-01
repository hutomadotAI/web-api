package hutoma.api.server.utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.*;

/**
 * Created by mauriziocibelli on 27/04/16.
 */
public class remotefs {

    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }

    public static String cmd(String host, String cmd) {
        String result ="";
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(utils.getConfigProp("pemfile"));
            jsch.setConfig("StrictHostKeyChecking", "no");
            Session session=jsch.getSession("ubuntu", host, 22);
            session.connect();
            String command = cmd;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channel.setOutputStream(baos);

            channel.connect();
            result = new String(baos.toByteArray());

            channel.disconnect();
            session.disconnect();

        } catch (Exception ex) {}
        return result;
    }

    public static String upload(String host, String lfile,String rfile) {
        String result ="";
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(utils.getConfigProp("pemfile"));
            jsch.setConfig("StrictHostKeyChecking", "no");
            Session session=jsch.getSession("ubuntu", host, 22);
            session.connect();
            boolean ptimestamp = true;

            // exec 'scp -t rfile' remotely
            String command="scp " + (ptimestamp ? "-p" :"") +" -t "+rfile;
            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out=channel.getOutputStream();
            InputStream in=channel.getInputStream();

            channel.connect();

            File _lfile = new File(lfile);

            if(ptimestamp){
                command="T "+(_lfile.lastModified()/1000)+" 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command+=(" "+(_lfile.lastModified()/1000)+" 0\n");
                out.write(command.getBytes()); out.flush();
              }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize=_lfile.length();
            command="C0644 "+filesize+" ";
            if(lfile.lastIndexOf('/')>0){
                command+=lfile.substring(lfile.lastIndexOf('/')+1);
            }
            else{
                command+=lfile;
            }
            command+="\n";
            out.write(command.getBytes()); out.flush();

            // send a content of lfile
            FileInputStream fis=new FileInputStream(lfile);
            byte[] buf=new byte[1024];
            while(true){
                int len=fis.read(buf, 0, buf.length);
                if(len<=0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis=null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1); out.flush();
            out.close();
            channel.disconnect();
            session.disconnect();


        } catch (Exception ex) {}
        return result;
    }
}