package makers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nami
 * @date 14.05.2023
 * @time 11:49
 */
public class MobiusArchivePacketMaker implements IMaker {
    private static MobiusArchivePacketMaker instance;

    public static MobiusArchivePacketMaker getInstance() {
        if (instance == null) {
            instance = new MobiusArchivePacketMaker();
        }
        return instance;
    }

    public void moveMobiusPacketsToNewStandards(File dir, String innerPath) {
        if (dir.isDirectory()) {
            for (File item : dir.listFiles()) {
                if (item.isDirectory()) {
                    moveMobiusPacketsToNewStandards(new File(dir + "/" + item.getName()), innerPath);
                } else {
                    if (!item.getName().contains(".java")) {
                        continue;
                    }

                    Path path = FileSystems.getDefault().getPath(item.getAbsolutePath());
                    Charset charset = StandardCharsets.UTF_8;
                    String content = "";

                    try {
                        if (Files.exists(path)) {
                            content = new String(Files.readAllBytes(path), charset);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    String writeMethod = FindWriteMethod(content);
                    String replaceMethod = writeMethod;

                    replaceMethod = changePacketEnumName(replaceMethod);
                    replaceMethod = clearWriteId(replaceMethod);
                    replaceMethod = clearWrite(replaceMethod);
                    replaceMethod = changeWrite(replaceMethod);
                    replaceMethod = removeBoolean(replaceMethod);

                    content = content.replace(writeMethod, replaceMethod);

                    writeNormalFile(item, content, charset, "\\result", innerPath);
                }
            }
        }
    }

    //Находит метод write в пакете, чтобы делать изменения только в нём
    public String FindWriteMethod(String content){
        String method = "";
        if (!content.contains("public boolean write"))
            return "";

        int startIndex = content.indexOf("public boolean write");
        String partialContent = content.substring(startIndex);
        int countUnclosedBrackets = 0;
        boolean endRead = false;

        for (char ch : partialContent.toCharArray()){
            if (ch == '}') {
                countUnclosedBrackets -= 1;
                method += ch;
                if (countUnclosedBrackets == 0) {
                    return method;
                }
                continue;
            }
            if (ch == '{'){
                countUnclosedBrackets += 1;
            }
            method += ch;
        }
        return "";
    }

    //Меняет имя пакет енума
    public String changePacketEnumName(String content) {
        String requestPattern = "OutgoingPackets\\.(.*)\\.writeId(.*);";
        Pattern p = Pattern.compile(requestPattern);
        Matcher matcher = p.matcher(content);
        String packetENUMName = "";
        if (matcher.find()) {
            packetENUMName = matcher.group(1);
        }
        return content.replace(packetENUMName, packetENUMName);
    }

    //убирает полностью строку OutgoingPackets.*.writeId(*);
    public String clearWriteId(String content) {
        String requestPattern = "OutgoingPackets\\.(.*)\\.writeId(.*);";
        return content.replaceAll(requestPattern, "");
    }

    //меняет packet.write на write
    public String clearWrite(String content) {
        String requestPattern = "packet\\.write";
        return content.replaceAll(requestPattern, "write");
    }

    //меняет составляющие packet.write*(*);
    public String changeWrite(String content) {
        String requestPattern = "packet\\.write(\\S*)\\((.*)\\);";
        Pattern p = Pattern.compile(requestPattern);
        Matcher matcher = p.matcher(content);

        if (matcher.find()) {
            int start = 0;
            String exceptionContent = "";

            while (matcher.find(start)) {
                String result = matcher.group();
                String writeType = matcher.group(1);
                String args = matcher.group(2);

                String newString = result;
                content = content.replace(result, newString);
                start = matcher.end(2); //следующий матч после кэпчур группы 2
            }
        }
        return content;
    }

    public String removeBoolean(String content){
        content = content.replace("public boolean write", "public void write");
        content = content.replace("return true", "return");
        content = content.replace("return false", "return");
        return content;
    }
}
