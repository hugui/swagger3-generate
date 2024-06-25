package com.gustavo.service;

import com.google.common.base.Strings;
import com.gustavo.utils.BaiduTranslate;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.TypeConversionUtil;
import com.gustavo.utils.CommonUtil;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static com.intellij.psi.CommonClassNames.*;
import static java.util.Optional.ofNullable;

public class FieldAnnotationService {

    private final AnnotationWriteService annotationWriteService;

    public FieldAnnotationService(AnnotationWriteService annotationWriteService) {
        this.annotationWriteService = annotationWriteService;
    }

    public void generateFieldAnnotation(Project project, PsiField psiField) {
        PsiDocComment docComment = psiField.getDocComment();
        String commentText = "";
        if (docComment != null) {
            commentText = getDocCommentText(docComment);
        }
        // 判断是否有 @ApiModelProperty 注解
        String apiModelPropertyValue = getApiModelPropertyValue(psiField);

        var classType = PsiUtil.resolveClassInClassTypeOnly(psiField.getType());

        if (classType == null) {
            classType = PsiUtil.resolveClassInType(psiField.getType());
        }

        var primitive = TypeConversionUtil.isPrimitiveAndNotNull(psiField.getType());

        var list = isPsiTypeFromList(psiField.getType(), psiField.getProject());

        boolean required = (classType == null || classType.isInterface() || primitive || hasAnyJavaxOrJakartaValidationAnnotation(psiField)) && !hasAnyNullableAnnotation(psiField) && !list;

        StringBuilder sb = new StringBuilder("@Schema(");

        String description = "";
        if (!Strings.isNullOrEmpty(apiModelPropertyValue)) {
            description = apiModelPropertyValue;
        } else if (!Strings.isNullOrEmpty(commentText)) {
            description = commentText;
        } else {
            // 翻译
            String fieldName = psiField.getName();
            try {
                String convertedName = CommonUtil.camelCaseToSpaceSeparated(fieldName);
                description = BaiduTranslate.translate(convertedName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Strings.isNullOrEmpty(description)) {
            sb.append("description = ").append("\"").append(description).append("\"");
        }
        if (required) {
            sb.append(", required = true");
        }
        sb.append(")");
        annotationWriteService.doWrite("Schema", "io.swagger.v3.oas.annotations.media.Schema", sb.toString(), psiField);

        // 获取字段上的所有注解
        PsiAnnotation[] annotations = psiField.getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            // 判断是否是 @ApiModelProperty 注解
            if ("io.swagger.annotations.ApiModelProperty".equals(annotation.getQualifiedName())) {
                // 删除该注解
                annotation.delete();
            }
        }
    }

    protected boolean isPsiTypeFromList(PsiType psiFieldType, Project project) {
        if (psiFieldType instanceof PsiClassReferenceType psiClassReferenceType) {
            var resolveClass = PsiType.getTypeByName(psiClassReferenceType.getClassName(), project, GlobalSearchScope.allScope(project));
            return isPsiList(resolveClass, project, JAVA_UTIL_ARRAYS
                    , JAVA_UTIL_COLLECTIONS
                    , JAVA_UTIL_COLLECTION
                    , JAVA_UTIL_MAP
                    , JAVA_UTIL_MAP_ENTRY
                    , JAVA_UTIL_HASH_MAP
                    , JAVA_UTIL_LINKED_HASH_MAP
                    , JAVA_UTIL_SORTED_MAP
                    , JAVA_UTIL_NAVIGABLE_MAP
                    , JAVA_UTIL_CONCURRENT_HASH_MAP
                    , JAVA_UTIL_LIST
                    , JAVA_UTIL_ARRAY_LIST
                    , JAVA_UTIL_LINKED_LIST
                    , JAVA_UTIL_SET
                    , JAVA_UTIL_HASH_SET
                    , JAVA_UTIL_LINKED_HASH_SET
                    , JAVA_UTIL_SORTED_SET
                    , JAVA_UTIL_NAVIGABLE_SET
                    , JAVA_UTIL_QUEUE
                    , JAVA_UTIL_STACK);
        }
        return false;
    }

    public boolean isPsiList(PsiClassType psiClass, Project project, String... qNameOfXxx) {
        for (String qName : qNameOfXxx) {
            PsiClassType psiType = PsiType.getTypeByName(qName, project, GlobalSearchScope.allScope(project));
            if (psiType.getClassName().equalsIgnoreCase(psiClass.getClassName()) || psiType.isAssignableFrom(psiClass) || psiType.isConvertibleFrom(psiClass.rawType()) || psiType.equals(psiClass)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyNullableAnnotation(PsiField psiField) {
        var annotations = Arrays.stream(ofNullable(psiField.getModifierList())
                .map(PsiAnnotationOwner::getAnnotations)
                .orElse(new PsiAnnotation[]{}));

        return annotations.anyMatch(a -> a.getQualifiedName() != null && a.getQualifiedName().toLowerCase().contains("nullable"));
    }

    private String getApiModelPropertyValue(PsiField field) {
        PsiModifierList modifierList = field.getModifierList();
        if (modifierList != null) {
            PsiAnnotation annotation = modifierList.findAnnotation("io.swagger.annotations.ApiModelProperty");
            if (annotation != null) {
                PsiAnnotationMemberValue value = annotation.findAttributeValue("value");
                if (value instanceof PsiLiteralExpression) {
                    return ((PsiLiteralExpression) value).getValue().toString();
                }
            }
        }
        return null;
    }

    private boolean hasAnyJavaxOrJakartaValidationAnnotation(PsiField psiField) {
        var annotations = Arrays.stream(ofNullable(psiField.getModifierList())
                .map(PsiAnnotationOwner::getAnnotations)
                .orElse(new PsiAnnotation[]{}));
        return annotations.anyMatch(a -> a.getQualifiedName() != null && (a.getQualifiedName().contains("javax.validation") || a.getQualifiedName().contains("jakarta.validation")));
    }

    private String getDocCommentText(PsiDocComment docComment) {
        StringBuilder commentText = new StringBuilder();
        for (PsiElement element : docComment.getDescriptionElements()) {
            if (element instanceof PsiDocToken) {
                commentText.append(element.getText());
            }
        }
        return commentText.toString().trim();
    }
}
