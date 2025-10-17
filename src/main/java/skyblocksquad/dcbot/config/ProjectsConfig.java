package skyblocksquad.dcbot.config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProjectsConfig extends JSONConfig {

    private final List<Project> projects = new ArrayList<>();

    public ProjectsConfig() {
        super(Path.of("projects.json"), null);
    }

    @Override
    protected void load(JSONObject object) {
        JSONArray projectsArray = object.getJSONArray("projects");
        for (Object projectObject : projectsArray)
            if (projectObject instanceof JSONObject projectJson)
                projects.add(Project.load(projectJson));

        projects.sort(Comparator.comparingLong(Project::getDateCreated));
    }

    @Override
    protected void save(JSONObject object) {
        JSONArray projectsArray = new JSONArray();
        for (Project project : projects) {
            JSONObject jsonProject = new JSONObject();
            project.save(jsonProject);
            projectsArray.put(jsonProject);
        }
        object.put("projects", projectsArray);
    }

    public List<Project> getProjects() {
        return projects;
    }

}