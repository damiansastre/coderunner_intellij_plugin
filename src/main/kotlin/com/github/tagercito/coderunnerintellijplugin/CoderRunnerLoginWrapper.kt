package com.github.tagercito.coderunnerintellijplugin

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.customization.UtmIdeUrlTrackingParametersProvider
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField

class CoderRunnerLoginWrapper : DialogWrapper( true) {

    private val panel = JPanel(GridBagLayout())
    private var txtUsername = JTextField()
    private var txtBaseUrl = JTextField()
    private var txtPassword = JPasswordField()

    init {
        init();
        title = "Demo Data"
        val state = CodeRunnerPluginSettings.getInstance().state
        try{
            val credentialAttributes = CredentialAttributes("CodeRunnerPlugin")
            val credentials = PasswordSafe.instance.get(credentialAttributes)
            txtPassword.text = credentials?.getPasswordAsString()
            txtUsername.text = credentials?.userName.toString()
            txtBaseUrl.text = state?.base_url;
        } catch (e: Exception){
            print(e)
        }
    }
    override fun createCenterPanel(): JComponent? {
        val gb = GridBag()
            .setDefaultInsets(Insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))
            .setDefaultWeightX(1.0)
            .setDefaultFill(GridBagConstraints.HORIZONTAL)

        panel.preferredSize = Dimension(400, 200)
        panel.add(label("BASE URL"), gb.nextLine().next().weightx(0.2))
        panel.add(txtBaseUrl, gb.next().weightx(0.8))
        panel.add(label("Username"), gb.nextLine().next().weightx(0.2))
        panel.add(txtUsername, gb.next().weightx(0.8))
        panel.add(label("Password"), gb.nextLine().next().weightx(0.2))
        panel.add(txtPassword, gb.next().weightx(0.8))

        return panel
    }

    override fun doOKAction() {
        val credentialAttributes = CredentialAttributes("CodeRunnerPlugin")
        val credentials = Credentials(txtUsername.text, txtPassword.password)
        PasswordSafe.instance.set(credentialAttributes, credentials)
        val state = CodeRunnerPluginSettings.getInstance()
        var state_data = state.getState()
        state_data?.base_url = txtBaseUrl.text
        if (state_data != null) {
            state.loadState(state_data)
        }
    }

    private fun label(text: String): JComponent {
        val label = JBLabel(text)
        label.componentStyle = UIUtil.ComponentStyle.SMALL
        label.fontColor = UIUtil.FontColor.BRIGHTER
        label.border = JBUI.Borders.empty(0, 5, 2, 0)
        return label
    }

}