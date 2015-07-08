package jalse.entities;

import java.util.Objects;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class EntitiesTest {

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
