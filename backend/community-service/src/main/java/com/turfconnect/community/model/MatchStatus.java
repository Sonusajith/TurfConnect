package com.turfconnect.community.model;

public enum MatchStatus {
    CHALLENGED, // Proposed by home team, waiting for away team
    ACCEPTED,   // Away team accepted
    REJECTED,   // Away team declined
    CANCELLED,  // Match cancelled
    COMPLETED   // Match played
}
