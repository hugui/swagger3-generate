package com.gustavo.service;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.TypeConversionUtil;

import java.util.Arrays;

import static com.intellij.psi.CommonClassNames.*;
import static java.util.Optional.ofNullable;

public class FieldAnnotationService {

    private final AnnotationWriteService annotationWriteService;

    public FieldAnnotationService(AnnotationWriteService annotationWriteService) {
        this.annotationWriteService = annotationWriteService;
    }

    public void generateFieldAnnotation(PsiField psiField) {
        var classType = PsiUtil.resolveClassInClassTypeOnly(psiField.getType());

        if (classType == null) {
            classType = PsiUtil.resolveClassInType(psiField.getType());
        }

        var primitive = TypeConversionUtil.isPrimitiveAndNotNull(psiField.getType());

        var list = isPsiTypeFromList(psiField.getType(), psiField.getProject());

        boolean required = (classType == null || classType.isInterface() || primitive || hasAnyJavaxOrJakartaValidationAnnotation(psiField)) && !hasAnyNullableAnnotation(psiField) && !list;
        annotationWriteService.doWrite("Schema", "io.swagger.v3.oas.annotations.media.Schema", String.format("@Schema(required = %s)", required), psiField);
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

    private boolean hasAnyJavaxOrJakartaValidationAnnotation(PsiField psiField) {
        var annotations = Arrays.stream(ofNullable(psiField.getModifierList())
                .map(PsiAnnotationOwner::getAnnotations)
                .orElse(new PsiAnnotation[]{}));
        return annotations.anyMatch(a -> a.getQualifiedName() != null && (a.getQualifiedName().contains("javax.validation") || a.getQualifiedName().contains("jakarta.validation")));
    }

}
