package com.tictactoe.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * NOTE: xWins/oWins need an explicit @JsonProperty. Without it, Jackson's
 * default bean-property naming (java.beans.Introspector.decapitalize) only
 * lowercases a getter's leading capital when the *second* character is
 * lowercase too — "getXWins" -> "XWins" (X and W are both capitals, so it's
 * left alone) instead of the intended "xWins". That silently serialized
 * this DTO as {"XWins":...,"OWins":...,"draws":...}, which the Angular
 * scoreboard component (expecting lowercase xWins/oWins) would have shown
 * as blank/undefined. Caught by the first real `mvn test` run against
 * actual Spring/Jackson - the hand-written stubs used for compile-checking
 * in the sandbox don't emulate Jackson's runtime serialization, so this
 * couldn't have been caught before that.
 */
public class ScoreboardResponse {
    @JsonProperty("xWins")
    private int xWins;

    @JsonProperty("oWins")
    private int oWins;

    private int draws;

    public ScoreboardResponse() {
    }

    public ScoreboardResponse(int xWins, int oWins, int draws) {
        this.xWins = xWins;
        this.oWins = oWins;
        this.draws = draws;
    }

    public int getXWins() {
        return xWins;
    }

    public void setXWins(int xWins) {
        this.xWins = xWins;
    }

    public int getOWins() {
        return oWins;
    }

    public void setOWins(int oWins) {
        this.oWins = oWins;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }
}
