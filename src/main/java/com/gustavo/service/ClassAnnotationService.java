package com.gustavo.service;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ClassAnnotationService {

    private static final Set<String> CONTROLLER_ANNOTATION = Set.of("Controller", "RestController", "org.springframework.web.bind.annotation.RestController", "org.springframework.stereotype.Controller");

    private final AnnotationWriteService annotationWriteService;

    public ClassAnnotationService(AnnotationWriteService annotationWriteService) {
        this.annotationWriteService = annotationWriteService;
    }

    public void generateClassAnnotation(PsiClass psiClass, boolean isController) {
        if (isController && !hasAnyApiAnnotation(psiClass)) {
            var className = psiClass.getQualifiedName();
            var annotationFromText = String.format("@Tag(name = %s)", className);
            annotationWriteService.doWrite("Tag", "io.swagger.v3.oas.annotations.tags.Tag", annotationFromText, psiClass);
        }
    }

    private boolean hasAnyApiAnnotation(PsiClass psiClass) {
        var annotations = Arrays.stream(Optional.of(psiClass.getAnnotations())
                .orElse(new PsiAnnotation[]{}));
        return annotations.anyMatch(a -> a.getQualifiedName() != null && a.getQualifiedName().contains("io.swagger.v3.oas.annotations."));
    }

    public void generateClassAnnotation(PsiClass psiClass) {
        var isController = isController(psiClass);
        generateClassAnnotation(psiClass, isController);
    }

    public static boolean isController(PsiClass psiClass) {
        if (psiClass.getName() != null && psiClass.getName().toLowerCase().contains("controller")) {
            return true;
        }
        PsiAnnotation[] psiAnnotations = Objects.requireNonNull(psiClass.getModifierList()).getAnnotations();
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            if (CONTROLLER_ANNOTATION.contains(psiAnnotation.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

}
