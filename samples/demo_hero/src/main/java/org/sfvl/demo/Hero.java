package org.sfvl.demo;

/**
 * Hero is one of character controlled by the player.
 */

public class Hero
{

    public CappedValue getStaminaCapped() {
        return staminaCapped;
    }

    public CappedValue getManaCapped() {
        return manaCapped;
    }

    public CappedValue getHealthCapped() {
        return healthCapped;
    }

    private final String lastname;

    private final HeroAttribut strengthAttribut = new HeroAttribut();
    private final HeroAttribut vitalityAttribut = new HeroAttribut();
    private final HeroAttribut intelligenceAttribut = new HeroAttribut();
    private final HeroAttribut wisdomAttribut = new HeroAttribut();

    private final CappedValue healthCapped = new CappedValue();
    private final CappedValue staminaCapped = new CappedValue();
    private final CappedValue manaCapped = new CappedValue();

    private double maxLoad;

    private final HeroExperience ninjaExperience = new HeroExperience() {
        @Override
        protected void levelgainRule() {
            int value = -randomBetween(1, 4);
            getStrengthAttribut().increaseBoost(value);
            int value1 = -randomBetween(1, 4);
            getVitalityAttribut().setValueBoost(getVitalityAttribut().getValueBoost() + value1);
            Hero.this.getHealthCapped().increaseMax(randomModuloDividedMin(5, 5));
            int statinc = randomModuloDividedMin(5, 5);
            Hero.this.getStaminaCapped().increaseMax(statinc);
            Hero.this.getStaminaCapped().increaseValue(statinc);
            updateMaxLoad();
        }
    };

    private final HeroExperience wizardExperience = new HeroExperience() {
        @Override
        protected void levelgainRule() {
            int value = -randomBetween(2, 5);
            getIntelligenceAttribut().setValueBoost(getIntelligenceAttribut().getValueBoost() + value);
            Hero.this.getHealthCapped().increaseMax(randomModuloDividedMin(5, 7));
            int statinc = randomModuloDividedMin(5, 7);
            Hero.this.getStaminaCapped().increaseMax(statinc);
            Hero.this.getStaminaCapped().increaseValue(statinc);
            Hero.this.getManaCapped().increaseMax(Math.max(1, GlobalData.randGen.nextInt(5) + getIntelligenceAttribut().getValue() / Math.max(4, 9 - getLevel())));
        }
    };

    private final HeroExperience priestExperience = new HeroExperience() {
        @Override
        protected void levelgainRule() {
            int value = -randomBetween(2, 5);
            getWisdomAttribut().setValueBoost(getWisdomAttribut().getValueBoost() + value);
            Hero.this.getHealthCapped().increaseMax(randomModuloDividedMin(5, 7));
            int statinc = randomModuloDividedMin(5, 7);
            Hero.this.getStaminaCapped().increaseMax(statinc);
            Hero.this.getStaminaCapped().increaseValue(statinc);
            Hero.this.getManaCapped().increaseMax(Math.max(1, GlobalData.randGen.nextInt(5) + getWisdomAttribut().getValue() / Math.max(4, 9 - getLevel())));
        }
    };

    private final HeroExperience fighterExperience = new HeroExperience() {
        @Override
        protected void levelgainRule() {
            int value = -randomBetween(2, 5);
            getStrengthAttribut().increaseBoost(value);
            int value1 = -randomBetween(1, 4);
            getVitalityAttribut().setValueBoost(getVitalityAttribut().getValueBoost() + value1);
            Hero.this.getHealthCapped().increaseMax(randomModuloDividedMin(5, 4));
            int statinc = randomModuloDividedMin(5, 4);
            Hero.this.getStaminaCapped().increaseMax(statinc);
            Hero.this.getStaminaCapped().increaseValue(statinc);
            updateMaxLoad();
        }
    };

    public Hero(String lastname) {
        this.lastname = lastname;
    }

    public HeroAttribut getStrengthAttribut() {
        return strengthAttribut;
    }

    public HeroAttribut getVitalityAttribut() {
        return vitalityAttribut;
    }

    public HeroAttribut getIntelligenceAttribut() {
        return intelligenceAttribut;
    }

    public HeroAttribut getWisdomAttribut() {
        return wisdomAttribut;
    }

    public double getMaxLoad() {
        return maxLoad;
    }

    public String getHeroName() {
        return lastname;
    }

    public HeroExperience getNinjaExperience() {
        return ninjaExperience;
    }

    public HeroExperience getWizardExperience() {
        return wizardExperience;
    }

    public HeroExperience getPriestExperience() {
        return priestExperience;
    }

    public HeroExperience getFighterExperience() {
        return fighterExperience;
    }

    public void updateMaxLoad() {
        updateMaxLoad(getStaminaCapped().getValue(), getStaminaCapped().getMax(), getStrengthAttribut().getValue());
    }

    private void updateMaxLoad(int stamina, int maxstamina, int strength) {
        if (stamina < maxstamina / 5) {
            this.maxLoad = getMaxLoad() * 2 / 3;
        } else if (stamina < maxstamina / 3) {
            this.maxLoad = getMaxLoad() * 4 / 5;
        } else {
            this.maxLoad = (double) (strength * 4 / 5);
        }
    }

    public HeroExperience getExperience(HeroClass classgain) {
        switch (classgain) {
            case NINJA: return getNinjaExperience();
            case WIZARD: return getWizardExperience();
            case PRIEST:return getPriestExperience();
            case FIGHTER:return getFighterExperience();
            default: throw new IllegalArgumentException("Classgain '" + classgain+ "' is invalid");
        }
    }

    public void gainxp(HeroClass classgain, int xpToAdd, Message message) {
        HeroExperience xp = getExperience(classgain);
        xp.gainxp(xpToAdd, () -> message.setMessage(getHeroName() + " gains a " + classgain + " level!"));
    }

    private int randomModuloDividedMin(int modulo, int dividedBy) {
        return Math.max(1, GlobalData.randGen.nextInt() % modulo + getVitalityAttribut().getValue() / dividedBy);
    }

    private int randomBetween(int min, int max) {
        return GlobalData.randGen.nextInt(( max - min)) + min;
    }

}

class Message {
    public static Message instance = new Message();

    public void setMessage(String message) {
        System.out.println(message);
    }
}

class CappedValue {
    private int max;
    private int value;

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void increaseValue(int value) {
        this.value += value;
    }

    public void increaseMax(int value) {
        this.max += value;
    }
}

class HeroAttribut {
    private int value;
    private int valueBoost;

    public int getValue() {
        return value;
    }

    public int getValueBoost() {
        return valueBoost;
    }

    public void setValueBoost(int valueBoost) {
        this.valueBoost = valueBoost;
    }

    public void increaseBoost(int value) {
        this.valueBoost += value;
    }

}

enum HeroClass {
    NINJA,
    WIZARD,
    PRIEST,
    FIGHTER;
}

