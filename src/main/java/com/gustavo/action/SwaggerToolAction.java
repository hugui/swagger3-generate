package com.gustavo.action;

import com.gustavo.service.CodeGeneratorService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.gustavo.constants.SwaggerAnnotationsText.DELETE_ANNOTATION_TEXT;
import static com.gustavo.constants.SwaggerAnnotationsText.GET_ANNOTATION_TEXT;
import static com.gustavo.constants.SwaggerAnnotationsText.GET_LIST_ANNOTATION_TEXT;
import static com.gustavo.constants.SwaggerAnnotationsText.POST_ANNOTATION_TEXT;
import static com.gustavo.constants.SwaggerAnnotationsText.PUT_ANNOTATION_TEXT;


public abstract class SwaggerToolAction extends AnAction {

    abstract SwaggerOperation operation();

    public enum SwaggerOperation {
        GET(GET_ANNOTATION_TEXT),
        GET_LIST(GET_LIST_ANNOTATION_TEXT),
        POST(POST_ANNOTATION_TEXT),
        PUT(PUT_ANNOTATION_TEXT),
        DELETE(DELETE_ANNOTATION_TEXT);

        SwaggerOperation(String annotationText) {
            this.annotationText = annotationText;
        }

        public String getAnnotationText() {
            return annotationText;
        }

        private final String annotationText;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();

        if(Objects.isNull(project)){
            return;
        }

        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (Objects.isNull(editor)) {
            return;
        }

        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        PsiClass psiClass = PsiTreeUtil.findChildOfAnyType(psiFile, PsiClass.class);
        var psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        new CodeGeneratorService(project, psiClass).generate(psiElement, operation());
    }
}
