import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
        /* STEP1
        String path = "C:\\Assembla\\NamiUtils\\resources";

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
        String path = "C:\\Users\\Admin\\IdeaProjects\\NamiUtils\\packets\\gameserver";
        File packetSenders = new File(path.toString());
        movePacketSendersToNewStandard(packetSenders);
        movePacketBroadcastersToNewStandard(packetSenders);
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

    public static void movePacketSendersToNewStandard(File dir){
        if(dir.isDirectory()) {
            for (File item : dir.listFiles()) {
                if (item.isDirectory())
                {
                    //копаем дальше в директорию
                    movePacketSendersToNewStandard(new File(dir + "/" + item.getName()));
                } else {
                    Path path = FileSystems.getDefault().getPath(item.getAbsolutePath());
                    Charset charset = StandardCharsets.UTF_8;

                    String content = "";

                    try {
                        content = new String(Files.readAllBytes(path), charset);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    String requestPatternException = "(\\w*).sendPacket\\(new (\\w*)\\((.*)\\)(, new .*)\\);";
                    Pattern pExcept = Pattern.compile(requestPatternException);
                    Matcher matcherExcept = pExcept.matcher(content);

                    //несколько пакетов
                    if (matcherExcept.find()) {
                        continue;
                    }

                    String requestPattern = "(\\w*).sendPacket\\(new (\\w*)\\((.*)\\);";
                    Pattern p = Pattern.compile(requestPattern);
                    //content = "activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST).addString(name), pook);";
                    Matcher matcher = p.matcher(content);

                    //content = " sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_FAILED));";
                    //Matcher matcher = p.matcher(content);

                    if (matcher.find()) {
                        int start = 0;

                        while (matcher.find(start)) {
                            String invoker = matcher.group(1);
                            String packet = matcher.group(2);
                            String args = matcher.group(3);

                            String invoker_new = invoker == "" ? "this, " : matcher.group(1) + ", ";
                            String args_new = args;
                            String packet_new = packet + ".class";

                            //если аргументы не пустые
                            if (args.indexOf(")") != 0){
                                packet_new = packet_new + ", ";
                            }

                            String result1 = matcher.group();
                            String resultOrdinary = matcher.group().replace(invoker, "NetworkPacketController.getInstance()");
                            resultOrdinary = resultOrdinary.replace("new " + packet + "(", invoker_new);
                            resultOrdinary = resultOrdinary.replace(args, packet_new + args_new);
                            resultOrdinary = resultOrdinary.replace("));", ");");

                            String resultSystemMessage = matcher.group().replace("new " + packet, "NetworkPacketController.getInstance().getSystemMessage");
                            resultSystemMessage = resultSystemMessage.replace(args, invoker_new + args_new);//+ packet_new + args_new);

                            //content = content.replace(matcher.group(), result);
                            start = matcher.end(3); //следующий матч после кэпчур группы 3
                            if (matcher.group().contains("SystemMessage")){
                                //System.out.println(path);
                                content = content.replace(matcher.group(), resultSystemMessage);
                            }
                            else{
                                content = content.replace(matcher.group(), resultOrdinary);
                            }
                        }

                        try {
                            Files.write(path, content.getBytes(charset));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    public static void movePacketBroadcastersToNewStandard(File dir){
        if(dir.isDirectory()) {
            for (File item : dir.listFiles()) {
                if (item.isDirectory())
                {
                    //копаем дальше в директорию
                    movePacketSendersToNewStandard(new File(dir + "/" + item.getName()));
                } else {
                    Path path = FileSystems.getDefault().getPath(item.getAbsolutePath());
                    Charset charset = StandardCharsets.UTF_8;

                    String content = "";

                    try {
                        content = new String(Files.readAllBytes(path), charset);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    String requestPatternException = "(\\w*).broadcastPacket\\(new (\\w*)\\((.*)\\)(, new .*)\\);";
                    Pattern pExcept = Pattern.compile(requestPatternException);
                    Matcher matcherExcept = pExcept.matcher(content);

                    //несколько пакетов
                    if (matcherExcept.find()) {
                        continue;
                    }

                    String requestPattern = "(\\w*).broadcastPacket\\(new (\\w*)\\((.*)\\);";
                    Pattern p = Pattern.compile(requestPattern);
                    //content = "activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_NOT_ON_YOUR_FRIEND_LIST).addString(name), pook);";
                    Matcher matcher = p.matcher(content);


                    if (matcher.find()) {
                        int start = 0;

                        while (matcher.find(start)) {
                            String invoker = matcher.group(1);
                            String packet = matcher.group(2);
                            String args = matcher.group(3);

                            String invoker_new = invoker == "" ? "this, " : matcher.group(1) + ", ";
                            String args_new = args;
                            String packet_new = packet + ".class";

                            //если аргументы не пустые
                            if (args.indexOf(")") != 0){
                                packet_new = packet_new + ", ";
                            }

                            String result1 = matcher.group();
                            String resultOrdinary = matcher.group().replace(invoker, "NetworkPacketController.getInstance()");
                            resultOrdinary = resultOrdinary.replace("new " + packet + "(", invoker_new);
                            resultOrdinary = resultOrdinary.replace(args, packet_new + args_new);
                            resultOrdinary = resultOrdinary.replace("));", ");");

                            String resultSystemMessage = matcher.group().replace("new " + packet, "NetworkPacketController.getInstance().getSystemMessage");
                            resultSystemMessage = resultSystemMessage.replace(args, invoker_new + args_new);//+ packet_new + args_new);

                            //content = content.replace(matcher.group(), result);
                            start = matcher.end(3); //следующий матч после кэпчур группы 3
                            if (matcher.group().contains("SystemMessage")){
                                //System.out.println(path);
                                content = content.replace(matcher.group(), resultSystemMessage);
                            }
                            else{
                                content = content.replace(matcher.group(), resultOrdinary);
                            }
                        }

                        try {
                            Files.write(path, content.getBytes(charset));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}