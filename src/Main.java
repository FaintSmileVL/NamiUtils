import makers.BroadcastPacketMaker;
import makers.SendPacketMaker;

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
        File clientPacketsHighFive = new File(path.toString() + "/clientpackets/highfive");

        makers.EnumBuilder.buildEnumHf(path);
        makers.EnumBuilder.buildEnumSalv(path);
*/

        /* STEP2 */
        //ПОМЕНЯТЬ ДИРЕКТОРИИ НА ХОДУ, ПРОКИДЫВАЮТСЯ ВНУТРЬ
        //String path = "C:\\Assembla\\NamiUtils\\resources";
        String path = "C:\\Users\\Admin\\IdeaProjects\\NamiUtils1\\packets";
        //String innerPath = "\\src";
        String innerPath = "\\gameserver";
        path = path + innerPath;
        File packetSenders = new File(path.toString());
        File packetSenders2 = new File(path.toString());
        File packetSenders3 = new File(path.toString());

        SendPacketMaker.getInstance().movePacketSendersToNewStandard(packetSenders, "\\result\\EXCEPTIONS", innerPath);
        BroadcastPacketMaker.getInstance().movePacketBroadcastersToNewStandard(packetSenders2, "\\result\\EXCEPTIONS", innerPath);
        BroadcastPacketMaker.getInstance().movePacketBroadcasters2ToNewStandard(packetSenders3, "\\result\\EXCEPTIONS", innerPath);
    }


}