package jalse.entities;

import java.util.Objects;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import jalse.attributes.AttributeListener;
import jalse.attributes.Attributes;
import jalse.attributes.NamedAttributeType;

public class EntitiesTest {

    public static class RecursiveAttributeListenerTest {

	private class TestAttributeListener implements AttributeListener<Integer> {}

	private class TestAttributeListenerSupplier implements Supplier<AttributeListener<Integer>> {

	    @Override
	    public AttributeListener<Integer> get() {
		return new TestAttributeListener();
	    }
	}

	@Test
	public void createRecursiveAttributeListenerTest() {
	    final EntityListener listener = Entities.newRecursiveAttributeListener(
		    Attributes.newNamedIntegerType("Test"), new TestAttributeListenerSupplier());
	    final EntityListener depthLimitedListener = Entities.newRecursiveAttributeListener(
		    Attributes.newNamedIntegerType("Test"), new TestAttributeListenerSupplier(), 3);

	    Assert.assertTrue(Objects.nonNull(listener));
	    Assert.assertTrue(Objects.nonNull(depthLimitedListener));
	}

	@Test
	public void recursionTest() {
	    final NamedAttributeType<Integer> testType = Attributes.newNamedIntegerType("Test");
	    final EntityContainer container = new DefaultEntityContainer.Builder().build();
	    final EntityListener listener = Entities.newRecursiveAttributeListener(testType,
		    new TestAttributeListenerSupplier(), 2);

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

	private class TestEntityListenerSupplier implements Supplier<EntityListener> {

	    @Override
	    public EntityListener get() {
		return new TestEntityListener();
	    }
	}

	@Test
	public void createRecursiveEntityListenerTest() {
	    final EntityListener listener = Entities.newRecursiveEntityListener(new TestEntityListenerSupplier());
	    final EntityListener depthLimitedListener = Entities
		    .newRecursiveEntityListener(new TestEntityListenerSupplier(), 3);

	    Assert.assertTrue(Objects.nonNull(listener));
	    Assert.assertTrue(Objects.nonNull(depthLimitedListener));
	}

	@Test
	public void recursionTest() {
	    final EntityContainer container = new DefaultEntityContainer.Builder().build();
	    final EntityListener listener = Entities.newRecursiveEntityListener(new TestEntityListenerSupplier(), 2);

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
