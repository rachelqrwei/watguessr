package com.gooners.watguessr.dto;

import java.util.HashMap;
import java.util.UUID;

public class RankedGameResultDto {
    private HashMap<UUID, Integer> userPoints;
    private HashMap<UUID, Integer> eloChanges;

    public HashMap<UUID, Integer> getUserPoints() { return userPoints; }

    public void setUserPoints(HashMap<UUID, Integer> userPoints) {
        this.userPoints = userPoints;
    }

    public HashMap<UUID, Integer> getEloChanges() { return eloChanges; }

    public void setEloChanges(HashMap<UUID, Integer> eloChanges) {
        this.eloChanges = eloChanges;
    }
}
