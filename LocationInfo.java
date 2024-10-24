class LocationInfo {
    private int[] coordinates;
    private boolean hasBreeze;
    private boolean hasStench;
    private boolean hasPit;
    private boolean hasWumpus;
    private boolean hasGlitter;
    private boolean hasGold;

    public LocationInfo(int[] coordinates, boolean breeze, boolean stench, boolean pit, boolean wumpus, boolean glitter, boolean gold) {
        this.coordinates = coordinates;
        this.hasBreeze = breeze;
        this.hasStench = stench;
        this.hasPit = pit;
        this.hasWumpus = wumpus;
        this.hasGlitter = glitter;
        this.hasGold = gold;
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    public boolean hasBreeze() {
        return hasBreeze;
    }

    public boolean hasStench() {
        return hasStench;
    }

    public boolean hasPit() {
        return hasPit;
    }

    public boolean hasWumpus() {
        return hasWumpus;
    }

    public boolean hasGlitter() {
        return hasGlitter;
    }

    public boolean hasGold() {
        return hasGold;
    }

    @Override
    public String toString() {
        return String.format("Location: (%d, %d)\n |-- Breeze: %b\n |-- Stench: %b\n |-- Pit: %b\n |-- Wumpus: %b\n |-- Glitter: %b\n |-- Gold: %b\n",
            coordinates[0], coordinates[1], hasBreeze, hasStench, hasPit, hasWumpus, hasGlitter, hasGold);
    }
}
