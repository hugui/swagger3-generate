package com.gustavo.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocToken;

public class CommonUtil {

    public static String camelCaseToSpaceSeparated(String fieldName) {
        // 使用正则表达式将小驼峰转换成空格分隔的格式
        return fieldName.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
    }


    public static String getDocCommentText(PsiDocComment docComment) {
        StringBuilder commentText = new StringBuilder();
        for (PsiElement element : docComment.getDescriptionElements()) {
            if (element instanceof PsiDocToken) {
                commentText.append(element.getText());
            }
        }
        return commentText.toString().trim();
    }


    public static void main(String[] args) {
        String s = "camelCaseToSpaceSeparated";
        String s1 = camelCaseToSpaceSeparated(s);

        System.out.println(s1);
    }
}
