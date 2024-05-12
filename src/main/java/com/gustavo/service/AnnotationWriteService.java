package com.gustavo.service;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiNameValuePair;

import java.util.Objects;

public class AnnotationWriteService {

    private final Project project;

    public AnnotationWriteService(Project project) {
        this.project = project;
    }

    public void doWrite(String name,
                        String qualifiedName,
                        String annotationText,
                        PsiModifierListOwner psiModifierListOwner) {

        var elementFactory = JavaPsiFacade.getElementFactory(project);
        PsiAnnotation psiAnnotationDeclare = elementFactory.createAnnotationFromText(annotationText, psiModifierListOwner);

        final PsiNameValuePair[] attributes = psiAnnotationDeclare.getParameterList().getAttributes();
        PsiAnnotation existAnnotation = Objects.requireNonNull(psiModifierListOwner.getModifierList()).findAnnotation(name);

        if (Objects.isNull(existAnnotation)) {
            existAnnotation = Objects.requireNonNull(psiModifierListOwner.getModifierList()).findAnnotation(qualifiedName);
        }
        if (existAnnotation != null) {
            existAnnotation.delete();
        }

        PsiAnnotation psiAnnotation = psiModifierListOwner.getModifierList().addAnnotation(name);
        for (PsiNameValuePair pair : attributes) {
            psiAnnotation.setDeclaredAttributeValue(pair.getName(), pair.getValue());
        }
    }

}
