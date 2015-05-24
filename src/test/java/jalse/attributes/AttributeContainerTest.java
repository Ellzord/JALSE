package jalse.attributes;

import jalse.listeners.AttributeContainerEvent;
import jalse.listeners.AttributeContainerListener;

import java.util.HashSet;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class AttributeContainerTest {

    private class TestAttributeContainerListener implements AttributeContainerListener<Integer> {

	public boolean changed = false;

	@Override
	public void attributeAdded(final AttributeContainerEvent<Integer> event) {}

	@Override
	public void attributeChanged(final AttributeContainerEvent<Integer> event) {
	    changed = true;
	}

	@Override
	public void attributeRemoved(final AttributeContainerEvent<Integer> event) {}
    }

    DefaultAttributeContainer container = null;

    @Test
    public void addFromContainerTest() {
	final NamedAttributeType<Integer> namedAttributeType = new NamedAttributeType<Integer>("test",
		Attributes.INTEGER_TYPE);
	final TestAttributeContainerListener attributeContainerListener = new TestAttributeContainerListener();
	container = new DefaultAttributeContainer();

	container.setAttribute(namedAttributeType, 10);
	container.addAttributeContainerListener(namedAttributeType, attributeContainerListener);

	DefaultAttributeContainer otherContainer = new DefaultAttributeContainer();
	otherContainer.addAllAttributes(container);
	Assert.assertTrue(otherContainer.getAttributes().contains(10));
	otherContainer.addAllAttributeContainerListeners(container);
	Assert.assertTrue(otherContainer.getAttributeContainerListeners(namedAttributeType).contains(
		attributeContainerListener));

	otherContainer = new DefaultAttributeContainer();
	otherContainer.addAll(container);
	Assert.assertTrue(otherContainer.getAttributes().contains(10));
	Assert.assertTrue(otherContainer.getAttributeContainerListeners(namedAttributeType).contains(
		attributeContainerListener));
    }

    @After
    public void after() {
	container = null;
    }

    @Test
    public void attributeContainerListenerTest() {
	final NamedAttributeType<Integer> namedAttributeType = new NamedAttributeType<Integer>("test",
		Attributes.INTEGER_TYPE);
	final TestAttributeContainerListener attributeContainerListener = new TestAttributeContainerListener();
	container = new DefaultAttributeContainer();

	container.addAttributeContainerListener(namedAttributeType, attributeContainerListener);
	Assert.assertTrue(container.hasAttributeContainerListeners(namedAttributeType));
	Assert.assertTrue(container.hasAttributeContainerListeners(namedAttributeType.getName(),
		namedAttributeType.getType()));
	Assert.assertTrue(container.hasAttributeContainerListener(namedAttributeType, attributeContainerListener));
	Assert.assertTrue(container.hasAttributeContainerListener(namedAttributeType.getName(),
		namedAttributeType.getType(), attributeContainerListener));

	Assert.assertEquals(1, container.getAttributeContainerListeners(namedAttributeType).size());
	Assert.assertTrue(container.getAttributeContainerListeners(namedAttributeType).contains(
		attributeContainerListener));

	container.setAttribute(namedAttributeType, 10);
	Assert.assertFalse(attributeContainerListener.changed);
	container.fireAttributeChanged(namedAttributeType);
	Assert.assertTrue(attributeContainerListener.changed);

	container.removeAttributeContainerListener(namedAttributeType, attributeContainerListener);
	Assert.assertFalse(container.hasAttributeContainerListeners(namedAttributeType));
	Assert.assertFalse(container.hasAttributeContainerListeners(namedAttributeType.getName(),
		namedAttributeType.getType()));
	Assert.assertFalse(container.hasAttributeContainerListener(namedAttributeType, attributeContainerListener));
	Assert.assertFalse(container.hasAttributeContainerListener(namedAttributeType.getName(),
		namedAttributeType.getType(), attributeContainerListener));

	container.addAttributeContainerListener(namedAttributeType, attributeContainerListener);
	container.removeAttributeContainerListeners(namedAttributeType);
	Assert.assertFalse(container.hasAttributeContainerListeners(namedAttributeType));
    }

    @Test
    public void attributeTest() {
	final NamedAttributeType<Integer> namedAttributeType = new NamedAttributeType<Integer>("test",
		Attributes.INTEGER_TYPE);
	container = new DefaultAttributeContainer();

	container.setAttribute(namedAttributeType, 10);
	Assert.assertTrue(container.hasAttributes());
	Assert.assertTrue(container.hasAttribute(namedAttributeType));
	Assert.assertTrue(container.hasAttribute(namedAttributeType.getName(), namedAttributeType.getType()));
	Assert.assertEquals(10, (int) container.getAttribute(namedAttributeType));

	final HashSet<?> attributes = (HashSet<?>) container.getAttributes();
	Assert.assertEquals(1, attributes.size());
	Assert.assertTrue(attributes.contains(10));

	container.removeAttribute(namedAttributeType);
	Assert.assertFalse(container.hasAttributes());
	Assert.assertFalse(container.hasAttribute(namedAttributeType));
	Assert.assertFalse(container.hasAttribute(namedAttributeType.getName(), namedAttributeType.getType()));
    }

    @Test
    public void optAttributeTest() {
	final NamedAttributeType<Integer> namedAttributeType = new NamedAttributeType<Integer>("test",
		Attributes.INTEGER_TYPE);
	container = new DefaultAttributeContainer();

	Optional<Integer> set = container.setOptAttribute(namedAttributeType, 10);
	Assert.assertFalse(set.isPresent());
	set = container.setOptAttribute(namedAttributeType, 20);
	Assert.assertEquals(10, (int) set.get());

	Optional<Integer> get = container.getOptAttribute(namedAttributeType);
	Assert.assertEquals(20, (int) get.get());

	Optional<Integer> removed = container.removeOptAttribute(namedAttributeType);
	Assert.assertEquals(20, (int) removed.get());
	removed = container.removeOptAttribute(namedAttributeType);
	Assert.assertFalse(removed.isPresent());

	get = container.getOptAttribute(namedAttributeType);
	Assert.assertFalse(get.isPresent());
    }
}
