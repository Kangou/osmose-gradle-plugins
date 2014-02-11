package com.osmose.gradle.plugins

import org.gradle.api.Action;
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

        // Force refreshing SNAPSHOT bundles
        projectInternal.getProject().configurations.all {
            resolutionStrategy {
                cacheChangingModulesFor 0, 'seconds'
                resolutionRules.with {
                    eachModule({ moduleResolve ->
                        if (moduleResolve.request.version.endsWith('-SNAPSHOT')) {
                            // This will cause the dependency to be refreshed once per build execution
                            moduleResolve.cacheFor(0, SECONDS)
                            // This would cause the dependency to be refreshed once per sub-project in a multi-project build. You wouldn't normally want that.
                            // moduleResolve.refresh()
                        }
                    } as Action)
                    eachArtifact({ artifactResolve ->
                        if (artifactResolve.request.moduleVersionIdentifier.version.endsWith('-SNAPSHOT')) {
                            artifactResolve.cacheFor(0, SECONDS)
                        }
                    } as Action)
                }
            }
        }
    }
}
