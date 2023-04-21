package vsu.tp53.onboardapplication.home

class SessionModel(_id: Int, _session_name: String, _date: String, _city: String, _players: String) {

    private var id: Int = _id
    private var name: String = _session_name
    private var date: String = _date
    private var city: String = _city
    private var players: String = _players

    fun getId(): Int {
        return id
    }

    fun setId(_id: Int) {
        this.id = _id
    }

    fun getName(): String {
        return name
    }

    fun setName(_name: String) {
        this.name = _name
    }

    fun getDate(): String {
        return date
    }

    fun setDate(_date: String) {
        this.date = _date
    }

    fun getCity(): String {
        return city
    }

    fun setCity(_city: String) {
        this.city = _city
    }

    fun getPlayers(): String {
        return players
    }

    fun setPlayers(_players: String) {
        this.players = _players
    }

}