package jalse.entities;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import jalse.attributes.DefaultAttributeContainer;

public class DefaultEntityContainerTest {

    public static class BuilderTest {

	DefaultEntityContainer container = null;

	@After
	public void after() {
	    container = null;
	}

	@Test(expected = IllegalArgumentException.class)
	public void buildDuplicateId() {
	    final DefaultEntityContainer.Builder builder = new DefaultEntityContainer.Builder();
	    final UUID id = new UUID(0, 0);
	    builder.newEntity(id);
	    builder.newEntity(id, TestEntity.class);
	    builder.build();
	}

	@Test
	public void buildTest1() {
	    final DefaultEntityContainer.Builder builder = new DefaultEntityContainer.Builder();
	    container = builder.build();
	    Assert.assertTrue(container.getEntityListeners().isEmpty());
	    Assert.assertTrue(container.getEntities().isEmpty());
	}

	@Test
	public void buildTest2() {
	    final DefaultEntityContainer.Builder builder = new DefaultEntityContainer.Builder();
	    builder.setFactory(new DefaultEntityFactory());
	    container = builder.build();
	}

	@Test
	public void buildTest3() {
	    final DefaultEntityContainer.Builder builder = new DefaultEntityContainer.Builder();
	    builder.setDelegateContainer(new DefaultEntityContainer());
	    container = builder.build();
	}

	@Test
	public void buildTest4() {
	    final DefaultEntityContainer.Builder builder = new DefaultEntityContainer.Builder();
	    builder.setFactory(new DefaultEntityFactory());
	    builder.setDelegateContainer(new DefaultEntityContainer());
	    container = builder.build();
	}

	@Test
	public void buildTest5() {
	    final DefaultEntityContainer.Builder builder = new DefaultEntityContainer.Builder();
	    builder.newEntity(new UUID(0, 0));
	    container = builder.build();
	    Assert.assertFalse(container.getEntities().isEmpty());
	    Assert.assertTrue(container.getEntitiesOfType(TestEntity.class).isEmpty());
	}

	@Test
	public void buildTest6() {
	    final DefaultEntityContainer.Builder builder = new DefaultEntityContainer.Builder();
	    builder.newEntity(new UUID(0, 0), TestEntity.class);
	    container = builder.build();
	    Assert.assertFalse(container.getEntitiesOfType(TestEntity.class).isEmpty());
	}

	@Test
	public void buildTest7() {
	    final DefaultEntityContainer.Builder builder = new DefaultEntityContainer.Builder();
	    builder.newEntity(new UUID(0, 0), TestEntity.class, new DefaultAttributeContainer());
	    container = builder.build();
	    Assert.assertFalse(container.getEntitiesOfType(TestEntity.class).isEmpty());
	}

	@Test
	public void buildTest8() {
	    final DefaultEntityContainer.Builder builder = new DefaultEntityContainer.Builder();
	    builder.addListener(new EntityListener() {});
	    container = builder.build();
	    Assert.assertFalse(container.getEntityListeners().isEmpty());
	}
    }

    private interface TestEntity extends Entity {}

    private class TestEntityListener implements EntityListener {

	public boolean killed = false;

	@Override
	public void entityCreated(final EntityEvent event) {}

	@Override
	public void entityKilled(final EntityEvent event) {
	    killed = true;
	}

	@Override
	public void entityReceived(final EntityEvent event) {}

	@Override
	public void entityTransferred(final EntityEvent event) {}
    }

    DefaultEntityContainer container = null;

