package org.example.core.models.commands.host_executor;

import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(WindowsCondition.class)
public class WindowsCommandExecutor extends CommandExecutorBase implements ICommandExecutor {
    @Override
    protected ProcessBuilder getProcessBuilder(String command) {
        return new ProcessBuilder("cmd", "/c", command);
    }
}
