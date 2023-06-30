package makers;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nami
 * @date 14.05.2023
 * @time 11:46
 */
public class EnumMaker {
    public static void buildServerPacketsEnumSalv(String path) {
        File serverPacketsSalvation = new File(path.toString() + "/serverpackets/enums");
        LinkedList<String> serverPacketsSalvationEnumList = collectToList(serverPacketsSalvation);
        writeToFile(serverPacketsSalvationEnumList, "EServerPacketsSalvation");
    }

    private static String pattern = "([A-Z_]+)\\((.+)\\)";
    public static LinkedList<String> collectToList(File dir) {
        LinkedList<String> result = new LinkedList<>();
        if (!dir.isDirectory()) {
            System.out.println(dir.getName() + "is not a directory");
        }
        int i = 0;
        for (File file : dir.listFiles()) {
            System.out.println(file.getName());
            i += 1;

            File myFile = new File(file.getAbsolutePath());
            try (FileReader reader = new FileReader(myFile)) {
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Pattern p = Pattern.compile(pattern);
                    Matcher matcher = p.matcher(line);
                    if (matcher.find()) {
                        String name = matcher.group(1);
                        String className = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, matcher.group(1));
                        String opCodes = matcher.group(2);
                        result.addLast(name + "(" + opCodes + ", " + className + ".class" + "),");
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
        System.out.println("Processed " + i + " files");
        return result;
    }

    /**
     * Writing ENUM
     *
     * @param list
     * @param name
     */
    public static void writeToFile(LinkedList<String> list, String name) {
        try (FileWriter writer = new FileWriter(name + ".java", false)) {
            writer.write("public enum " + name + " {\n");
            for (String content : list) {
                writer.write("\t" + content + "\n");
            }
            writer.write("}");
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
