package com.gustavo.service;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.gustavo.action.SwaggerToolAction;

public class MethodAnnotationService {

    private final AnnotationWriteService annotationWriteService;

    public MethodAnnotationService(AnnotationWriteService annotationWriteService) {
        this.annotationWriteService = annotationWriteService;
    }

    public void generateMethodAnnotation(PsiMethod psiMethod, SwaggerToolAction.SwaggerOperation operation) {
        PsiAnnotation swaggerErrors = psiMethod.getModifierList().findAnnotation("controllers.extranet.users.CommonSwaggerErrors");
        if (swaggerErrors == null) {
            annotationWriteService.doWrite("CommonSwaggerErrors", "controllers.extranet.users.CommonSwaggerErrors", "@CommonSwaggerErrors", psiMethod);
        }

        PsiAnnotation apiOperationExist = psiMethod.getModifierList().findAnnotation("io.swagger.v3.oas.annotations.Operation");
        if (apiOperationExist == null) {
            annotationWriteService.doWrite("Operation", "io.swagger.v3.oas.annotations.Operation", operation.getAnnotationText(), psiMethod);
        }
    }

    public void generateMethodAnnotation(PsiMethod psiMethod) {
        generateMethodAnnotation(psiMethod, SwaggerToolAction.SwaggerOperation.GET);
    }
}
