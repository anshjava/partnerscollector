package ru.kamuzta.partnerscollector.model;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final String INPUT_FILE_PATH = "D:/javaprojects/files/ozru_partners.html";
    private static final String OUTPUT_FILE_PATH = "D:/javaprojects/files/ozru_partners_decoded.html";

    public static String readFile(String filePath) {
        String line = "";
        try (FileReader fr = new FileReader(filePath)) {
            while (fr.ready()) {
                line = line + String.valueOf((char) fr.read());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }

    public static String decodeFile(String s) {
        StringBuilder sb = new StringBuilder();

        Pattern p = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher m = p.matcher(s);

        int lastIndex = 0;
        while (m.find()) {

            sb.append(s.substring(lastIndex, m.start()));
            lastIndex = m.end();

            sb.append((char)Integer.parseInt(m.group(1), 16));
        }

        if (lastIndex < s.length())
            sb.append(s.substring(lastIndex));

        return sb.toString();
    }

    public static void writeFile(String filePath, String text) {
        try (FileWriter fileWriter = new FileWriter(new File(filePath))) {
            fileWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
