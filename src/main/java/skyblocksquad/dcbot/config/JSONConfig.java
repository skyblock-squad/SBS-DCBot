package skyblocksquad.dcbot.config;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("unused")
public abstract class JSONConfig {

    private final Path path;
    private final String resourceDefault;
    private final boolean prettyPrinting;

    public JSONConfig(Path path, String resourceDefault) {
        this(path, resourceDefault, false);
    }

    public JSONConfig(Path path, String resourceDefault, boolean prettyPrinting) {
        this.path = path;
        this.resourceDefault = resourceDefault;
        this.prettyPrinting = prettyPrinting;
    }

    protected abstract void load(JSONObject object);

    protected abstract void save(JSONObject object);

    public boolean load() {
        try {
            if (path.getParent() != null)
                Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                if (resourceDefault == null)
                    return false;
                copyDefaults();
            }
            JSONObject object;
            try (Reader reader = Files.newBufferedReader(path)) {
                object = new JSONObject(new JSONTokener(reader));
            }
            if (object.isEmpty())
                System.out.println("Warning: Config " + path + " in " + getClass().getName() + " is empty");
            load(object);
            return true;
        } catch (Exception e) {
            System.out.println("Error: Failed to load " + path + " in " + getClass().getName());
            createBackupCopy();
            return false;
        }
    }

    private void createBackupCopy() {
        try {
            String fileName = path.getFileName() + "." + System.currentTimeMillis() + ".backup";
            Path backupFile = path.getParent();
            if (backupFile == null) backupFile = Path.of(fileName);
            else backupFile = backupFile.resolve(fileName);
            Files.copy(path, backupFile);
        } catch (Exception e) {
            System.out.println("Error: Failed to create a backup of " + path + " in " + getClass().getName());
        }
    }

    private void copyDefaults() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceDefault)) {
            if (inputStream == null) return;
            Files.copy(inputStream, path);
        }
    }

    public boolean save() {
        try {
            JSONObject object = new JSONObject();
            save(object);
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                if (prettyPrinting) {
                    object.write(writer, 2, 0);
                } else {
                    object.write(writer);
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error: Failed to save " + path + " in " + getClass().getName());
            return false;
        }
    }

}