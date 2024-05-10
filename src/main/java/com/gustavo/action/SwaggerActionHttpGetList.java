package com.gustavo.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class SwaggerActionHttpGetList extends SwaggerToolAction {

    @Override
    SwaggerOperation operation() {
        return SwaggerOperation.GET_LIST;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        super.actionPerformed(event);
    }
}
