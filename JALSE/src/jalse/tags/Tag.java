package jalse.tags;

/**
 * Tags are used to provide internal state information without polluting the data model. Tag is just
 * a marker {@code interface} any class implementing this can be used to show state. Tags cannot be
 * added externally but should be added during execution of JALSE (at the relevant points of
 * execution). Tags should be immutable, constants or {@code enum} as much as possible (to help
 * filtering).
 *
 * @author Elliot Ford
 *
 */
public interface Tag {}
