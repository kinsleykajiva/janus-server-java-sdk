package africa.jopen.sdk.models.events;

/**
 * Represents a participant in the system.
 *
 * @param id         The unique identifier of the participant.
 * @param display    The display name of the participant.
 * @param privateId  The private identifier of the participant.
 */
public record ParticipantPojo(long id, String display , long privateId){
}
