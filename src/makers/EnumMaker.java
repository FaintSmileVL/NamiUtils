package makers;

import java.io.*;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * @author Nami
 * @date 14.05.2023
 * @time 11:46
 */
public class EnumMaker {
    public static void buildEnumHf(String path) {
        File serverPacketsHighFive = new File(path.toString() + "/serverpackets/highfive");
        LinkedList<Pair> serverPacketsHighFiveEnumList = collectToList(serverPacketsHighFive);
        writeToFile(serverPacketsHighFiveEnumList, "EServerPackets");
    }

    public static void buildEnumSalv(String path) {
        File serverPacketsSalvation = new File(path.toString() + "/serverpackets/salvation");
        LinkedList<Pair> serverPacketsSalvationEnumList = collectToList(serverPacketsSalvation);
        writeToFile(serverPacketsSalvationEnumList, "EServerPacketsSalvation");
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
     *
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
}
