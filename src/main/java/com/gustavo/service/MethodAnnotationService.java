package com.gustavo.service;

import com.gustavo.action.SwaggerToolAction;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;

import java.util.Arrays;
import java.util.stream.Stream;

public class MethodAnnotationService {

    private final AnnotationWriteService annotationWriteService;

    public MethodAnnotationService(AnnotationWriteService annotationWriteService) {
        this.annotationWriteService = annotationWriteService;
    }

    public void generateMethodAnnotation(PsiMethod psiMethod, SwaggerToolAction.SwaggerOperation operation) {
        PsiAnnotation apiOperationExist = psiMethod.getModifierList().findAnnotation("io.swagger.v3.oas.annotations.Operation");
        if (apiOperationExist == null) {
            annotationWriteService.doWrite("Operation", "io.swagger.v3.oas.annotations.Operation", operation.getAnnotationText(), psiMethod);
        }
    }

    public void generateMethodAnnotation(PsiMethod psiMethod) {
        var annotations = Arrays.stream(psiMethod.getAnnotations());
        var operation = autoQualifyByAnnotationName(annotations);
        generateMethodAnnotation(psiMethod, operation);
    }

    private SwaggerToolAction.SwaggerOperation autoQualifyByAnnotationName(Stream<PsiAnnotation> annotations) {
//        if (matchHttpMethodAnnotation(annotations, SwaggerToolAction.SwaggerOperation.GET)) {
//            return SwaggerToolAction.SwaggerOperation.GET_LIST;
//        }
//
//        if (matchHttpMethodAnnotation(annotations, SwaggerToolAction.SwaggerOperation.POST)) {
//            return SwaggerToolAction.SwaggerOperation.POST;
//        }
//
//        if (matchHttpMethodAnnotation(annotations, SwaggerToolAction.SwaggerOperation.PUT)) {
//            return SwaggerToolAction.SwaggerOperation.PUT;
//        }
//
//        if (matchHttpMethodAnnotation(annotations, SwaggerToolAction.SwaggerOperation.DELETE)) {
//            return SwaggerToolAction.SwaggerOperation.DELETE;
//        }
//
//        return SwaggerToolAction.SwaggerOperation.GET;

        return SwaggerToolAction.SwaggerOperation.SIMPLE;
    }

    private boolean matchHttpMethodAnnotation(Stream<PsiAnnotation> annotations, SwaggerToolAction.SwaggerOperation operation) {
        return annotations.anyMatch(a -> a.getQualifiedName() != null && a.getQualifiedName().toLowerCase().contains(operation.name().toLowerCase()));
    }
}
