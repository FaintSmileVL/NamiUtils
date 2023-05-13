import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ${USER}
 * @date ${DATE}
 * @time ${TIME}
 */
public class Main {
    public static void main(String[] args) {
        /* STEP1 */
       /* String path = "C:\\Assembla\\NamiUtils\\resources";

        File clientPacketsSalvation = new File(path.toString() + "/clientpackets/salvation");
        File serverPacketsSalvation = new File(path.toString() + "/serverpackets/salvation");

        File clientPacketsHighFive = new File(path.toString() + "/clientpackets/highfive");
        File serverPacketsHighFive = new File(path.toString() + "/serverpackets/highfive");


        LinkedList<Pair> serverPacketsHighFiveEnumList = collectToList(serverPacketsHighFive);
        LinkedList<Pair> serverPacketsSalvationEnumList = collectToList(serverPacketsSalvation);

        writeToFile(serverPacketsHighFiveEnumList, "EServerPackets");
        writeToFile(serverPacketsSalvationEnumList, "EServerPacketsSalvation");
*/

        /* STEP2 */
        //String path = "C:\\Assembla\\NamiUtils\\resources\\src";
        //ПОМЕНЯТЬ ДИРЕКТОРИИ НА ХОДУ, ПРОКИДЫВАЮТСЯ ВНУТРЬ
        String path = "C:\\Users\\Admin\\IdeaProjects\\NamiUtils\\packets";
        String innerPath = "\\gameserver";
        path = path + innerPath;
        String innerPath2 = "\\result";
        File packetSenders = new File(path.toString());
        File packetSenders2 = new File(path.toString());

        movePacketSendersToNewStandard(packetSenders, "\\result\\EXCEPTIONS\\SENDERS", innerPath);
        //movePacketBroadcastersToNewStandard(packetSenders2, "\\result\\EXCEPTIONS\\BROADCASTERS", innerPath2);
    }

