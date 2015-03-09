package jalse.attributes;

import jalse.listeners.AttributeListener;

/**
 * Attributes can be considered the core data of the JALSE model. Attribute is just a marker
 * {@code interface} any class implementing this can be used as a data type to store/retrieve.
 * {@link AttributeContainer} is an attribute container where attributes can be stored and
 * {@link AttributeListener} can be set to trigger on value updates. Existing or {@code final}
 * classes can be made into attributes by extending {@link NonAttributeWrapper}.<br>
 * <br>
 *
 * An example attribute:
 *
 * <pre>
 * <code>
 * public class Load implements Attribute {
 * 
 * 	private float percentage;
 * 
 * 	public Load(float percentage) {
 * 
 * 		this.percentage = percentage;
 * 	}
 * 
 * 	public float getPercentage() {
 * 
 * 		return percentage;
 * 	}
 * }
 * </code>
 * </pre>
 *
 * @author Elliot Ford
 *
 * @see AttributeContainer
 * @see AttributeListener
 * @see AttributeSet
 *
 */
public interface Attribute {}
