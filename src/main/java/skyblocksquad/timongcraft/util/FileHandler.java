package skyblocksquad.timongcraft.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHandler {
    private final Map<String, String> config;

    public FileHandler(String fileName) { config = parseSimpleYaml(fileName); }

    public String getString(String key) {
        return config.get(key);
    }

    public int getInt(String key) {
        String value = config.get(key);
        if(value != null) {
            return Integer.parseInt(value);
        }
        return 0;
    }

    public boolean getBoolean(String key) {
        String value = config.get(key);
        if(value != null) {
            return Boolean.parseBoolean(value);
        }
        return false;
    }

    public static List<String> readFile(String fileName) {
        try {
            return Files.readAllLines(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void appendToFile(String fileName, String content) {
        try(FileWriter fw = new FileWriter(fileName, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeLineFromFile(String fileName, String lineToRemove) {
        File file = new File(fileName);
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        try(BufferedReader br = new BufferedReader(new FileReader(file));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String currentLine;
            while((currentLine = br.readLine()) != null) {
                if(!currentLine.equals(lineToRemove)) {
                    bw.write(currentLine);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!file.delete()) {
            System.out.println("[DC-Bot] §cCould not delete " + fileName + " file while updating");
        }

        if(!tempFile.renameTo(file)) {
            System.out.println("[DC-Bot] §cCould not rename " + fileName + " file while updating");
        }
    }

    private Map<String, String> parseSimpleYaml(String fileName) {
        Map<String, String> yamlData = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while((line = br.readLine()) != null) {
                if(line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                String[] keyValue = line.split(":", 2);
                if(keyValue.length == 2) {
                    yamlData.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return yamlData;
    }
}