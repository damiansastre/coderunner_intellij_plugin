package com.github.tagercito.coderunnerintellijplugin

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name="CodeRunner",
    storages = [Storage("code-runner.xml")]
)
class CodeRunnerPluginSettings: PersistentStateComponent<CodeRunnerPluginState> {

    private var pluginState = CodeRunnerPluginState()

    override fun getState(): CodeRunnerPluginState? {
        return pluginState
    }

    override fun loadState(state: CodeRunnerPluginState) {
        pluginState = state
    }
    companion object {
        @JvmStatic
        fun getInstance(): PersistentStateComponent<CodeRunnerPluginState>{
            return ServiceManager.getService(CodeRunnerPluginSettings::class.java)
        }
    }
}