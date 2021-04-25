package com.github.tagercito.coderunnerintellijplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class LoginAction: AnAction(){
    override fun actionPerformed(e: AnActionEvent){
        val wrapper = CoderRunnerLoginWrapper()
        if (wrapper.showAndGet()){
            wrapper.close(1)
        }
    }
}