package jalse.attributes;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class DefaultAttributeContainerTest {

    public static class BuilderTest {

	DefaultAttributeContainer container = null;

	@After
	public void after() {
	    container = null;
	}

	@Test
	public void attributeBuildTest() {
	    container = new DefaultAttributeContainer.Builder()
		    .setAttribute(Attributes.newNamedBooleanType("scary"), true).build();
	}

	@Test
	public void attributeBuildTest2() {
	    container = new DefaultAttributeContainer.Builder().setAttribute("scary", Attributes.BOOLEAN_TYPE, true)
		    .build();
	}

	@Test
	public void buildTest() {
	    container = new DefaultAttributeContainer.Builder().build();
	}

	@Test
	public void delegateBuildTest() {
	    container = new DefaultAttributeContainer.Builder().setDelegateContainer(new DefaultAttributeContainer())
		    .build();
	}

	@Test
	public void listenerBuilderTest() {
	    container = new DefaultAttributeContainer.Builder()
		    .addListener(Attributes.newNamedIntegerType("score"), new TestAttributeListener()).build();
	}

	@Test
	public void listenerBuilderTest2() {
	    container = new DefaultAttributeContainer.Builder()
		    .addListener("score", Attributes.INTEGER_TYPE, new TestAttributeListener()).build();
	}
    }

    private static class TestAttributeListener implements AttributeListener<Integer> {

	public boolean added = false;
	public boolean changed = false;
	public boolean removed = false;

	@Override
	public void attributeAdded(final AttributeEvent<Integer> event) {
	    added = true;
	}

	@Override
	public void attributeChanged(final AttributeEvent<Integer> event) {
	    changed = true;
	}

	@Override
	public void attributeRemoved(final AttributeEvent<Integer> event) {
	    removed = true;
	}
    }

    DefaultAttributeContainer container = null;

    @After
    public void after() {
	container = null;
    }

    @Test
    public void attributeListenerTest() {
	final TestAttributeListener attributeListener = new TestAttributeListener();
	container = new DefaultAttributeContainer();

	Assert.assertTrue(container.getAttributeListenerNames().isEmpty());
	Assert.assertTrue(container.getAttributeListenerTypes("test").isEmpty());
	Assert.assertTrue(container.getAttributeListeners("test", Attributes.INTEGER_TYPE).isEmpty());

	container.addAttributeListener("test", Attributes.INTEGER_TYPE, attributeListener);
	Assert.assertTrue(container.getAttributeListenerNames().contains("test"));
	Assert.assertTrue(container.getAttributeListenerTypes("test").contains(Attributes.INTEGER_TYPE));
	Assert.assertTrue(container.getAttributeListeners("test", Attributes.INTEGER_TYPE).contains(attributeListener));

	Assert.assertFalse(attributeListener.added);
	container.setAttribute("test", Attributes.INTEGER_TYPE, 10);
	Assert.assertTrue(attributeListener.added);

	Assert.assertFalse(attributeListener.changed);
	container.fireAttributeChanged("test", Attributes.INTEGER_TYPE);
	Assert.assertTrue(attributeListener.changed);

	// Nothing happens.
	container.fireAttributeChanged("test", Attributes.DOUBLE_TYPE);
	// Nothing happens.
	container.fireAttributeChanged("test2", Attributes.DOUBLE_TYPE);
	// Nothing happens.
	container.setAttribute("test2", Attributes.DOUBLE_TYPE, 3.14);
	container.fireAttributeChanged("test2", Attributes.DOUBLE_TYPE);

	Assert.assertFalse(attributeListener.removed);
	container.removeAttribute("test", Attributes.INTEGER_TYPE);
	Assert.assertTrue(attributeListener.removed);

	Assert.assertTrue(container.getAttributeListenerTypes("test").contains(Attributes.INTEGER_TYPE));
	Assert.assertTrue(container.removeAttributeListener("test", Attributes.INTEGER_TYPE, attributeListener));
	Assert.assertTrue(container.getAttributeListenerTypes("test").isEmpty());

	// Try to remove the same listener twice.
	Assert.assertFalse(container.removeAttributeListener("test", Attributes.INTEGER_TYPE, attributeListener));

	container.addAttributeListener("test", Attributes.INTEGER_TYPE, attributeListener);
	container.removeAttributeListeners("test", Attributes.INTEGER_TYPE);
	Assert.assertTrue(container.getAttributeListenerTypes("test").isEmpty());

	container.addAttributeListener("test", Attributes.INTEGER_TYPE, attributeListener);
	container.removeAttributeListeners();
	Assert.assertTrue(container.getAttributeListenerTypes("test").isEmpty());
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

	final TestAttributeListener attributeListener = new TestAttributeListener();
	container.addAttributeListener("test", Attributes.INTEGER_TYPE, attributeListener);
	Assert.assertNotEquals(container, otherContainer);

	otherContainer.addAttributeListener("test", Attributes.INTEGER_TYPE, attributeListener);
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

	final TestAttributeListener attributeListener = new TestAttributeListener();
	container.addAttributeListener("test", Attributes.INTEGER_TYPE, attributeListener);
	Assert.assertNotEquals(container.hashCode(), otherContainer.hashCode());

	otherContainer.addAttributeListener("test", Attributes.INTEGER_TYPE, attributeListener);
	Assert.assertEquals(container.hashCode(), otherContainer.hashCode());
    }
}
