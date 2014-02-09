package com.osmose.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.plugins.BasePlugin;


public class VersionPackagingPlugin implements Plugin<ProjectInternal> {

    public static final String SNAPSHOT = "-SNAPSHOT";
    public static final String BRANCH = "branch";
    public static final String DEVELOP = "develop";
    public static final String MASTER = "master";

    @Override
    public void apply(ProjectInternal projectInternal) {
        projectInternal.getPlugins().apply(BasePlugin.class);
        String version = (String) projectInternal.getVersion();
        if (projectInternal.getProperties().containsKey(BRANCH)) {
            String branch = (String) projectInternal.getProperties().get(BRANCH);
            switch (branch) {
                case DEVELOP:
                    if (!version.contains(SNAPSHOT)) {
                        // When a release branch is closed and merged on the develop one, we don't
                        // want to build and publish a non-snapshot artifact from develop.
                        version = version + SNAPSHOT;
                    }
                    break;
                case MASTER:
                    version = version.replace(SNAPSHOT, "");
                    break;
                default:
                    version = new StringBuilder().append(version.replace(SNAPSHOT, ""))
                                                 .append("-")
                                                 .append(BRANCH)
                                                 .append(SNAPSHOT).toString();
            }
            projectInternal.setVersion(version);
            System.out.println(version);
        }
    }
}
