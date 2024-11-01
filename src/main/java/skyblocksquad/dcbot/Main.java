package skyblocksquad.dcbot;

import skyblocksquad.dcbot.config.ProjectsConfig;
import skyblocksquad.dcbot.config.main.Config;
import skyblocksquad.dcbot.util.JDAManager;


public class Main {

    private static final Config CONFIG = new Config();
    private static final ProjectsConfig PROJECTS_CONFIG = new ProjectsConfig();

    public static void main(String[] args) {
        CONFIG.load();

        PROJECTS_CONFIG.load();

        new JDAManager().initialize(CONFIG.getBotToken());
    }

    public static Config getConfig() {
        return CONFIG;
    }

    public static ProjectsConfig getProjectsConfig() {
        return PROJECTS_CONFIG;
    }

}