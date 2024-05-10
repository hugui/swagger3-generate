package com.gustavo.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class SwaggerActionHttpPut extends SwaggerToolAction {

    @Override
    SwaggerOperation operation() {
        return SwaggerOperation.PUT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        super.actionPerformed(event);
    }
}
