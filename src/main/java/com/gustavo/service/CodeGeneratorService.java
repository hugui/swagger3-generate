package com.gustavo.service;

import com.gustavo.action.SwaggerToolAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

public class CodeGeneratorService {

    private final Project project;
    private final PsiClass psiClass;

    public CodeGeneratorService(Project project, PsiClass psiClass) {
        this.project = project;
        this.psiClass = psiClass;
    }

    public void generate() {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            var writeService = new AnnotationWriteService(project);
            var isController = ClassAnnotationService.isController(psiClass);
            new ClassAnnotationService(writeService).generateClassAnnotation(psiClass, isController);

            if (isController) {
                PsiMethod[] methods = psiClass.getMethods();
                for (PsiMethod psiMethod : methods) {
                    new MethodAnnotationService(writeService).generateMethodAnnotation(psiMethod);
                }
                return;
            }

            PsiField[] field = psiClass.getAllFields();
            for (PsiField psiField : field) {
                new FieldAnnotationService(writeService).generateFieldAnnotation(psiField);
            }
        });
    }


    public void generate(PsiElement psiElement, SwaggerToolAction.SwaggerOperation operation) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            var writeService = new AnnotationWriteService(project);

            if (psiElement instanceof PsiClass clazz) {
                new ClassAnnotationService(writeService).generateClassAnnotation(clazz);
            }

            if (psiElement instanceof PsiMethod) {
                new MethodAnnotationService(writeService).generateMethodAnnotation((PsiMethod) psiElement, operation);
            }

            if (psiElement instanceof PsiField) {
                new FieldAnnotationService(writeService).generateFieldAnnotation((PsiField) psiElement);
            }

        });
    }
}
