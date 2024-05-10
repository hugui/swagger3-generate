package com.gustavo.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class SwaggerActionHttpPost extends SwaggerToolAction {

    @Override
    SwaggerOperation operation() {
        return SwaggerOperation.POST;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        super.actionPerformed(event);
    }
}
