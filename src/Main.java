import makers.BroadcastPacketMaker;
import makers.MobiusArchivePacketMaker;
import makers.SendPacketMaker;
import java.io.File;

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
        String innerPath = "\\serverpackets";
        path = path + innerPath;
        String exceptionsPath = path.replace(innerPath, "\\result\\EXCEPTIONS\\exceptions.txt");

        MobiusArchivePacketMaker.getInstance().moveMobiusPacketsToNewStandards(new File(path.toString()), innerPath);
        //SendPacketMaker.getInstance().movePacketSendersToNewStandard(new File(path.toString()), exceptionsPath, innerPath);
        //BroadcastPacketMaker.getInstance().movePacketBroadcastersToNewStandard(new File(path.toString()), exceptionsPath, innerPath);
        //BroadcastPacketMaker.getInstance().movePacketBroadcasters2ToNewStandard(new File(path.toString()), exceptionsPath, innerPath);
    }


}