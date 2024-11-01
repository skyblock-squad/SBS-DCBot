package skyblocksquad.dcbot.util;

import org.json.JSONObject;

public class Project {

    public static Project load(JSONObject object) {
        Project project = new Project();

        project.setDateCreated(object.getLong("dateCreated"));
        project.setTitle(object.getString("title"));
        project.setThumbnail(object.optString("thumbnail", null));
        project.setVideoUrl(object.getString("videoUrl"));
        project.setPmcUrl(object.getString("pmcUrl"));
        project.setMcVersion(object.getString("mcVersion"));
        project.setDownloadUrl(object.optString("downloadUrl", null));

        return project;
    }

    private long dateCreated;
    private String title;
    private String thumbnail;
    private String videoUrl;
    private String pmcUrl;
    private String mcVersion;
    private String downloadUrl;

    public void save(JSONObject object) {
        object.put("dateCreated", getDateCreated());
        object.put("title", getTitle());
        object.put("thumbnail", getThumbnail());
        object.put("videoUrl", getVideoUrl());
        object.put("pmcUrl", getPmcUrl());
        object.put("mcVersion", getMcVersion());
        object.put("downloadUrl", getDownloadUrl());
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getPmcUrl() {
        return pmcUrl;
    }

    public void setPmcUrl(String pmcUrl) {
        this.pmcUrl = pmcUrl;
    }

    public String getMcVersion() {
        return mcVersion;
    }

    public void setMcVersion(String mcVersion) {
        this.mcVersion = mcVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

}
