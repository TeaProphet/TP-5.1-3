package vsu.tp53.onboardapplication.home

class PlayerModel(_name: String, _reputation: Int) {
    private var reputation: Int = _reputation
    private var name: String = _name
    fun getReputation(): Int {
        return reputation
    }

    fun setReputation(reputation: Int) {
        this.reputation = reputation
    }

    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }
}