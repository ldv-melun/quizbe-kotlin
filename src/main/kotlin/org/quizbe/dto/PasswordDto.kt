package org.quizbe.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class PasswordDto {

    @field:NotBlank(message = "{password.name.blank}")
    @field:Size(min = 8, max = 30, message = "{password.name.min.max}")
    var password: String? = null

    @field:NotBlank(message = "{password.name.blank}")
    @field:Size(min = 8, max = 30, message = "{password.name.min.max}")
    var confirmPassword:  String? = null

    constructor() {}
    constructor(password: String?, confirmPassword: String?) : super() {
        this.password = password
        this.confirmPassword = confirmPassword
    }
}