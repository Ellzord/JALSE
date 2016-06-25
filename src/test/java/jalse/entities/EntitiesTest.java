package jalse.entities;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

import jalse.attributes.AttributeListener;
import jalse.attributes.Attributes;
import jalse.attributes.NamedAttributeType;

public class EntitiesTest {
    
    private EntitiesTest() {}

    public static class RecursiveAttributeListenerTest {

	private class TestAttributeListener implements AttributeListener<Integer> {}

	@Test
	public void createRecursiveAttributeListenerTest() {
	    final EntityListener listener = Entities
		    .newRecursiveAttributeListener(Attributes.newNamedIntegerType("Test"), TestAttributeListener::new);
	    final EntityListener depthLimitedListener = Entities.newRecursiveAttributeListener(
		    Attributes.newNamedIntegerType("Test"), TestAttributeListener::new, 3);

	    Assert.assertTrue(Objects.nonNull(listener));
	    Assert.assertTrue(Objects.nonNull(depthLimitedListener));
	}

	@Test
	public void recursionTest() {
	    final NamedAttributeType<Integer> testType = Attributes.newNamedIntegerType("Test");
	    final EntityContainer container = new DefaultEntityContainer.Builder().build();
	    final EntityListener listener = Entities.newRecursiveAttributeListener(testType, TestAttributeListener::new,
		    2);

	    container.addEntityListener(listener);
	    final Entity entityDepth1 = container.newEntity();
	    Assert.assertEquals(1, entityDepth1.getEntityListeners().size());
	    Assert.assertEquals(1, entityDepth1.getAttributeListeners(testType).size());

	    final Entity entityDepth2 = entityDepth1.newEntity();
	    Assert.assertEquals(1, entityDepth2.getEntityListeners().size());
	    Assert.assertEquals(1, entityDepth2.getAttributeListeners(testType).size());

	    final Entity entityDepth3 = entityDepth2.newEntity();
	    Assert.assertTrue(entityDepth3.getEntityListeners().isEmpty());
	    Assert.assertTrue(entityDepth3.getAttributeListeners(testType).isEmpty());
	}
    }

    public static class RecursiveEntityListenerTest {

	private class TestEntityListener implements EntityListener {}

	@Test
	public void createRecursiveEntityListenerTest() {
	    final EntityListener listener = Entities.newRecursiveEntityListener(TestEntityListener::new);
	    final EntityListener depthLimitedListener = Entities.newRecursiveEntityListener(TestEntityListener::new, 3);

	    Assert.assertTrue(Objects.nonNull(listener));
	    Assert.assertTrue(Objects.nonNull(depthLimitedListener));
	}

	@Test
	public void recursionTest() {
	    final EntityContainer container = new DefaultEntityContainer.Builder().build();
	    final EntityListener listener = Entities.newRecursiveEntityListener(TestEntityListener::new, 2);

	    container.addEntityListener(listener);
	    final Entity entityDepth1 = container.newEntity();
	    Assert.assertEquals(2, entityDepth1.getEntityListeners().size());

	    final Entity entityDepth2 = entityDepth1.newEntity();
	    Assert.assertEquals(2, entityDepth2.getEntityListeners().size());

	    final Entity entityDepth3 = entityDepth2.newEntity();
	    Assert.assertTrue(entityDepth3.getEntityListeners().isEmpty());
	}
    }
}
