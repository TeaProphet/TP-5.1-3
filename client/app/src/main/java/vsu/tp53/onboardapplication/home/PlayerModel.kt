package vsu.tp53.onboardapplication.home

class PlayerModel(_name: String, _reputation: Double) {
    private var reputation: Double = _reputation
    private var name: String = _name
    fun getReputation(): Double {
        return reputation
    }

    fun setReputation(reputation: Double) {
        this.reputation = reputation
    }

    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }
}