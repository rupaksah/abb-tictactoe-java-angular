package com.tictactoe.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoveHistoryItem {
    private int moveNumber;
    private String player;
    private int row;
    private int col;
}
