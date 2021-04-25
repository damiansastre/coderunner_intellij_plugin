package com.github.tagercito.coderunnerintellijplugin

import com.beust.klaxon.Klaxon
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.ui.components.JBLabel
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionListener
import java.net.URL
import java.util.*
import javax.swing.JComponent
import javax.swing.JPanel


data class LoginToken(val token: String, val privatetoken: String)

data class Courses(val courses: List<Course>?)
data class Course(val shortname: String = "", val fullname: String = "", val id: Int = 0)

data class Quizzes(val quizzes: List<Quiz>?)
data class Quiz(val name: String = "", val id: Int = 0 )

data class Attempts(val attempts: List<Attempt>?)
data class Attempt(val id: Int = 0, val quiz: Int = 0, val state: String = "" )

data class Questions(val questions: List<Question>)
data class QuestionTestCase(val testcode: String, val expected: String)
data class Question(val test_cases: List<QuestionTestCase>,
                    val template: String,
                    val name: String,
                    val question_text: String,
                    val type: String)

class CodeRunnerQuizSelectionWrapper: DialogWrapper( true) {
    private val panel = JPanel(GridBagLayout())
    var token: String = ""
    var courses = ComboBox(arrayOf("Select a Course"), 300)
    var quizzes = ComboBox(arrayOf("Select a Quizz"), 300);
    var password: String? = ""
    var username: String? = ""
    var base_url: String? = ""

    init {
        init();
        val state = CodeRunnerPluginSettings.getInstance().state
        val credentialAttributes = CredentialAttributes("CodeRunnerPlugin")
        val credentials = PasswordSafe.instance.get(credentialAttributes)
        password = credentials?.getPasswordAsString()
        print(password)
        username = credentials?.userName.toString()
        base_url = state?.base_url;
        val choices = get_choices()
        for (choice in choices){
            courses.addItem(choice);
        }
        courses.addActionListener(ActionListener {
            var quizz_list = get_quizzes(courses.selectedItem.toString())
            quizzes.removeAllItems()
            for (quiz in quizz_list) {
                quizzes.addItem(quiz)
            }
        })
    }
    override fun createCenterPanel(): JComponent? {

        val gb = GridBag()
            .setDefaultInsets(Insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))
            .setDefaultWeightX(1.0)
            .setDefaultFill(GridBagConstraints.HORIZONTAL)

        panel.preferredSize = Dimension(400, 100)
        panel.add(label("Select Course"), gb.nextLine().next().weightx(0.2))
        panel.add(courses, gb.next().weightx(0.8));
        panel.add(label("Select Quizz"), gb.nextLine().next().weightx(0.2))
        panel.add(quizzes, gb.next().weightx(0.8));

        return panel
    }

    override fun doOKAction() {
        val p: Project = ProjectManager.getInstance().openProjects[0]
        val fileFactory = PsiFileFactory.getInstance(p)
        val projectBasePath = p.guessProjectDir()
        val quiz_name = quizzes.selectedItem.toString()
        var sub: PsiDirectory? = null;
        if (projectBasePath != null) {
            ApplicationManager.getApplication().runWriteAction {
                    var dir = PsiDirectoryFactory.getInstance(p).createDirectory(projectBasePath)
                    var sub = dir.createSubdirectory(quiz_name)
                    var questions = get_questions()
                    if (questions != null) {
                        for (question in questions){
                            var file = fileFactory.createFileFromText(question.name,
                                PlainTextFileType.INSTANCE,
                                String(Base64.getDecoder().decode(question.question_text)))
                                sub.add(file)
                        }
                    }
            }
        }
    }

    private fun label(text: String): JComponent {
        val label = JBLabel(text)
        label.componentStyle = UIUtil.ComponentStyle.SMALL
        label.fontColor = UIUtil.FontColor.BRIGHTER
        label.border = JBUI.Borders.empty(0, 5, 2, 0)
        return label
    }

    private fun login(): String {
        var url = "$base_url/login/token.php?username=$username&password=$password&service=moodle_mobile_app";
        val jsonStr = URL(url).readText()
        val parser = Klaxon().parse<Map<String,Any>>(jsonStr);
        val token = parser?.get("token").toString()
        return token
    }

    private fun get_quizzes(course: String): Array<String>{
        var quizzes: Array<String> = arrayOf()
        var course = course.split(" ").toTypedArray()[0]
        var quizz_url = "$base_url/webservice/rest/server.php?moodlewsrestformat=json&wstoken=$token&wsfunction=mod_quiz_get_quizzes_by_courses&courseids[0]=$course"
        var quizz_request = URL(quizz_url).readText()
        val parser = Klaxon().parse<Quizzes>(quizz_request);
        var quizz_list = parser?.quizzes;

        if (quizz_list != null) {
            for (quizz in  quizz_list){
                quizzes += quizz.id.toString() + " " + quizz.name
                print(quizz.id.toString() + " " + quizz.name + "\n")
            }
        }
        return quizzes
    }

    private fun get_courses(): Array<String>{
        var courses : Array<String> = arrayOf("")
        var courses_url = "$base_url/webservice/rest/server.php?moodlewsrestformat=json&wstoken=$token&wsfunction=core_course_get_enrolled_courses_by_timeline_classification&classification=inprogress"
        var course_request = URL(courses_url).readText()
        val parser = Klaxon().parse<Courses>(course_request);
        var course_list = parser?.courses;
        if (course_list != null) {
            for (course in  course_list){
                courses += course.id.toString() + " " + course.shortname
            }
        }
        return courses
    }

    private fun get_code_runner_questions(quiz: String, attempt: String): List<Question>? {
        var questions_url = "$base_url/webservice/rest/server.php?moodlewsrestformat=json&wstoken=$token&wsfunction=coderunner_api_get_coderunner_quiz&quizid=$quiz&attemptid=$attempt"
        var question_request = URL(questions_url).readText()
        val parser = Klaxon().parse<Questions>(question_request);
        var question_list = parser?.questions;
        return question_list;
    }

    private fun get_questions(): List<Question>? {
        var quiz = quizzes.selectedItem.toString().split(" ").toTypedArray()[0]
        var attempts : Array<String> = arrayOf()
        var attempts_url = "$base_url/webservice/rest/server.php?moodlewsrestformat=json&wstoken=$token&wsfunction=mod_quiz_get_user_attempts&quizid=$quiz&status=all&includepreviews=1"
        var attempts_request = URL(attempts_url).readText()
        val parser = Klaxon().parse<Attempts>(attempts_request);
        var attempt_list = parser?.attempts;
        val attempt = attempt_list?.get(0)
        return get_code_runner_questions(quiz, attempt?.id.toString())
    }
     private fun get_choices(): Array<String> {
        token = login()
        var courses = get_courses()
        return courses
    }

}