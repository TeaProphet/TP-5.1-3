package vsu.tp53.onboardapplication.auth.service

enum class Errors(var text: String) {
    INVALID_CREDENTIALS("Недействительные учётные данные"),
    INVALID_DATA("Недействительные данные"),
    INVALID_TOKEN("Недействительный токен"),
    EXPIRED_TOKEN("Токен просрочен"),
    REQUESTER_IS_ADDRESSER("Адресат является отправителем"),
    ACCESS_DENIED("Доступ запрещён"),
    ALREADY_BANNED("Уже заблокирован"),
    ALREADY_UNBANNED("Уже разблокирован"),
    ALREADY_CHANGED("Репутация уже изменена");

    companion object {
        fun getByName(name: String): String {
            val errors = Errors.values()
            for (error in errors) {
                if (error.name == name) {
                    return error.text
                }
            }

            return ""
        }
    }
}