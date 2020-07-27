package org.sfvl.demo;

abstract class HeroExperience {
    private int level;
    private int levelBoost;
    private int xp;

    public HeroExperience() {
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevelBoost() {
        return levelBoost;
    }

    public void setLevelBoost(int levelBoost) {
        this.levelBoost = levelBoost;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void increaseXp(int value) {
        setXp(getXp() + value);
    }

    public void gainxp(int xpToAdd, Runnable callback) {

        if (getLevel() >= 15) {
            return;
        }

        int levelToUse = getLevel()-getLevelBoost();

        increaseXp(xpToAdd);
        if (getXp() > (int) (Math.pow(1.55, (double) levelToUse)) + 35 * levelToUse + 50) {
            levelgain();
            callback.run();
        }
    }

    public final void levelgain() {
        setLevel(getLevel() + 1);
        setXp(0);
        levelgainRule();
    }

    protected abstract void levelgainRule();
}
