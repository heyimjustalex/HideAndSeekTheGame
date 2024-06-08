package Game.GameClasses;

public enum MessageType {
    //ELECTION MESSAGE TYPES
    GREETING, GREETING_OK, ELECTION, ELECTION_OK, COORDINATOR,
    // MUTUAL EXCLUSION MESSAGE TYPES
    REQUEST_RESOURCE, RESOURCE_GRANTED, RESOURCE_NOT_GRANTED, ACK, SEEKER
}