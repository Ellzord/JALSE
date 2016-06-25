package jalse.attributes;

import java.util.HashSet;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class AttributeContainerTest {

    private class TestAttributeListener implements AttributeListener<Integer> {

	public boolean changed;

	@Override
	public void attributeAdded(final AttributeEvent<Integer> event) {}

	@Override
	public void attributeChanged(final AttributeEvent<Integer> event) {
	    changed = true;
	}

	@Override
	public void attributeRemoved(final AttributeEvent<Integer> event) {}
    }

    DefaultAttributeContainer container;

    @Test
    public void addFromContainerTest() {
	final NamedAttributeType<Integer> namedAttributeType = new NamedAttributeType<Integer>("test",
		Attributes.INTEGER_TYPE);
	final TestAttributeListener attributeListener = new TestAttributeListener();
	container = new DefaultAttributeContainer();

	container.setAttribute(namedAttributeType, 10);
	container.addAttributeListener(namedAttributeType, attributeListener);

	DefaultAttributeContainer otherContainer = new DefaultAttributeContainer();
	otherContainer.addAllAttributes(container);
	Assert.assertTrue(otherContainer.getAttributes().contains(10));
	otherContainer.addAllAttributeListeners(container);
	Assert.assertTrue(otherContainer.getAttributeListeners(namedAttributeType).contains(attributeListener));

	otherContainer = new DefaultAttributeContainer();
	otherContainer.addAll(container);
	Assert.assertTrue(otherContainer.getAttributes().contains(10));
	Assert.assertTrue(otherContainer.getAttributeListeners(namedAttributeType).contains(attributeListener));
    }

    @After
    public void after() {
	container = null;
    }

    @Test
    public void attributeListenerTest() {
	final NamedAttributeType<Integer> namedAttributeType = new NamedAttributeType<Integer>("test",
		Attributes.INTEGER_TYPE);
	final TestAttributeListener attributeListener = new TestAttributeListener();
	container = new DefaultAttributeContainer();

	container.addAttributeListener(namedAttributeType, attributeListener);
	Assert.assertTrue(container.hasAttributeListeners(namedAttributeType));
	Assert.assertTrue(container.hasAttributeListeners(namedAttributeType.getName(), namedAttributeType.getType()));
	Assert.assertTrue(container.hasAttributeListener(namedAttributeType, attributeListener));
	Assert.assertTrue(container.hasAttributeListener(namedAttributeType.getName(), namedAttributeType.getType(),
		attributeListener));

	Assert.assertEquals(1, container.getAttributeListeners(namedAttributeType).size());
	Assert.assertTrue(container.getAttributeListeners(namedAttributeType).contains(attributeListener));

	container.setAttribute(namedAttributeType, 10);
	Assert.assertFalse(attributeListener.changed);
	container.fireAttributeChanged(namedAttributeType);
	Assert.assertTrue(attributeListener.changed);

	container.removeAttributeListener(namedAttributeType, attributeListener);
	Assert.assertFalse(container.hasAttributeListeners(namedAttributeType));
	Assert.assertFalse(container.hasAttributeListeners(namedAttributeType.getName(), namedAttributeType.getType()));
	Assert.assertFalse(container.hasAttributeListener(namedAttributeType, attributeListener));
	Assert.assertFalse(container.hasAttributeListener(namedAttributeType.getName(), namedAttributeType.getType(),
		attributeListener));

	container.addAttributeListener(namedAttributeType, attributeListener);
	container.removeAttributeListeners(namedAttributeType);
	Assert.assertFalse(container.hasAttributeListeners(namedAttributeType));
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
