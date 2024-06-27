package com.gustavo.action;

import com.gustavo.utils.CommonUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

import java.awt.datatransfer.StringSelection;

public class CopyAdaptorToClipboardAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

        if (project == null || psiFile == null || !(psiFile instanceof PsiJavaFile)) {
            return;
        }

        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
        var psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        String javaFileName = CommonUtil.firstLetterToLowerCase(getFileNameWithoutExtension(psiJavaFile));
//        System.out.println("javaFileName: " + javaFileName);

        test(psiElement, javaFileName);
    }


    @Override
    public void update(AnActionEvent event) {
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

        if (psiFile != null && psiFile instanceof PsiJavaFile) {
            var psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
            PsiMethod method = (PsiMethod) psiElement;
            PsiType returnType = method.getReturnType();
            String typeName = getTypeName(returnType);
            event.getPresentation().setEnabledAndVisible("IRpcResult".equals(typeName));
        } else {
            event.getPresentation().setEnabledAndVisible(false);
        }
    }

    public void test(PsiElement psiElement, String javaFileName) {

        if (psiElement instanceof PsiMethod) {
//            System.out.println("PsiMethod: ");
            printMethodDetails((PsiMethod) psiElement, javaFileName);
        }
    }

    private void printMethodDetails(PsiMethod method, String javaFileName) {
        // 获取方法名称
        String methodName = method.getName();
//        System.out.println("Method Name: " + methodName);

        PsiType returnType = method.getReturnType();
        String typeName = getTypeName(returnType);
//        System.out.println("Return Type: " + typeName);
        // 获取返回类型
        String genericReturnType = printMethodGenericReturnType(method);
//        System.out.println("Generic Return Type: " + genericReturnType);

        // 获取参数
        PsiParameter[] parameters = method.getParameterList().getParameters();
        for (PsiParameter parameter : parameters) {
            PsiType parameterType = parameter.getType();
            String parameterName = parameter.getName();
//            System.out.println("Parameter: " + getTypeName(parameterType) + " " + parameterName);
        }


        StringBuilder sb = new StringBuilder();
        sb.append("public ").append(genericReturnType).append(" ").append(methodName).append("(");

        for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            PsiType parameterType = parameter.getType();
            String parameterName = parameter.getName();
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(getTypeName(parameterType)).append(" ").append(parameterName);
        }
        sb.append(") {\n");
        sb.append("IRpcResult<").append(genericReturnType).append(">").append(" ").append("rpcResult").append(" = ");
        sb.append(javaFileName).append(".").append(methodName).append("(");
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            String parameterName = parameter.getName();
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterName);
        }
        sb.append(");");
        sb.append("\nlogHelper.logRpc(() -> log.info(\"").append(javaFileName).append(".").append(methodName).append(",");
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            String parameterName = parameter.getName();
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameterName).append(":{}");
        }
        sb.append(",rpcResult:{}\",");
        for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            String parameterName = parameter.getName();
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("JSON.toJSONString(").append(parameterName).append(")");
        }
        sb.append(",JSON.toJSONString(rpcResult));");
        sb.append("\nreturn rpcResult.getData();");
        sb.append("\n}");

        System.out.println(sb.toString());

        // 复制到剪贴板
        CopyPasteManager.getInstance().setContents(new StringSelection(sb.toString()));
    }

    private String getTypeName(PsiType type) {
        if (type instanceof PsiPrimitiveType) {
            // 基本类型
            return type.getCanonicalText();
        } else if (type instanceof PsiClassType) {
            // 引用类型
            PsiClassType classType = (PsiClassType) type;
            PsiClass psiClass = classType.resolve();
            if (psiClass != null) {
                return psiClass.getName();
            } else {
                // 未解析类型
                return classType.getClassName();
            }
        } else if (type instanceof PsiArrayType) {
            // 数组类型
            PsiType componentType = ((PsiArrayType) type).getComponentType();
            return getTypeName(componentType) + "[]";
        } else {
            // 其他类型
            return type.getCanonicalText();
        }
    }

    private String printMethodGenericReturnType(PsiMethod method) {
        // 获取返回类型
        PsiType returnType = method.getReturnType();
        if (returnType != null) {
            return getGenericReturnType(returnType);
        }
        return "";
    }

    private String getGenericReturnType(PsiType type) {
        if (type instanceof PsiClassType classType) {
            PsiType[] parameters = classType.getParameters();
            if (parameters.length > 0) {
                StringBuilder result = new StringBuilder();
                String className = classType.rawType().getClassName();
                if (!"IRpcResult".equals(className)) {
                    result.append(className);
                    result.append("<");
                }
                for (int i = 0; i < parameters.length; i++) {
                    if (i > 0) {
                        result.append(", ");
                    }
                    result.append(getGenericReturnType(parameters[i]));
                }
                if (!"IRpcResult".equals(className)) {
                    result.append(">");
                }
                return result.toString();
            } else {
                return classType.rawType().getClassName();
            }
        }
        return type.getPresentableText();
    }

    public static String getFileNameWithoutExtension(PsiJavaFile psiJavaFile) {
        // 获取文件名，包括后缀
        String fileName = psiJavaFile.getName();

        // 去掉后缀，返回文件名
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }
}
