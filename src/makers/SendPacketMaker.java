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
public class SendPacketMaker implements IMaker {
    private static SendPacketMaker instance;
    public static SendPacketMaker getInstance() {
        if (instance == null) {
            instance = new SendPacketMaker();
        }
        return instance;
    }

    public void movePacketSendersToNewStandard(File dir, String exceptionPath, String innerPath) {
        if (dir.isDirectory()) {
            for (File item : dir.listFiles()) {
                if (item.isDirectory()) {
                    //копаем дальше в директорию
                    if (item.getAbsolutePath().contains("loginservercon"))
                        continue;
                    movePacketSendersToNewStandard(new File(dir + "/" + item.getName()), exceptionPath, innerPath);
                } else {
                    if (!item.getName().contains(".java")) {
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
                        String exceptionContent = "";

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

                            if (sendGavno.equals("(")) {
                                gavnoCode = true;
                            } else {
                                //не обрабатываем
                                if (sendGavno.contains("(")) {
                                    exceptionUnknown = true;
                                }
                            }

                            if (
                                    exceptionUnknown ||
                                            result.contains("LSConnection.getInstance") ||
                                            result.matches(".*sendPacket.*new.*,.*new.*") ||
                                            result.contains("getClient()")
                            ) {
                                writeException = true;
                                exceptionContent += "\n###################\nPATH: " + item.getAbsolutePath().replace(innerPath, "\\result") + "\n" + result + "\n###################";
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

                            for (char ch : args.toCharArray()) {

                                if (ch == '.' && countUnclosedBrackets == 0) {
                                    if (!result.contains("SystemMessage")) {
                                        writeException = true;
                                    }
                                    insideException = true;
                                    continue;
                                }

                                if (ch == ',' && countUnclosedBrackets == 0) {
                                    packetArgsEnd = true;
                                }

                                if (ch == '(') {
                                    countUnclosedBrackets += 1;
                                }

                                if (ch == ')') {
                                    countUnclosedBrackets -= 1;
                                }


                                if (packetArgsEnd) {
                                    nonPacketArgs += ch;
                                } else {
                                    packetArgs += ch;
                                }

                            }

                            if (insideException) {
                                exceptionContent += "\n###################\nPATH: " + item.getAbsolutePath().replace(innerPath, "\\result") + "\n" + result + "\n###################";
                                start = matcher.end(4); //следующий матч после кэпчур группы 4
                                continue;
                            }

                            packetArgs = packetArgs.substring(0, packetArgs.length() - 1);

                            if (gavnoCode) {
                                packetArgs = packetArgs.substring(0, packetArgs.length() - 1);
                            }


                            if (!packetArgs.equals("")) {
                                packet_new += ", ";
                            }

                            if (!nonPacketArgs.equals("")) {
                                start = matcher.end(4);
                                continue;
                            }

                            if (result.contains("SystemMessage")) {
                                start = matcher.end(4); //следующий матч после кэпчур группы 4
                                continue;
                            }

                            String newString = "NetworkPacketController.getInstance().sendPacket(" + invoker_new + packet_new + packetArgs + nonPacketArgs + ");";
                            content = content.replace(result, newString);
                            start = matcher.end(4); //следующий матч после кэпчур группы 4
                        }

                        if (writeException) {
                            writeExceptionFile(exceptionContent, charset, exceptionPath);
                        }
                        writeNormalFile(item, content, charset, "\\result", innerPath);

                    }
                }
            }
        }
    }
}
