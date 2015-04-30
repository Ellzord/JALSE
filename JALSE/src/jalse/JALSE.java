package jalse;

import jalse.actions.Action;
import jalse.actions.ActionEngine;
import jalse.actions.ActionScheduler;
import jalse.entities.Entities;
import jalse.entities.Entity;
import jalse.entities.EntityContainer;
import jalse.misc.Identifiable;
import jalse.tags.Taggable;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JALSE is the overall parent container and engine for each simulation. It provides the ability to
 * create a number of {@link Entity} and execute {@link Action} at given intervals.
 *
 *
 * @author Elliot Ford
 *
 * @see DefaultJALSE
 *
 */
public interface JALSE extends Identifiable, ActionEngine, ActionScheduler<JALSE>, EntityContainer, Taggable {

    /**
     * Gets a set containing the entire entity tree.
     *
     * @return The entire entity tree.
     *
     * @see #streamEntityTree()
     */
    default Set<Entity> getEntityTree() {
	return streamEntityTree().collect(Collectors.toSet());
    }

    /**
     * Gets the current set of IDs for the entire tree.
     *
     * @return All entity IDs.
     *
     * @see Entities#getEntityIDsRecursively(EntityContainer)
     */
    public Set<UUID> getIDsInTree();

    /**
     * Gets the current total number of entities.
     *
     * @return Total number of entities within the simulation.
     *
     * @see Entities#getEntityCountRecursively(EntityContainer)
     */
    public int getTreeCount();

    /**
     * Streams the entire entity tree.
     *
     * @return Stream of the entire tree.
     *
     * @see Entities#walkEntities(EntityContainer)
     */
    public Stream<Entity> streamEntityTree();
}