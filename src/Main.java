import makers.EnumMaker;

/**
 * @author ${USER}
 * @date ${DATE}
 * @time ${TIME}
 */
public class Main {
    public static void main(String[] args) {
        int a = 0;
        int b = 0;
        double g1 = Math.random(), g2 = Math.random(), g3 = Math.random(), g4 = Math.random();
        for (int j = 0; j < 100; j++) {
            TestClass[] array = new TestClass[100000];
            String[] arrayS = new String[100000];
            long begin = System.currentTimeMillis();
            for (int i = 0; i < 100000; i++) {
                array[i] = new TestClass(g1, g2, g3, g4);
            }
            long curr = System.currentTimeMillis();
            for (int i = 0; i < 100000; i++) {
                arrayS[i] = new String("prefix " + g1 + " " + g2 + " " + g3 + " " + g4);
            }
            long end = System.currentTimeMillis();
            a += end - curr;
            b += curr - begin;
        }
        System.out.println(a / 100);
        System.out.println(b / 100);
    }

    public static void main2(String[] args) {
        /* STEP1 */
        String clientPacksPath = "C:\\Assembla\\NamiUtils\\resources";
        //EnumMaker.buildEnumHf(clientPacksPath);
        EnumMaker.buildServerPacketsEnumSalv(clientPacksPath);

        /* STEP2 */
        //ПОМЕНЯТЬ ДИРЕКТОРИИ НА ХОДУ, ПРОКИДЫВАЮТСЯ ВНУТРЬ
        String path = "C:\\Assembla\\NamiUtils\\resources";
        //String path = "C:\\Users\\Admin\\IdeaProjects\\NamiUtils1\\packets";
        String innerPath = "\\src";
        //String innerPath = "\\gameserver";
        path = path + innerPath;
        String exceptionsPath = path.replace(innerPath, "\\result\\EXCEPTIONS\\exceptions.txt");

        //MobiusArchivePacketMaker.getInstance().moveMobiusPacketsToNewStandards(new File(path.toString()), innerPath);
        //SendPacketMaker.getInstance().movePacketSendersToNewStandard(new File(path.toString()), exceptionsPath, innerPath);
        //BroadcastPacketMaker.getInstance().movePacketBroadcastersToNewStandard(new File(path.toString()), exceptionsPath, innerPath);
        //BroadcastPacketMaker.getInstance().movePacketBroadcasters2ToNewStandard(new File(path.toString()), exceptionsPath, innerPath);
    }


}