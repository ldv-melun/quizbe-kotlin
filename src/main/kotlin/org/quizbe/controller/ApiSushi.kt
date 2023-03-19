package org.quizbe.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File

class BoxDtoJson(
    val id: Long?,
    val pieces: Int,
    val nom: String,
    val image: String,
    val prix: Double,
    val saveurs: Set<String>,
    val aliments: List<AlimentBoxDtoJson>
) {
    override fun toString(): String {
        return "BoxDtoJson(id=$id, pieces=$pieces, nom='$nom', image='$image', prix=$prix, saveurs=$saveurs, aliments=$aliments)"
    }
}
class AlimentBoxDtoJson (
    var nom: String,
    var quantite: Float) {

    override fun toString(): String {
        return "AlimentBoxDtoJson(nom='$nom', quantite=$quantite)"
    }
}

@RestController
@RequestMapping
class ApiSushi {

    val boxesDtoJsonList : List<BoxDtoJson>

    constructor(){
        val fileNameJson = "boxes-sushi.json"
        val mapper = jacksonObjectMapper()
        val boxesJsonStr: String = File(fileNameJson).readText(Charsets.UTF_8)
        this.boxesDtoJsonList = mapper.readValue(boxesJsonStr)
    }

    @CrossOrigin
    @GetMapping("/api/boxes")
    fun apiSushi() : ResponseEntity<List<BoxDtoJson>> {
        return ResponseEntity.ok(boxesDtoJsonList)
    }
}
