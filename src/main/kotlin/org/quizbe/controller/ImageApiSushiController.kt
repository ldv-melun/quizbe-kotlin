package org.quizbe.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller()
@CrossOrigin(origins = ["*"])
@RequestMapping(value = ["/api"])
public class ImageController {

    var logger: Logger = LoggerFactory.getLogger(ImageController::class.java)

    @GetMapping(value = ["/image/{name}"])
    @ResponseBody
    fun image(@PathVariable("name") name: String): ResponseEntity<Any> {
//        logger.info("in image controller : ${name}")
        var data = javaClass
            .getResourceAsStream("/static/images/sushis/" + name + ".jpg")

        // pattern nul-object
        if (data == null) {
            data = javaClass
                .getResourceAsStream("/static/images/sushis/no-image.jpg")
        }

        val res: ByteArray = data.readAllBytes()

        return try {
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.IMAGE_JPEG_VALUE))
                .body(res)
        } catch (error: NoSuchElementException) {
            ResponseEntity
                .notFound()
                .build()
        }
    }
}

