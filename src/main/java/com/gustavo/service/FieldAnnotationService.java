package com.gustavo.service;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiUtil;

import java.util.Arrays;

import static java.util.Optional.ofNullable;

public class FieldAnnotationService {

    private final AnnotationWriteService annotationWriteService;

    public FieldAnnotationService(AnnotationWriteService annotationWriteService) {
        this.annotationWriteService = annotationWriteService;
    }

    public void generateFieldAnnotation(PsiField psiField) {
        var classType = PsiUtil.resolveClassInClassTypeOnly(psiField.getType());
        boolean required = classType == null || classType.isInterface() || hasAnyJavaxValidationAnnotation(psiField);
        annotationWriteService.doWrite("Schema", "io.swagger.v3.oas.annotations.media.Schema", String.format("@Schema(required = %s)", required), psiField);
    }

    private boolean hasAnyJavaxValidationAnnotation(PsiField psiField) {
        var annotations = Arrays.stream(ofNullable(psiField.getModifierList())
                .map(PsiAnnotationOwner::getAnnotations)
                .orElse(new PsiAnnotation[]{}));
        return annotations.anyMatch(a -> a.getQualifiedName() != null && a.getQualifiedName().contains("javax.validation.constraints"));
    }
}
