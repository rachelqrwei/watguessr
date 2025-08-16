package com.gooners.watguessr.dto;

import java.util.List;
import java.util.UUID;

public class RoundGuessesDto {
    private UUID roundId;
    private List<GuessDto> guesses;

    public UUID getRoundId() {
        return roundId;
    }

    public void setRoundId(UUID roundId) {
        this.roundId = roundId;
    }

    public List<GuessDto> getGuesses() {
        return guesses;
    }

    public void setGuesses(List<GuessDto> guesses) {
        this.guesses = guesses;
    }
}


