package org.quizbe.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class UserDto {

    var id: Long? = null

    @field:NotBlank(message = "{user.username.blank}")
    @field:Size(min = 3, max = 30, message = "{user.username.min.max}")
    var username: String = ""

    @field:NotBlank(message = "{user.email.blank}")
    @field:Email(message = "{user.email.invalid}")
    var email:  String = ""

    @field:NotBlank(message = "{user.password.blank}")
    @field:Size(min = 8, message = "{user.password.min}")
    var password:  String = ""

    var roles: MutableSet<String> = mutableSetOf()

    constructor()

    /**
     * UserDto
     * @param userName
     * @param email
     * @param password
     */
    constructor(userName: String, email: String, password: String) : super() {
        this.username = userName
        this.email = email
        this.password = password
    }

    constructor(userName: String, email: String, password: String, roles: MutableSet<String>) : this(userName, email, password) {
        this.roles = roles
    }


    override fun toString(): String {
        return "UserDto {" +
                "id='" + id + '\'' +
                ", userName='" + username + '\'' +
                ", email='" + email + '\'' +
                ", Roles=" + roles.toString() +
                '}'
    }
}