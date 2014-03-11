import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Rahul Nair
 * Net ID: rkn130030
 */
public class Host {
    public static String type = null;
    public static Long timeToStart = null;
    public static Long period = null;
    public static Integer id = null;
    public static Integer lanId = null;
    public static Integer startLineNumber = 0;

    /**
     * Executes for 100secs and write into Hout
     */
    public static void writeIntoHout() {

        BufferedWriter bw = null;
        try {

            File file = new File("hout" + id + ".txt");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            Long hostStart = System.currentTimeMillis();
            Long hostEnd = System.currentTimeMillis();
            //For Hosts of type Receiver
            if ("receiver".equals(type)) {

                while ((hostEnd - hostStart) <= 100000) {
                    bw = new BufferedWriter(fw);
                    bw.write("receiver " + lanId);
                    bw.newLine();
                    bw.flush();
                    long start = System.currentTimeMillis();
                    long end = start + 10000;
                    while (end - start > 0) {
                        readLanFile();
                        start = System.currentTimeMillis();
                    }
                    hostEnd = System.currentTimeMillis();
                }
            }//For hosts of type Sender
            else if ("sender".equals(type)) {
                Thread.sleep(timeToStart * 1000);
                while ((hostEnd - hostStart) <= 100000) {
                    bw.write("data " + lanId + " " + lanId);
                    bw.newLine();
                    bw.flush();
                    Thread.sleep(period * 1000);
                    hostEnd = System.currentTimeMillis();
                }
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Reads LAN file (if receiver) for any incoming message
     */
    private static void readLanFile() {
        BufferedReader br = null;

        try {
            String sCurrentLine;
            File lanFile = new File("lan" + lanId + ".txt");
            if (lanFile.exists()) {
                br = new BufferedReader(new FileReader("lan" + lanId + ".txt"));
                int currLineNumber = 0;
                while (currLineNumber <= startLineNumber) {
                    br.readLine();
                    currLineNumber++;
                }
                while ((sCurrentLine = br.readLine()) != null) {
                    startLineNumber++;
                    if (sCurrentLine.startsWith("data") && sCurrentLine.contains("data " + lanId)) {
                        writeIntoHin(sCurrentLine);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Writes into Hin file for hosts of receiver type
     * @param data
     */
    public static void writeIntoHin(String data) {
        try {
            File file = new File("hin" + id + ".txt");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            if (null != data) {
                bw.write(data);
                bw.newLine();
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length < 3) {
                System.out.println("Usage: host host-id lan-id type time-to-start period  &");
                System.exit(1);
            }

            id = Integer.valueOf(args[0]);
            lanId = Integer.valueOf(args[1]);
            type = args[2];
            if ("sender".equals(type)) {
                timeToStart = Long.valueOf(args[3]);
                period = Long.valueOf(args[4]);
            } else {
                timeToStart = null;
                period = null;
            }
            writeIntoHout();
        } catch (Exception e) {
            System.out.println(e + " in Host main()");
        }
    }


}

