package org.quizbe.service
import org.quizbe.model.Question
import org.springframework.stereotype.Service


@Service
class ExportService{

    fun questionToTextRaw(question: Question?): String {
        val build = StringBuilder()
        if (question != null) {
            build.append(question.sentence).append(": ").append("\n")
                for (response in question.responses) {
                    if (response != null) {
                        build.append("[ ]").append(response.proposition).append("\n")
                    }
                }

        }
        return build.toString()
    }


    /**
     * Fait une somme des values, la value de la question est divisée par la somme * 100, pour trouver la value en % sur 100 */


    /**
     * TODO
     *
     * @param question to XML Moodle transform
     * @return XML Moodle image of question
     */
    fun questionToXMLMoodle(question: Question): String {
        val build = StringBuilder()
        if (question != null) {
            build.append("<question type=\"multichoice\">")
            build.append("<name>")
            build.append("<text>")
            build.append(question.name)
            build.append("</text>")
            build.append("</name>")
            build.append("<questiontext format='html'>")
            build.append("<text>")
            build.append("<![CDATA[")
            build.append(question.sentence)
            build.append("]]>")
            build.append("</text>")
            build.append("</questiontext>")
            build.append("<generalfeedback format='html'>")
            build.append("<text></text>")
            build.append("</generalfeedback>")
            build.append("<defaultgrade>1.0000000</defaultgrade>")
            build.append("<penalty>0.3333333</penalty>")
            build.append("<hidden>0</hidden>")
            build.append("<idnumber></idnumber>")
            build.append("<single>true</single>")
            build.append("<shuffleanswers>true</shuffleanswers>")
            build.append("<answernumbering>abc</answernumbering>")
            build.append("<showstandardinstruction>0</showstandardinstruction>")
            build.append("<correctfeedback format='html'>")
            build.append("<text>Your answer is correct.</text>")
            build.append("</correctfeedback>")
            build.append("<partiallycorrectfeedback format='html'>")
            build.append("<text>Your answer is partially correct.</text>")
            build.append("</partiallycorrectfeedback>")
            build.append("<incorrectfeedback format='html'>")
            build.append("<text>Your answer is incorrect.</text>")
            build.append("</incorrectfeedback>")
            val sumPositiveValue = question.responses.filter { it.value!! >= 1 }.sumOf { it.value!! }
            val sumNegativeValue = question.responses.filter { it.value!! <= -1 }.sumOf { it.value!! }
            var valueToGrade: Float = 0F
            for (response in question.responses) {
                if (response != null) {
                    if (response.value!! >= 1) {
                        valueToGrade = (response.value!!.toFloat() / sumPositiveValue.toFloat() * 100F)
                    } else
                        valueToGrade = (response.value!!.toFloat() / sumNegativeValue.toFloat() * -100F)
                    build.append("<answer fraction='")
                    build.append(valueToGrade)
                    build.append("' format='html'>")
                    build.append("<text>")
                    build.append("<![CDATA[")
                    build.append(response.proposition)
                    build.append("]]>")
                    build.append("</text>")
                    build.append("<feedback format='html'>")
                    build.append("<text>")
                    build.append("<![CDATA[")
                    build.append(response.feedback)
                    build.append("]]>")
                    build.append("</text>")
                    build.append("</feedback>")
                    build.append("</answer>")
                }
            }
        }
        build.append("</question>")
        return build.toString()
    }
}