    @Test
    public void addEntityTest() {
	container = new DefaultEntityContainer();

	container.newEntity();
	container.newEntity(new UUID(0, 0));
	container.newEntity(new UUID(0, 1), TestEntity.class, new DefaultAttributeContainer());
	Assert.assertEquals(3, container.getEntityCount());
	Assert.assertTrue(container.getEntityIDs().contains(new UUID(0, 0)));
	Assert.assertTrue(container.getEntityIDs().contains(new UUID(0, 1)));

	final Stream<Entity> entityStream = container.streamEntities();
	Assert.assertTrue(entityStream.filter(e -> e.getID().equals(new UUID(0, 0))).findAny().isPresent());

	Assert.assertEquals(new UUID(0, 0), container.getEntity(new UUID(0, 0)).getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addEntityTwiceTest() {
	container = new DefaultEntityContainer();
	final UUID id = new UUID(0, 0);
	container.newEntity(id);
	container.newEntity(id);
    }

    @After
    public void after() {
	container = null;
    }

    @Test
    public void delegateContainerTest() {
	container = new DefaultEntityContainer();
	container.newEntity();

	final DefaultEntityContainer otherContainer = new DefaultEntityContainer(new DefaultEntityFactory(), container);

	Assert.assertEquals(container, otherContainer.getDelegateContainer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateEntityTest() {
	container = new DefaultEntityContainer();

	container.newEntity(new UUID(0, 0));
	container.newEntity(new UUID(0, 0));
    }

    @Test
    public void entityListenerTest() {
	container = new DefaultEntityContainer();

	final UUID id = new UUID(0, 0);
	container.newEntity(id);

	final TestEntityListener entityListener = new TestEntityListener();
	container.addEntityListener(entityListener);

	Assert.assertTrue(container.getEntityListeners().contains(entityListener));

	container.removeEntityListener(entityListener);
	Assert.assertFalse(container.getEntityListeners().contains(entityListener));

	container.addEntityListener(entityListener);
	container.removeEntityListeners();
	Assert.assertFalse(container.getEntityListeners().contains(entityListener));
    }

    @Test
    public void equalsTest() {
	container = new DefaultEntityContainer();
	Assert.assertEquals(container, container);

	final DefaultEntityContainer otherContainer = new DefaultEntityContainer();
	Assert.assertEquals(container, otherContainer);

	final UUID id = new UUID(0, 0);
	container.newEntity(id);
	Assert.assertNotEquals(container, otherContainer);
	otherContainer.newEntity(id);
	Assert.assertEquals(container, otherContainer);

	final TestEntityListener entityListener = new TestEntityListener();
	container.addEntityListener(entityListener);
	Assert.assertNotEquals(container, otherContainer);
	otherContainer.addEntityListener(entityListener);
	Assert.assertEquals(container, otherContainer);
    }

    @Test(expected = IllegalStateException.class)
    public void exportButNotTransferTest() {
	container = new DefaultEntityContainer();

	final UUID id = new UUID(0, 0);
	container.newEntity(id);
	container.newEntity();

	final DefaultEntityContainer otherContainer = new DefaultEntityContainer();
	otherContainer.newEntity(id);

	container.transferEntity(id, otherContainer);
    }

    @Test
    public void factoryTest() {
	final DefaultEntityFactory entityFactory = new DefaultEntityFactory();
	container = new DefaultEntityContainer(entityFactory);

	Assert.assertEquals(entityFactory, container.getFactory());
    }

    @Test
    public void hashCodeTest() {
	container = new DefaultEntityContainer();

	final DefaultEntityContainer otherContainer = new DefaultEntityContainer();
	Assert.assertEquals(container.hashCode(), otherContainer.hashCode());

	final UUID id = new UUID(0, 0);
	container.newEntity(id);
	otherContainer.newEntity(id);
	Assert.assertEquals(container.hashCode(), otherContainer.hashCode());

	final TestEntityListener entityListener = new TestEntityListener();
	container.addEntityListener(entityListener);
	Assert.assertNotEquals(container.hashCode(), otherContainer.hashCode());
	otherContainer.addEntityListener(entityListener);
	Assert.assertEquals(container.hashCode(), otherContainer.hashCode());
    }

    @Test
    public void killEntityTest() {
	container = new DefaultEntityContainer();

	container.newEntity(new UUID(0, 0));

	TestEntityListener entityListener = new TestEntityListener();
	container.addEntityListener(entityListener);

	Assert.assertFalse(entityListener.killed);
	Assert.assertTrue(container.killEntity(new UUID(0, 0)));

	// Try killing the same entity twice.
	Assert.assertFalse(container.killEntity(new UUID(0, 0)));

	// Try killing an entity that's not in the container.
	Assert.assertFalse(container.killEntity(new UUID(0, 1)));
	Assert.assertTrue(entityListener.killed);

	container = new DefaultEntityContainer();
	container.newEntity(new UUID(0, 0));
	entityListener = new TestEntityListener();
	container.addEntityListener(entityListener);

	Assert.assertFalse(entityListener.killed);
	container.killEntities();
	Assert.assertTrue(entityListener.killed);
    }

    @Test
    public void receiveTest() {
	container = new DefaultEntityContainer();

	final UUID id = new UUID(0, 0);
	final DefaultEntityFactory factory = new DefaultEntityFactory();
	final DefaultEntityContainer container = new DefaultEntityContainer();
	final DefaultEntity entity = new DefaultEntity(id, factory, container);

	Assert.assertFalse(container.getEntityIDs().contains(id));
	Assert.assertTrue(container.receiveEntity(entity));
	Assert.assertTrue(container.getEntityIDs().contains(new UUID(0, 0)));

	// Try receiving the same entity twice.
	Assert.assertFalse(container.receiveEntity(entity));
    }

    @Test(expected = IllegalArgumentException.class)
    public void transferSameContainerTest() {
	container = new DefaultEntityContainer();

	final UUID id = new UUID(0, 0);
	container.newEntity(id);

	final DefaultEntityContainer otherContainer = new DefaultEntityContainer();
	otherContainer.newEntity(id);

	container.transferEntity(id, otherContainer);
    }

    @Test
    public void transferTest() {
	container = new DefaultEntityContainer();

	final UUID id = new UUID(0, 0);
	container.newEntity(id);

	final DefaultEntityContainer otherContainer = new DefaultEntityContainer();

	Assert.assertTrue(container.transferEntity(id, otherContainer));
	Assert.assertFalse(container.hasEntities());

	// Try transferring an entity that the container no longer contains.
	Assert.assertFalse(container.transferEntity(id, otherContainer));

    }
}
