import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Rahul Nair
 * Net ID: rkn130030
 */
public class Router {

    public static Integer id = null;
    public static Integer[] lanIds = new Integer[10];
    public static Integer[] startLineNumber = new Integer[10];
    public static HashMap<Integer, Integer[]> dvRoutingMap = new HashMap<Integer, Integer[]>();
    public static int numberOfConnectedLans = 0;
    public static Long[] receiverTracking = new Long[10];

    /**
     * Executes for 100secs and write into Rout messages and DVs
     */
    public static void writeIntoRout() {
        try {
            File file = new File("rout" + id + ".txt");

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = null;

            fw = new FileWriter(file.getAbsoluteFile(), true);

            BufferedWriter bw = new BufferedWriter(fw);
            long routerProcessStart = System.currentTimeMillis();
            long routerProcessEnd = System.currentTimeMillis();
            while ((routerProcessEnd - routerProcessStart) <= 100000) {
                writeDVmsg(bw);
//                Thread.sleep(1000);

                while (System.currentTimeMillis() - routerProcessEnd <= 5000) {
                    readLanFile(bw);
                }

                routerProcessEnd = System.currentTimeMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeDVmsg(BufferedWriter bw) throws IOException {
        StringBuilder dvPortion = new StringBuilder();
        for (Integer dvLanId : dvRoutingMap.keySet()) {
            dvPortion.append(" ").append(dvRoutingMap.get(dvLanId)[0]).append(" ").append(dvRoutingMap.get(dvLanId)[1]);
        }
        for (int i = 0; i < numberOfConnectedLans; i++) {
            Integer srcLanId = lanIds[i];
            if (null != srcLanId) {
                StringBuilder dvMessage = new StringBuilder();
                dvMessage.append("DV ").append(srcLanId).append(" ").append(id).append(dvPortion);
                bw.write(dvMessage.toString());
                bw.newLine();
                bw.flush();
            }
        }
    }

    /**
     * Reading from LAN file and writing into RoutX
     * @param bw
     */

    private static void readLanFile(BufferedWriter bw) {
        BufferedReader br = null;

        try {
            int lanNumber = 0;
            while (lanNumber < numberOfConnectedLans) {
                Integer lanId = lanIds[lanNumber];
                if (null != lanId) {
                    String sCurrentLine;
                    File lanFile = new File("lan" + lanId + ".txt");
                    if (lanFile.exists()) {
                        br = new BufferedReader(new FileReader("lan" + lanId + ".txt"));
                        int currLineNumber = 0;
                        while (currLineNumber <= startLineNumber[lanNumber]) {
                            br.readLine();
                            currLineNumber++;
                        }
                        while ((sCurrentLine = br.readLine()) != null) {
                            startLineNumber[lanNumber]++;

                            Long currentTime = System.currentTimeMillis();
                            //Time checking for sending NMR for no receiver messages received
                            if (currentTime - receiverTracking[lanId] > 20000 && receiverTracking[lanId] > 0) {
                                StringBuffer nmr = new StringBuffer();
                                nmr.append("NMR ").append(lanId + " ").append(id + " ").append(lanId);
                                bw.write(nmr.toString());
                                bw.newLine();
                                bw.flush();
                            }
                            //For data message read from lan file
                            if (sCurrentLine.startsWith("data")) {
                                String[] spMsg = sCurrentLine.split(" ");
                                for (int j = 0; j < numberOfConnectedLans; j++) {
                                    Integer localLanId = lanIds[j];
                                    if (null != localLanId && lanId != localLanId && dvRoutingMap.get(localLanId)[0] == 0) {
                                        bw.write(sCurrentLine.replaceFirst(String.valueOf(sCurrentLine.charAt(5)), localLanId.toString()));
                                        bw.newLine();
                                        bw.flush();
                                    }
                                }
                                //For DV message read from the lan
                            } else if (sCurrentLine.startsWith("DV")) {
                                String[] msgParameters = sCurrentLine.split(" ");
                                int msgSrcLanId = Integer.valueOf(msgParameters[1]);
                                int msgSrcRouterId = Integer.valueOf(msgParameters[2]);
                                int indexForRoutingMap = 0;
                                int hopCountIndex = 3;
                                //Updating DV for the LAN connected to router
                                dvRoutingMap.get(msgSrcLanId)[0] = 0;
                                dvRoutingMap.get(msgSrcLanId)[1] = Math.min(msgSrcRouterId, dvRoutingMap.get(msgSrcLanId)[1]);
                                while (hopCountIndex + 1 < msgParameters.length && indexForRoutingMap < 10 && indexForRoutingMap != msgSrcLanId) {
                                    Integer hopCount = Integer.valueOf(msgParameters[hopCountIndex]);
                                    Integer routerId = Integer.valueOf(msgParameters[hopCountIndex + 1]);
                                    //Checking for lesser hopcount
                                    if (dvRoutingMap.get(indexForRoutingMap)[0] - hopCount > 1) {
                                        dvRoutingMap.put(indexForRoutingMap, new Integer[]{hopCount + 1, msgSrcRouterId});
                                    } else if ((dvRoutingMap.get(indexForRoutingMap)[0] - hopCount) == 1) {
                                        //checking for lesser routerid
                                        if (dvRoutingMap.get(indexForRoutingMap)[1] > routerId) {
                                            dvRoutingMap.put(indexForRoutingMap, new Integer[]{hopCount + 1, msgSrcRouterId});
                                        }
                                    }
                                    indexForRoutingMap++;
                                    hopCountIndex += 2;
                                }
                                //Update DV if NMR received from lan file
                            } else if (sCurrentLine.startsWith("NMR")) {
                                String[] msgSplitNMR = sCurrentLine.split(" ");
                                dvRoutingMap.get(Integer.parseInt(msgSplitNMR[3].trim()))[0] = 10;
                                dvRoutingMap.get(Integer.parseInt(msgSplitNMR[3].trim()))[1] = 10;
                            }//if receiver message read from LAN file
                            else if (sCurrentLine.startsWith("receiver")) {
                                receiverTracking[lanId] = System.currentTimeMillis();
                                dvRoutingMap.get(lanId)[0] = 0;
                                dvRoutingMap.get(lanId)[1] = id;
                            }
                        }
                    }
                }
                lanNumber++;
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
            if (args.length < 1) {
                System.out.println("Usage: router router-id lan-ID lan-ID lan-ID ...  &");
                System.exit(1);
            }

            id = Integer.valueOf(args[0]);

            for (int index = 1; index < args.length; index++) {
                lanIds[index - 1] = Integer.valueOf(args[index]);
            }
            Integer[] routerHop;
            int i = 0;
            while (i < 10) {
                routerHop = new Integer[2];
                routerHop[0] = 10;
                routerHop[1] = 10;
                dvRoutingMap.put(i, routerHop);
                i++;

            }
            Arrays.fill(startLineNumber, 0);
            Arrays.fill(receiverTracking, 0L);
            numberOfConnectedLans = args.length - 1;
            writeIntoRout();
        } catch (Exception e) {
            System.out.println(e + " in Router main()");
        }
    }


}

