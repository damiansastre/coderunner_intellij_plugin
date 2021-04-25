package com.github.tagercito.coderunnerintellijplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GetQuizzes: AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val course_wrapper = CodeRunnerQuizSelectionWrapper()
        if (course_wrapper.showAndGet()){
            course_wrapper.close(1)
        }
    }

}