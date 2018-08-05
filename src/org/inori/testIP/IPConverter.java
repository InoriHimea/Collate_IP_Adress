package org.inori.testIP;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class IPConverter {

    private static final String[] ISO_DM = {"AF","AX","AL","DZ","AS","AD","AO","AI","AQ","AG","AR","AM","AW","AU","AT","AZ","BS","BH","BD","BB","BY","BE","BZ","BJ","BM","BT","BO","BQ","BA","BW","BV","BR","IO","BN","BG","BF","BI","CV","KH","CM","CA","KY","CF","TD","CL","CN","CX","CC","CO","KM","CG","CD","CK","CR","CI","HR","CU","CW","CY","CZ","DK","DJ","DM","DO","EC","EG","SV","GQ","ER","EE","ET","FK","FO","FJ","FI","FR","GF","PF","TF","GA","GM","GE","DE","GH","GI","GR","GL","GD","GP","GU","GT","GG","GN","GW","GY","HT","HM","VA","HN","HK","HU","IS","IN","ID","IR","IQ","IE","IM","IL","IT","JM","JP","JE","JO","KZ","KE","KI","KP","KR","KW","KG","LA","LV","LB","LS","LR","LY","LI","LT","LU","MO","MK","MG","MW","MY","MV","ML","MT","MH","MQ","MR","MU","YT","MX","FM","MD","MC","MN","ME","MS","MA","MZ","MM","NA","NR","NP","NL","NC","NZ","NI","NE","NG","NU","NF","MP","NO","OM","PK","PW","PS","PA","PG","PY","PE","PH","PN","PL","PT","PR","QA","RE","RO","RU","RW","BL","SH","KN","LC","MF","PM","VC","WS","SM","ST","SA","SN","RS","SC","SL","SG","SX","SK","SI","SB","SO","ZA","GS","SS","ES","LK","SD","SR","SJ","SZ","SE","CH","SY","TW","TJ","TZ","TH","TL","TG","TK","TO","TT","TN","TR","TM","TC","TV","UG","UA","AE","GB","US","UM","UY","UZ","VU","VE","VN","VG","VI","WF","EH","YE","ZM","ZW"};

    public static void main(String[] args) throws IOException {
        downloadFile();
        readFile();
    }

    public static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static void downloadFile() throws IOException {
        URL url = new URL("https://ftp.apnic.net/apnic/stats/apnic/delegated-apnic-latest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(3 * 1000);

        InputStream inputStream = conn.getInputStream();
        byte[] getData = readInputStream(inputStream);

        File file = new File("D:/ip.txt");
/*        if (! file.exists()) {
            file.mkdirs();
        }*/

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        if  (fos != null) {
            fos.close();
        }

        if (inputStream != null) {
            inputStream.close();
        }

        System.out.println("download success");
    }

    public static void readFile() throws IOException {
        File file = new File("D:/ip.txt");
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        Map<String, List<Map<String, String>>> areaMap = new LinkedHashMap<String, List<Map<String, String>>>();
        //Set<String> setDm = new HashSet<String>();

        String s = "";
        while ((s = reader.readLine()) != null) {
            if (s.startsWith("apnic")) {
                String[] array = s.split("\\|");

/*                String dm = array[1];

                for (String s1 : ISO_DM) {
                    if (s1.equals(dm)) {
                        setDm.add(dm);
                    }
                }*/


                if (! (array[1].equals("CN") || array[1].equals("*")) && (array[2].equals("ipv4") || array[2].equals("ipv6"))) {
                    List<Map<String, String>> area = areaMap.get(array[1]);

                    Map<String, String> ipTypeMap = new HashMap<String, String>();
                    if (array[2].equals("ipv4")) {
                        String ip = array[3];
                        int num = Integer.parseInt(array[4]);

                        String[] split = ip.split("\\.");
                        if (num <= 256) {
                            split[3] = String.valueOf(Integer.parseInt(split[3]) + (num - 1));
                        } else if (num / 256 <= 256) {
                            split[3] = "255";
                            split[2] = String.valueOf(Integer.parseInt(split[2]) + (num / 256 - 1));
                        } else if (num / (256 * 256) <= 256) {
                            split[3] = "255";
                            split[2] = "255";
                            split[1] = String.valueOf(Integer.parseInt(split[1]) + (num / (256 * 256) - 1));
                        } else if (num / (256 * 256 * 256) <= 256) {
                            split[3] = "255";
                            split[2] = "255";
                            split[1] = "255";
                            split[0] = String.valueOf(Integer.parseInt(split[0]) + (num / (256 * 256 * 256) - 1));
                        }

                        ipTypeMap.put("ipv4", ip + "-" + String.join(".", split));
                    }

                    if (array[2].equals("ipv6")) {
                        String ip = array[3];
                        String mask = array[4];

                        ipTypeMap.put("ipv6", ip + "/" + mask);
                    }

                    if (area == null) {
                        List<Map<String, String>> list1 = new LinkedList<Map<String, String>>();
                        list1.add(ipTypeMap);
                        areaMap.put(array[1], list1);
                    } else {
                        area.add(ipTypeMap);
                    }
                }
            }
        }

/*        List<String> a = new ArrayList<String>();
        Set<String> strings = areaMap.keySet();
        for (String s1 : setDm) {
            if (! strings.contains(s1)) {
                a.add(s1);
            }
        }*/

/*        System.out.println(a);
        System.out.println(setDm);
        System.out.println(setDm.size());*/
        reader.close();

        File file1 = new File("D:/googleIp.txt");
        FileOutputStream fileOutputStream = new FileOutputStream(file1);
        for (String key : areaMap.keySet()) {
            fileOutputStream.write((key + ":\r\n").getBytes());
            List<Map<String, String>> areaIpMap = areaMap.get(key);

            fileOutputStream.write("----ipv4-----\r\n".getBytes());
            for (Map<String, String> ipMap : areaIpMap) {
                String ipv4 = ipMap.get("ipv4");

                if (ipv4 == null) {
                    continue;
                }

                fileOutputStream.write((ipv4 + "\r\n").getBytes());
            }

            fileOutputStream.write("----ipv6----\r\n".getBytes());
            for (Map<String, String> ipMap : areaIpMap) {
                String ipv6 = ipMap.get("ipv6");

                if (ipv6 == null) {
                    continue;
                }

                fileOutputStream.write((ipv6 + "\r\n").getBytes());
            }
        }

        System.out.println("------输出完成------");
    }
}
