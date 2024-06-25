package com.gustavo.conf;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AppSettingsComponent {

    private final JPanel myMainPanel;

    private final JBTextField myUserNameText = new JBTextField();
    private final JBTextField secretKeyText = new JBTextField();
    private final JBTextField appidText = new JBTextField();

    private final JBCheckBox myIdeaUserStatus = new JBCheckBox("Do you use IntelliJ IDEA? ");
    private final JBRadioButton fanyi = new JBRadioButton("baidu");
    private final JBRadioButton fanyi1 = new JBRadioButton("google");

    public AppSettingsComponent() {
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("百度翻译APPID"), appidText)
                .addLabeledComponent(new JBLabel("百度翻译秘钥"), secretKeyText)
//                .addLabeledComponent(new JBLabel("Enter user name: "), myUserNameText, 1, false)
//                .addComponent(myIdeaUserStatus, 1)
//                .addComponent(fanyi, 1)
//                .addComponent(fanyi1, 1)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return myUserNameText;
    }

    @NotNull
    public String getUserNameText() {
        return myUserNameText.getText();
    }

    public void setUserNameText(@NotNull String newText) {
        myUserNameText.setText(newText);
    }

    public boolean getIdeaUserStatus() {
        return myIdeaUserStatus.isSelected();
    }

    public void setIdeaUserStatus(boolean newStatus) {
        myIdeaUserStatus.setSelected(newStatus);
    }

    public String getAppidText() {
        return appidText.getText();
    }

    public String getSecretKeyText() {
        return secretKeyText.getText();
    }

    public void setSecretKeyText(@NotNull String newText) {
        secretKeyText.setText(newText);
    }

    public void setAppidText(@NotNull String newText) {
        appidText.setText(newText);
    }
}