package org.example.server.models.commands.host_executor;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(UnixCondition.class)
public class UnixCommandExecutor extends CommandExecutorBase implements ICommandExecutor {
    @Override
    protected ProcessBuilder getProcessBuilder(String command) {
        return new ProcessBuilder("bash", "-c", command);
    }
}
