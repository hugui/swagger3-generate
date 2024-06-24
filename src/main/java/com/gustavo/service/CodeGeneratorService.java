package com.gustavo.service;

import com.gustavo.action.SwaggerToolAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;

public class CodeGeneratorService {

    private final Project project;
    private final PsiClass psiClass;
    private final PsiFile psiFile;

    public CodeGeneratorService(Project project, PsiClass psiClass, PsiFile psiFile) {
        this.project = project;
        this.psiClass = psiClass;
        this.psiFile = psiFile;
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
                if ("serialVersionUID".equals(psiField.getName())) {
                    // 排除 serialVersionUID 字段
                    continue;
                }
                boolean hasSchema = false;
                PsiAnnotation[] annotations = psiField.getAnnotations();
                for (PsiAnnotation annotation : annotations) {
                    if ("io.swagger.v3.oas.annotations.media.Schema".equals(annotation.getQualifiedName())) {
                        hasSchema = true;
                        break;
                    }
                }
                if (!hasSchema) {
                    new FieldAnnotationService(writeService).generateFieldAnnotation(project, psiField);
                }
            }
            // 导包
            if (psiFile instanceof PsiJavaFile javaFile) {
                addImportStatement(project, javaFile);
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
                new FieldAnnotationService(writeService).generateFieldAnnotation(project, (PsiField) psiElement);
            }

        });
    }

    public void addImportStatement(Project project, PsiJavaFile javaFile) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiImportList importList = javaFile.getImportList();

        if (importList != null) {
            PsiClass schemaClass = JavaPsiFacade.getInstance(project)
                    .findClass("io.swagger.v3.oas.annotations.media.Schema", GlobalSearchScope.allScope(project));

            if (schemaClass != null && !isImported(importList, schemaClass)) {
                PsiImportStatement importStatement = elementFactory.createImportStatement(schemaClass);
                importList.add(importStatement);
            }
        }
    }

    private boolean isImported(PsiImportList importList, PsiClass schemaClass) {
        for (PsiImportStatement importStatement : importList.getImportStatements()) {
            PsiJavaCodeReferenceElement reference = importStatement.getImportReference();
            if (reference != null && reference.getQualifiedName() != null &&
                    reference.getQualifiedName().equals(schemaClass.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }
}
