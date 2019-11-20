package com.liferay.ide.idea.server.portal;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import org.jetbrains.annotations.NotNull;

public class LiferayDefaultProgramRunner implements ProgramRunner<RunnerSettings> {


    /**
     * Returns the unique ID of this runner. This ID is used to store settings and must not change between plugin versions.
     *
     * @return the program runner ID.
     */
    @Override
    public @NotNull String getRunnerId() {
        return null;
    }

    /**
     * Checks if the program runner is capable of running the specified configuration with the specified executor.
     *
     * @param executorId ID of the {@link Executor} with which the user is trying to run the configuration.
     * @param profile    the configuration being run.
     * @return true if the runner can handle it, false otherwise.
     */
    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return false;
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {

    }
}
