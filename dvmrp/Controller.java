import java.io.*;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: Rahul Nair
 * Net ID: rkn130030
 */
public class Controller {

    public static Integer[] lanIds = new Integer[10];
    public static Integer[] routerIds = new Integer[10];
    public static Integer[] hostIds = new Integer[10];

    public static Integer[] startLineNumberForRout = new Integer[10];
    public static Integer[] startLineNumberForHout = new Integer[10];

    /**
     * Copy contents from Hout and Rout into LAN files for message communication
     */
    public static void checkAndCopyContents() {
        BufferedReader br = null;
        BufferedWriter bw = null;

        try {

            long processStartTime = System.currentTimeMillis();
            long processEndTime = processStartTime + 100000;
            while ((processEndTime - processStartTime) > 0) {

                int currentHostNumber = 0;
                //For reading all Hout files
                for (Integer hostId : hostIds) {
                    if (null != hostId) {
                        String sCurrentLine;
                        File hostFile = new File("hout" + hostId + ".txt");
                        if (hostFile.exists()) {
                            br = new BufferedReader(new FileReader("hout" + hostId + ".txt"));
                            int currLineNumber = 0;
                            while (currLineNumber < startLineNumberForHout[currentHostNumber]) {
                                br.readLine();
                                currLineNumber++;
                            }
                            while ((sCurrentLine = br.readLine()) != null) {
                                String[] splitLine = sCurrentLine.split(" ");
                                startLineNumberForHout[currentHostNumber]++;
                                for (Integer localLanId : lanIds) {
                                    if (splitLine[1].equals(localLanId.toString())) {
                                        File file = new File("lan" + splitLine[1] + ".txt");
                                        if (!file.exists()) {
                                            file.createNewFile();
                                        }
                                        FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                                        bw = new BufferedWriter(fw);
                                        bw.write(sCurrentLine);
                                        bw.newLine();
                                        bw.flush();
                                        break;
                                    }
                                }
                            }
                            currentHostNumber++;
                        }
                    }
                }
                //For reading all Rout files
                Integer currentRouterNumber = 0;
                for (Integer routerId : routerIds) {
                    if (null != routerId && currentRouterNumber < 10) {
                        String sCurrentLine;
                        File routerFile = new File("rout" + routerId + ".txt");
                        if (routerFile.exists()) {
                            br = new BufferedReader(new FileReader("rout" + routerId + ".txt"));
                            int currLineNumber = 0;
                            while (currLineNumber <= startLineNumberForRout[currentRouterNumber]) {
                                br.readLine();
                                currLineNumber++;
                            }
                            while ((sCurrentLine = br.readLine()) != null) {
                                startLineNumberForRout[currentRouterNumber]++;
                                String[] splitLine = sCurrentLine.split(" ");
                                File file = new File("lan" + splitLine[1] + ".txt");

                                if (!file.exists()) {
                                    file.createNewFile();
                                }

                                FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                                bw = new BufferedWriter(fw);

                                bw.write(sCurrentLine);
                                bw.newLine();
                                bw.flush();
                            }
                            currentRouterNumber++;
                        }
                    }

                }
                processStartTime = System.currentTimeMillis();
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


    public static void main(String[] args) {
        try {
            if (args.length < 6) {
                System.out.println("Usage: controller host id id id . . . id router id id . . . id lan id id . .  . id  &");
                System.exit(1);
            }
            int index = 1;
            int localIndex = 0;
            if ("host".equals(args[0])) {
                while (index < args.length && !"router".equals(args[index]) && !"lan".equals(args[index])) {
                    hostIds[localIndex] = Integer.valueOf(args[index]);
                    index++;
                    localIndex++;
                    if (localIndex > 9) {
                        System.out.println("Maximum 10 hosts are supported ");
                        return;
                    }
                }
            } else {
                System.out.println("Usage: controller host id id id . . . id router id id . . . id lan id id . .  . id  &");
                System.exit(1);
            }
            localIndex = 0;
            if ("router".equals(args[index])) {
                index++;
                while (index < args.length && !"lan".equals(args[index])) {
                    routerIds[localIndex] = Integer.valueOf(args[index]);
                    index++;
                    localIndex++;
                    if (localIndex > 9) {
                        System.out.println("Maximum 10 routers are supported ");
                        return;
                    }
                }
            } else {
                System.out.println("Usage: controller host id id id . . . id router id id . . . id lan id id . .  . id  &");
                System.exit(1);
            }
            localIndex = 0;
            if ("lan".equals(args[index])) {
                index++;
                while (index < args.length) {
                    lanIds[localIndex] = Integer.valueOf(args[index]);
                    index++;
                    localIndex++;
                    if (localIndex > 9) {
                        System.out.println("Maximum 10 lan are supported ");
                        return;
                    }
                }
            } else {
                System.out.println("Usage: controller host id id id . . . id router id id . . . id lan id id . .  . id  &");
                System.exit(1);
            }
            Arrays.fill(startLineNumberForHout, 0);
            Arrays.fill(startLineNumberForRout, 0);
            checkAndCopyContents();
        } catch (Exception e) {
            System.out.println(e + " in Controller main()");
        }
    }

}

