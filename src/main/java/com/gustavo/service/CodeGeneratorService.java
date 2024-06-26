package com.gustavo.service;

import com.google.common.base.Strings;
import com.gustavo.action.SwaggerToolAction;
import com.gustavo.utils.BaiduTranslate;
import com.gustavo.utils.CommonUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.compress.utils.Lists;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

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
                if (psiFile instanceof PsiJavaFile javaFile) {
                    // 导包
                    addImportStatement(project, javaFile, "io.swagger.v3.oas.annotations.tags.Tag");
                    addImportStatement(project, javaFile, "io.swagger.v3.oas.annotations.Operation");
                }
                return;
            }

            // 类属性列表
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
                    new FieldAnnotationService(writeService).generateFieldAnnotation(psiField);
                }
            }
            if (psiFile instanceof PsiJavaFile javaFile) {
                // 导包
                addImportStatement(project, javaFile, "io.swagger.v3.oas.annotations.media.Schema");

                String description = "";
                // 获取类上的所有注解
                PsiAnnotation[] annotations = psiClass.getAnnotations();

                boolean hasSchema = Arrays.stream(annotations).anyMatch(t -> "io.swagger.v3.oas.annotations.media.Schema".equals(t.getQualifiedName()));
                if (!hasSchema) {
                    for (PsiAnnotation annotation : annotations) {
                        if ("io.swagger.annotations.ApiModel".equals(annotation.getQualifiedName())) {
                            PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
                            if (value instanceof PsiLiteralExpression) {
                                description = Objects.requireNonNull(((PsiLiteralExpression) value).getValue()).toString();
                            }
                            break;
                        }
                    }
                    PsiDocComment docComment = psiClass.getDocComment();
                    if (docComment != null) {
                        String commentText = CommonUtil.getDocCommentText(docComment);
                        description = Strings.isNullOrEmpty(commentText) ? description : commentText;
                    }
                    if (Strings.isNullOrEmpty(description)) {
                        try {
                            String convertedName = CommonUtil.camelCaseToSpaceSeparated(psiClass.getName());
                            description = BaiduTranslate.translate(convertedName);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // 加注解
                    writeService.doWrite("Schema", "io.swagger.v3.oas.annotations.media.Schema", "@Schema(" + "description = " + "\"" + description + "\"", psiClass);
                }
            }

            // 处理内部类
            PsiClass[] innerClasses = psiClass.getInnerClasses();
            if (innerClasses.length > 0) {
                for (PsiClass innerClass : innerClasses) {
                    boolean isEnum = innerClass.isEnum();
                    if (isEnum) {
                        continue;
                    }
                    new CodeGeneratorService(project, innerClass, psiFile).generate();
                }
            }
        });
    }


    public void generate(PsiElement psiElement, SwaggerToolAction.SwaggerOperation operation) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            var writeService = new AnnotationWriteService(project);

            if (psiElement instanceof PsiClass clazz) {
                new ClassAnnotationService(writeService).generateClassAnnotation(clazz);

                if (psiFile instanceof PsiJavaFile javaFile) {
                    // 导包
                    addImportStatement(project, javaFile, "io.swagger.v3.oas.annotations.tags.Tag");
                }
            }

            if (psiElement instanceof PsiMethod) {
                new MethodAnnotationService(writeService).generateMethodAnnotation((PsiMethod) psiElement, operation);

                if (psiFile instanceof PsiJavaFile javaFile) {
                    // 导包
                    addImportStatement(project, javaFile, "io.swagger.v3.oas.annotations.Operation");
                }
            }

            if (psiElement instanceof PsiField) {
                new FieldAnnotationService(writeService).generateFieldAnnotation((PsiField) psiElement);
            }

        });
    }

    public void addImportStatement(Project project, PsiJavaFile javaFile, String clazzRef) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiImportList importList = javaFile.getImportList();

        if (importList != null) {
            PsiClass schemaClass = JavaPsiFacade.getInstance(project)
                    .findClass(clazzRef, GlobalSearchScope.allScope(project));

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