    public static LinkedList<Pair> collectToList(File dir) {
        LinkedList<Pair> result = new LinkedList<>();
        if (!dir.isDirectory()) {
            System.out.println(dir.getName() + "is not a directory");
        }
        int i = 0;
        for (File file : dir.listFiles()) {
            System.out.println(file.getName());
            i += 1;

            StringBuilder fileAsString = new StringBuilder();
            File myFile = new File(file.getAbsolutePath());
            boolean isPacket = false;
            boolean isExceptionFile = false;
            String className = null;
            try (FileReader reader = new FileReader(myFile)) {
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    fileAsString.append(line);
                    if (line.contains("extends L2GameServerPacket") && !line.contains("abstract")) {
                        isPacket = true;
                        if (line.contains("final")) {
                            className = line.split(" ")[3];
                        } else {
                            className = line.split(" ")[2];
                        }
                    }
                    if (line.contains("ExMpccPartymasterList")  // HF
                            || line.contains("WrappedMessage") || line.contains("PacketBuilder")
                            || line.contains("ExGMViewQuestItemList") || line.contains("ExOlympiadSpelledInfo")
                            || line.contains("ExPledgeCrestLarge") || line.contains("ExQuestItemList")
                            || line.contains("ExReplySentPost") || line.contains("ExRpItemLink")
                            || line.contains("ExShowQuestMark") || line.contains("ExShowReceivedPostList")
                            || line.contains("ExShowTrace") || line.contains("ExStorageMaxCount")
                            || line.contains("ExVitalityPointInfo")) {
                        isExceptionFile = true;
                    }
                    if (line.contains("Ex2ndPasswordAck") // SALVATION
                            || line.contains("Ex2ndPasswordCheck") || line.contains("Ex2ndPasswordVerify")
                            || line.contains("ExAttributeEnchantResult") || line.contains("ExRefundList")
                            || line.contains("NpcInfoPoly")) {
                        isExceptionFile = true;
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }

            if (isPacket && !isExceptionFile) {
                String fullFile = fileAsString.toString();
                String opCode = null;
                boolean isExPacket = false;
                if (fullFile.contains("0xFE")) {// обработка extended
                    opCode = getOpCode(fullFile);
                    isExPacket = true;
                } else if (fullFile.contains("EXTENDED_PACKET")) {// обработка extended
                    opCode = getOpCode(fullFile);
                    isExPacket = true;
                } else if (fullFile.contains("writeEx")) {// обработка extended
                    int indexOfOpCode = fullFile.indexOf("writeEx(") + "writeEx(".length();
                    opCode = fullFile.substring(indexOfOpCode, indexOfOpCode + 4);
                    isExPacket = true;
                } else {
                    int indexOfOpCode = fullFile.indexOf("writeC(");
                    if (indexOfOpCode == -1) {
                        indexOfOpCode = fullFile.indexOf("writeB(") + "writeB(".length();
                    } else {
                        indexOfOpCode += "writeC(".length();
                    }
                    opCode = fullFile.substring(indexOfOpCode, indexOfOpCode + 4);
                }

                //одинарные большие буквы друг за другом разделить символом "_"
                String classNameForEnum = className.replaceAll("([^_A-Z])([A-Z])", "$1_$2").toUpperCase();
                //камелкейс в снейк кейс
                String number = opCode.substring(2, 4).toUpperCase();
                String forEnum = classNameForEnum + "(" + (isExPacket ? "0xFE, " : "") + ("0x" + number) + ", " + className + ".class),";
                Pair pair = new Pair();
                pair.left = Integer.parseInt(number, 16);
                pair.right = forEnum;
                result.addLast(pair);
            }
        }
        System.out.println("Processed " + i + " files");
        result.sort(new Comparator<Pair>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                int result = 0;
                if (o1.right.contains("0xFE") && !o2.right.contains("0xFE")) {
                    return 1;
                }
                if (o2.right.contains("0xFE") && !o1.right.contains("0xFE")) {
                    return -1;
                }
                if (o1.left > o2.left) {
                    result = 1;
                } else {
                    result = -1;
                }
                return result;
            }
        });
        return result;
    }

    private static String getOpCode(String fullFile) {
        String opCode = null;
        int indexOfOpCode = fullFile.indexOf("writeH(");
        if (indexOfOpCode != -1) {
            indexOfOpCode += "writeH(".length();
        } else {
            indexOfOpCode = fullFile.indexOf("writeHG(");
            if (indexOfOpCode != -1) {
                indexOfOpCode += "writeHG(".length();
            } else {
                indexOfOpCode = fullFile.indexOf("writeHG37(");
                if (indexOfOpCode != -1) {
                    indexOfOpCode += "writeHG37(".length();
                } else {
                    indexOfOpCode = fullFile.indexOf("writeD(") + "writeD(".length();
                }
            }
        }

        if (fullFile.contains("getClient().isSalvation()")) {
            opCode = fullFile.substring(indexOfOpCode + 35, indexOfOpCode + 39);
        } else if (fullFile.contains("getClient().isLindvior()")) {
            opCode = fullFile.substring(indexOfOpCode + 34, indexOfOpCode + 38);
        } else {
            opCode = fullFile.substring(indexOfOpCode, indexOfOpCode + 4);
        }
        return opCode;
    }

    /**
     * Writing ENUM
     * @param list
     * @param name
     */
    public static void writeToFile(LinkedList<Pair> list, String name) {
        try (FileWriter writer = new FileWriter(name + ".java", false)) {
            writer.write("public enum " + name + " {\n");
            for (Pair pair : list) {
                writer.write("\t" + pair.right + "\n");
            }
            writer.write("}");
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void movePacketSendersToNewStandard(File dir, String exceptionPath, String innerPath){
        if(dir.isDirectory()) {
            for (File item : dir.listFiles()) {
                if (item.isDirectory())
                {
                    //копаем дальше в директорию
                    if (item.getAbsolutePath().contains("loginservercon"))
                        continue;
                    movePacketSendersToNewStandard(new File(dir + "/" + item.getName()), exceptionPath, innerPath);
                } else {

                    if (item.getName().contains(".htm")) {
                        continue;
                    }

                    Path path = FileSystems.getDefault().getPath(item.getAbsolutePath());
                    Charset charset = StandardCharsets.UTF_8;

                    String content = "";

                    try {
                        content = new String(Files.readAllBytes(path), charset);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    //String requestPattern = "(\\S*).sendPacket\\((\\S*)new (\\w*)([^,]*)(.*)\\);";
                    String requestPattern = "(\\S*).sendPacket\\((\\S*)new (\\w*)(.*)\\);";
                    Pattern p = Pattern.compile(requestPattern);
                    //content = " actor.sendPacket((new SystemMessage(3151)));";
                    Matcher matcher = p.matcher(content);
                    boolean writeException = false;

                    if (matcher.find()) {
                        int start = 0;

                        while (matcher.find(start)) {
                            String result = matcher.group();
                            String invoker = matcher.group(1);
                            String sendGavno = matcher.group(2);    //Касты и прочая шляпа идут на хуй
                            String packet = matcher.group(3);
                            String args = matcher.group(4);

                            String invoker_new = invoker == "" ? "this, " : matcher.group(1) + ", ";
                            String packet_new = packet + ".class";
                            args = args.substring(1);

                            boolean gavnoCode = false;
                            boolean exceptionUnknown = false;

                            if (sendGavno.equals("(")){
                                gavnoCode = true;
                            }
                            else {
                                //не обрабатываем
                                if (sendGavno.contains("(")){
                                    exceptionUnknown = true;
                                }
                            }

                            if(
                            exceptionUnknown ||
                            result.contains("LSConnection.getInstance") ||
                            result.matches(".*sendPacket.*new.*,.*new.*")||
                            result.contains("getClient()")
                            ){
                                writeException = true;
                                start = matcher.end(4);
                                continue;
                            }

                            //Парсинг аргументов
                            String packetArgs = "";
                            int countUnclosedBrackets = 1;
                            if (gavnoCode)
                                countUnclosedBrackets = 2;

                            String nonPacketArgs = "";
                            boolean insideException = false;
                            boolean packetArgsEnd = false;

                            for (char ch: args.toCharArray()) {

                                if (ch == '.' && countUnclosedBrackets == 0){
                                    if (!result.contains("SystemMessage")) {
                                        writeException = true;
                                    }
                                    insideException = true;
                                    continue;
                                }

                                if (ch == ',' && countUnclosedBrackets == 0){
                                    packetArgsEnd = true;
                                }

                                if (ch == '('){
                                    countUnclosedBrackets += 1;
                                }

                                if (ch == ')'){
                                    countUnclosedBrackets -= 1;
                                }


                                if (packetArgsEnd){
                                    nonPacketArgs += ch;
                                }
                                else{
                                    packetArgs += ch;
                                }

                            }

                            if (insideException) {
                                start = matcher.end(4); //следующий матч после кэпчур группы 4
                                continue;
                            }

                            packetArgs = packetArgs.substring(0, packetArgs.length() - 1);

                            if(gavnoCode){
                                packetArgs = packetArgs.substring(0, packetArgs.length() - 1);
                            }


                            if (!packetArgs.equals("")){
                                packet_new += ", ";
                            }

                            if (!nonPacketArgs.equals("")){
                                start = matcher.end(4);
                                continue;
                            }

                            if (result.contains("SystemMessage")){
                                start = matcher.end(4); //следующий матч после кэпчур группы 4
                                continue;
                            }

                            String newString = "NetworkPacketController.getInstance().sendPacket(" + invoker_new + packet_new + packetArgs + nonPacketArgs + ");";
                            content = content.replace(result, newString);
                            start = matcher.end(4); //следующий матч после кэпчур группы 4
                        }

                        if (writeException){
                            writeExceptionFile(item, content, charset, exceptionPath, innerPath);
                        }
                        else{
                            writeNormalFile(item, content, charset, "\\result", innerPath);
                        }
                    }
                }
            }
        }
    }

    public static void movePacketBroadcastersToNewStandard(File dir, String exceptionPath, String innerPath){
        if(dir.isDirectory()) {
            for (File item : dir.listFiles()) {
                if (item.isDirectory())
                {
                    //копаем дальше в директорию
                    if (item.getAbsolutePath().contains("loginservercon"))
                        continue;
                    movePacketBroadcastersToNewStandard(new File(dir + "/" + item.getName()), exceptionPath, innerPath);
                } else {

                    if (item.getName().contains(".htm")) {
                        continue;
                    }

                    Path path = FileSystems.getDefault().getPath(item.getAbsolutePath());
                    Charset charset = StandardCharsets.UTF_8;

                    String content = "";

                    try {
                        content = new String(Files.readAllBytes(path), charset);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    //String requestPattern = "(\\S*).sendPacket\\((\\S*)new (\\w*)([^,]*)(.*)\\);";
                    String requestPattern = "(\\S*).broadcastPacket\\((\\S*)new (\\w*)(.*)\\);";
                    Pattern p = Pattern.compile(requestPattern);
                    //content = " actor.sendPacket((new SystemMessage(3151)));";
                    Matcher matcher = p.matcher(content);
                    boolean writeException = false;

                    if (matcher.find()) {
                        int start = 0;

                        while (matcher.find(start)) {
                            String result = matcher.group();
                            String invoker = matcher.group(1);
                            String sendGavno = matcher.group(2);    //Касты и прочая шляпа идут на хуй
                            String packet = matcher.group(3);
                            String args = matcher.group(4);

                            String invoker_new = invoker == "" ? "this, " : matcher.group(1) + ", ";
                            String packet_new = packet + ".class";
                            args = args.substring(1);

                            boolean gavnoCode = false;
                            boolean exceptionUnknown = false;

                            if (sendGavno.equals("(")){
                                gavnoCode = true;
                            }
                            else {
                                //не обрабатываем
                                if (sendGavno.contains("(")){
                                    exceptionUnknown = true;
                                }
                            }

                            if(
                                    exceptionUnknown ||
                                            result.contains("LSConnection.getInstance") ||
                                            result.matches(".*broadcastPacket.*new.*,.*new.*")||
                                            result.contains("getClient()")
                            ){
                                writeException = true;
                                start = matcher.end(4);
                                continue;
                            }

                            //Парсинг аргументов
                            String packetArgs = "";
                            int countUnclosedBrackets = 1;
                            if (gavnoCode)
                                countUnclosedBrackets = 2;

                            String nonPacketArgs = "";
                            boolean insideException = false;
                            boolean packetArgsEnd = false;

                            for (char ch: args.toCharArray()) {

                                if (ch == '.' && countUnclosedBrackets == 0){
                                    if (!result.contains("SystemMessage")) {
                                        writeException = true;
                                    }
                                    insideException = true;
                                    continue;
                                }

                                if (ch == ',' && countUnclosedBrackets == 0){
                                    packetArgsEnd = true;
                                }

                                if (ch == '('){
                                    countUnclosedBrackets += 1;
                                }

                                if (ch == ')'){
                                    countUnclosedBrackets -= 1;
                                }


                                if (packetArgsEnd){
                                    nonPacketArgs += ch;
                                }
                                else{
                                    packetArgs += ch;
                                }

                            }

                            if (insideException) {
                                start = matcher.end(4); //следующий матч после кэпчур группы 4
                                continue;
                            }

                            packetArgs = packetArgs.substring(0, packetArgs.length() - 1);

                            if(gavnoCode){
                                packetArgs = packetArgs.substring(0, packetArgs.length() - 1);
                            }


                            if (!packetArgs.equals("")){
                                packet_new += ", ";
                            }

                            if (!nonPacketArgs.equals("")){
                                start = matcher.end(4);
                                continue;
                            }

                            if (result.contains("SystemMessage")){
                                start = matcher.end(4); //следующий матч после кэпчур группы 4
                                continue;
                            }

                            String newString = "NetworkPacketController.getInstance().broadcastPacket(" + invoker_new + packet_new + packetArgs + nonPacketArgs + ");";
                            content = content.replace(result, newString);
                            start = matcher.end(4); //следующий матч после кэпчур группы 4
                        }

                        if (writeException){
                            writeExceptionFile(item, content, charset, exceptionPath, innerPath);
                        }
                        else{
                            writeNormalFile(item, content, charset, "\\result", innerPath);
                        }
                    }
                }
            }
        }
    }

    private static void createDir(Path parent) {
        Path parentDir = Paths.get(parent.toUri()).getParent();
        try {
            if (!Files.exists(parentDir)) {
                createDir(parentDir);
            }
            if (!parent.toString().endsWith(".java")) {
                Files.createDirectory(parent);
            } else {
                Files.createFile(parent);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeExceptionFile(File item, String content, Charset charset, String newPath, String innerPath){
        try {
            Path path2 = FileSystems.getDefault().getPath(item.getAbsolutePath().replace(innerPath, newPath));
            if (!Files.exists(path2)) {
                createDir(path2);
            }
            Files.write(path2, content.getBytes(charset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeNormalFile(File item, String content, Charset charset, String newPath, String innerPath){
        try {
            Path path2 = FileSystems.getDefault().getPath(item.getAbsolutePath().replace(innerPath, newPath));
            if (!Files.exists(path2)) {
                createDir(path2);
            }
            Files.write(path2, content.getBytes(charset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}