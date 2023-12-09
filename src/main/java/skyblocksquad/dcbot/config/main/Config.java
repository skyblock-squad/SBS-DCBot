package skyblocksquad.dcbot.config.main;

import org.json.JSONObject;
import skyblocksquad.dcbot.config.JSONConfig;

import java.nio.file.Path;

@SuppressWarnings("unused")
public class Config extends JSONConfig {

    private String botToken;
    private long welcomeChannelId, logsChannelId, voiceLogsChannelId, memberRoleId, pingRolesNewsPingRoleId;
    private boolean silentLogMessages;

    public Config() {
        super(Path.of("config.json"), "default-config.json");
    }

    public String getBotToken() {
        return botToken;
    }

    public long getWelcomeChannelId() {
        return welcomeChannelId;
    }

    public long getLogsChannelId() {
        return logsChannelId;
    }

    public long getVoiceLogsChannelId() {
        return voiceLogsChannelId;
    }

    public long getMemberRoleId() {
        return memberRoleId;
    }

    public long getPingRolesNewsPingRoleId() {
        return pingRolesNewsPingRoleId;
    }

    public boolean isSilentLogMessages() {
        return silentLogMessages;
    }

    @Override
    protected void load(JSONObject object) {
        botToken = object.getString("botToken");
        welcomeChannelId = object.getLong("welcomeChannelId");
        logsChannelId = object.getLong("logsChannelId");
        voiceLogsChannelId = object.getLong("voiceLogsChannelId");
        memberRoleId = object.getLong("memberRoleId");
        pingRolesNewsPingRoleId = object.getLong("pingRolesNewsPingRoleId");
        silentLogMessages = object.getBoolean("silentLogMessages");
    }

    @Override
    protected void save(JSONObject object) {
        throw new IllegalStateException("Config can not be saved");
    }

}
