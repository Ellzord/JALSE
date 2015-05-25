package jalse.entities;

import jalse.attributes.DefaultAttributeContainer;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class DefaultEntityContainerTest {

    private interface TestEntity extends Entity {}

    private class TestEntityContainerListener implements EntityContainerListener {

	public boolean killed = false;

	@Override
	public void entityCreated(final EntityContainerEvent event) {}

	@Override
	public void entityKilled(final EntityContainerEvent event) {
	    killed = true;
	}

	@Override
	public void entityReceived(final EntityContainerEvent event) {}

	@Override
	public void entityTransferred(final EntityContainerEvent event) {}
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
    public void entityContainerListenerTest() {
	container = new DefaultEntityContainer();

	final UUID id = new UUID(0, 0);
	container.newEntity(id);

	final TestEntityContainerListener entityContainerListener = new TestEntityContainerListener();
	container.addEntityContainerListener(entityContainerListener);

	Assert.assertTrue(container.getEntityContainerListeners().contains(entityContainerListener));

	container.removeEntityContainerListener(entityContainerListener);
	Assert.assertFalse(container.getEntityContainerListeners().contains(entityContainerListener));

	container.addEntityContainerListener(entityContainerListener);
	container.removeEntityContainerListeners();
	Assert.assertFalse(container.getEntityContainerListeners().contains(entityContainerListener));
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

	final TestEntityContainerListener entityContainerListener = new TestEntityContainerListener();
	container.addEntityContainerListener(entityContainerListener);
	Assert.assertNotEquals(container, otherContainer);
	otherContainer.addEntityContainerListener(entityContainerListener);
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
	container = new DefaultEntityContainer(entityFactory, null);

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

	final TestEntityContainerListener entityContainerListener = new TestEntityContainerListener();
	container.addEntityContainerListener(entityContainerListener);
	Assert.assertNotEquals(container.hashCode(), otherContainer.hashCode());
	otherContainer.addEntityContainerListener(entityContainerListener);
	Assert.assertEquals(container.hashCode(), otherContainer.hashCode());
    }

    @Test
    public void killEntityTest() {
	container = new DefaultEntityContainer();

	container.newEntity(new UUID(0, 0));

	TestEntityContainerListener entityContainerListener = new TestEntityContainerListener();
	container.addEntityContainerListener(entityContainerListener);

	Assert.assertFalse(entityContainerListener.killed);
	Assert.assertTrue(container.killEntity(new UUID(0, 0)));

	// Try killing the same entity twice.
	Assert.assertFalse(container.killEntity(new UUID(0, 0)));

	// Try killing an entity that's not in the container.
	Assert.assertFalse(container.killEntity(new UUID(0, 1)));
	Assert.assertTrue(entityContainerListener.killed);

	container = new DefaultEntityContainer();
	container.newEntity(new UUID(0, 0));
	entityContainerListener = new TestEntityContainerListener();
	container.addEntityContainerListener(entityContainerListener);

	Assert.assertFalse(entityContainerListener.killed);
	container.killEntities();
	Assert.assertTrue(entityContainerListener.killed);
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
