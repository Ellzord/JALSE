package jalse.attributes;

import jalse.listeners.AttributeContainerEvent;
import jalse.listeners.AttributeContainerListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class DefaultAttributeContainerTest {

    private class TestAttributeContainerListener implements AttributeContainerListener<Integer> {

	public boolean added = false;
	public boolean changed = false;
	public boolean removed = false;

	@Override
	public void attributeAdded(final AttributeContainerEvent<Integer> event) {
	    added = true;
	}

	@Override
	public void attributeChanged(final AttributeContainerEvent<Integer> event) {
	    changed = true;
	}

	@Override
	public void attributeRemoved(final AttributeContainerEvent<Integer> event) {
	    removed = true;
	}
    }

    DefaultAttributeContainer container = null;

    @After
    public void after() {
	container = null;
    }

    @Test
    public void attributeContainerListenerTest() {
	final TestAttributeContainerListener attributeContainerListener = new TestAttributeContainerListener();
	container = new DefaultAttributeContainer();

	Assert.assertTrue(container.getAttributeContainerListenerNames().isEmpty());
	Assert.assertTrue(container.getAttributeContainerListenerTypes("test").isEmpty());
	Assert.assertTrue(container.getAttributeContainerListeners("test", Attributes.INTEGER_TYPE).isEmpty());

	container.addAttributeContainerListener("test", Attributes.INTEGER_TYPE, attributeContainerListener);
	Assert.assertTrue(container.getAttributeContainerListenerNames().contains("test"));
	Assert.assertTrue(container.getAttributeContainerListenerTypes("test").contains(Attributes.INTEGER_TYPE));
	Assert.assertTrue(container.getAttributeContainerListeners("test", Attributes.INTEGER_TYPE).contains(
		attributeContainerListener));

	Assert.assertFalse(attributeContainerListener.added);
	container.setAttribute("test", Attributes.INTEGER_TYPE, 10);
	Assert.assertTrue(attributeContainerListener.added);

	Assert.assertFalse(attributeContainerListener.changed);
	container.fireAttributeChanged("test", Attributes.INTEGER_TYPE);
	Assert.assertTrue(attributeContainerListener.changed);

	// Nothing happens.
	container.fireAttributeChanged("test", Attributes.DOUBLE_TYPE);
	// Nothing happens.
	container.fireAttributeChanged("test2", Attributes.DOUBLE_TYPE);
	// Nothing happens.
	container.setAttribute("test2", Attributes.DOUBLE_TYPE, 3.14);
	container.fireAttributeChanged("test2", Attributes.DOUBLE_TYPE);

	Assert.assertFalse(attributeContainerListener.removed);
	container.removeAttribute("test", Attributes.INTEGER_TYPE);
	Assert.assertTrue(attributeContainerListener.removed);

	Assert.assertTrue(container.getAttributeContainerListenerTypes("test").contains(Attributes.INTEGER_TYPE));
	Assert.assertTrue(container.removeAttributeContainerListener("test", Attributes.INTEGER_TYPE,
		attributeContainerListener));
	Assert.assertTrue(container.getAttributeContainerListenerTypes("test").isEmpty());

	// Try to remove the same listener twice.
	Assert.assertFalse(container.removeAttributeContainerListener("test", Attributes.INTEGER_TYPE,
		attributeContainerListener));

	container.addAttributeContainerListener("test", Attributes.INTEGER_TYPE, attributeContainerListener);
	container.removeAttributeContainerListeners("test", Attributes.INTEGER_TYPE);
	Assert.assertTrue(container.getAttributeContainerListenerTypes("test").isEmpty());

	container.addAttributeContainerListener("test", Attributes.INTEGER_TYPE, attributeContainerListener);
	container.removeAttributeContainerListeners();
	Assert.assertTrue(container.getAttributeContainerListenerTypes("test").isEmpty());
    }

    @Test
    public void attributeTest() {
	container = new DefaultAttributeContainer();

	Assert.assertTrue(container.getAttributeTypes("test").isEmpty());
	container.setAttribute("test", Attributes.INTEGER_TYPE, 10);
	Assert.assertTrue(container.streamAttributes().findAny().isPresent());
	Assert.assertTrue(container.getAttributeTypes("test").contains(Attributes.INTEGER_TYPE));
	Assert.assertTrue(container.getAttributeNames().contains("test"));
	Assert.assertEquals(10, (int) container.getAttribute("test", Attributes.INTEGER_TYPE));

	container.removeAttribute("test", Attributes.INTEGER_TYPE);
	Assert.assertTrue(container.getAttributeNames().isEmpty());

	Assert.assertNull(container.removeAttribute("test", Attributes.INTEGER_TYPE));

	container.setAttribute("test", Attributes.INTEGER_TYPE, 10);
	container.removeAttributes();
	Assert.assertTrue(container.getAttributeNames().isEmpty());

	container.setAttribute("test", Attributes.INTEGER_TYPE, 10);
	Assert.assertEquals(1, container.getAttributeCount());
	container.setAttribute("test2", Attributes.DOUBLE_TYPE, 3.14);
	Assert.assertEquals(2, container.getAttributeCount());
    }

    @Test
    public void delegateTest() {
	container = new DefaultAttributeContainer();
	container.setAttribute("test", Attributes.INTEGER_TYPE, 10);
	final DefaultAttributeContainer otherContainer = new DefaultAttributeContainer(container);

	Assert.assertEquals(container, otherContainer.getDelegateContainer());
    }

    @Test
    public void equalsTest() {
	container = new DefaultAttributeContainer();
	Assert.assertEquals(container, container);
	container.setAttribute("test", Attributes.INTEGER_TYPE, 10);

	final DefaultAttributeContainer otherContainer = new DefaultAttributeContainer();
	Assert.assertNotEquals(container, otherContainer);
	otherContainer.setAttribute("test", Attributes.INTEGER_TYPE, 10);

	final TestAttributeContainerListener attributeContainerListener = new TestAttributeContainerListener();
	container.addAttributeContainerListener("test", Attributes.INTEGER_TYPE, attributeContainerListener);
	Assert.assertNotEquals(container, otherContainer);

	otherContainer.addAttributeContainerListener("test", Attributes.INTEGER_TYPE, attributeContainerListener);
	Assert.assertEquals(container, otherContainer);
    }

    @Test
    public void hashCodeTest() {
	container = new DefaultAttributeContainer();
	Assert.assertEquals(container.hashCode(), container.hashCode());
	container.setAttribute("test", Attributes.INTEGER_TYPE, 10);

	final DefaultAttributeContainer otherContainer = new DefaultAttributeContainer();
	Assert.assertNotEquals(container.hashCode(), otherContainer.hashCode());
	otherContainer.setAttribute("test", Attributes.INTEGER_TYPE, 10);

	final TestAttributeContainerListener attributeContainerListener = new TestAttributeContainerListener();
	container.addAttributeContainerListener("test", Attributes.INTEGER_TYPE, attributeContainerListener);
	Assert.assertNotEquals(container.hashCode(), otherContainer.hashCode());

	otherContainer.addAttributeContainerListener("test", Attributes.INTEGER_TYPE, attributeContainerListener);
	Assert.assertEquals(container.hashCode(), otherContainer.hashCode());
    }
}
