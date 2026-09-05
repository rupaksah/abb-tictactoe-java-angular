package com.tictactoe.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
 *
 * @JsonProperty stays on the fields (not the Lombok-generated getters) so
 * it's applied regardless of what accessor name Lombok derives.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScoreboardResponse {
    @JsonProperty("xWins")
    private int xWins;

    @JsonProperty("oWins")
    private int oWins;

    private int draws;
}
