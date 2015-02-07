package jalse.attributes;

import jalse.Cluster;
import jalse.Core;
import jalse.agents.Agent;
import jalse.listeners.AttributeListener;

/**
 * Attributes can be considered the core data of the JALSE model. Attribute is
 * just a marker {@code interface} any class implementing this can be used as a
 * data type to store/retrieve. {@link Attributable} is an attribute container
 * where attributes can be stored and {@link AttributeListener} can be set to
 * trigger on value updates. Existing or {@code final} classes can be made into
 * attributes by extending {@link NonAttributeWrapper}.<br>
 * <br>
 *
 * An example attribute:
 *
 * <pre>
 * <code>
 * public class Moo implements Attribute {
 *
 *	private boolean loud;
 *
 *	public Moo(boolean loud) {
 *
 *		this.loud = loud;
 *	}
 *
 *	public boolean isLoud() {
 *
 *		return loud;
 *	}
 * }
 * </code>
 *
 * <pre>
 *
 * @author Elliot Ford
 *
 * @see Core
 * @see Cluster
 * @see Agent
 *
 */
public interface Attribute {

}
