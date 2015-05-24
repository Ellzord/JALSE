package jalse.entities;

import jalse.actions.Action;
import jalse.actions.ActionContext;
import jalse.actions.DefaultActionScheduler;
import jalse.attributes.Attributes;
import jalse.attributes.DefaultAttributeContainer;
import jalse.listeners.AttributeEvent;
import jalse.listeners.AttributeListener;
import jalse.listeners.EntityContainerEvent;
import jalse.listeners.EntityContainerListener;
import jalse.tags.EntityType;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class DefaultEntityTest {

    private class TestAction implements Action<Entity> {

	@Override
	public void perform(final ActionContext<Entity> context) throws InterruptedException {}
    }

    private class TestAttributeListener implements AttributeListener<Integer> {

	public boolean present = false;
	public int val = 0;

	@Override
	public void attributeAdded(final AttributeEvent<Integer> event) {
	    present = true;
	    val = event.getValue();
	}

	@Override
	public void attributeChanged(final AttributeEvent<Integer> event) {
	    val = event.getValue();
	}

	@Override
	public void attributeRemoved(final AttributeEvent<Integer> event) {
	    present = false;
	}
    }

    private interface TestEntity extends Entity {}

    private class TestEntityContainerListener implements EntityContainerListener {

	@Override
	public void entityCreated(final EntityContainerEvent event) {}

	@Override
	public void entityKilled(final EntityContainerEvent event) {}

	@Override
	public void entityReceived(final EntityContainerEvent event) {}

	@Override
	public void entityTransferred(final EntityContainerEvent event) {}
    }

    DefaultEntity entity = null;

    @After
    public void after() {
	entity = null;
    }

    @Test
    public void attributeListenerTest() {
	entity = createDefaultEntity();

	final TestAttributeListener testAttributeListener = new TestAttributeListener();
	Assert.assertFalse(testAttributeListener.present);

	entity.addAttributeListener("test", Attributes.INTEGER_TYPE, testAttributeListener);
	Assert.assertTrue(entity.getAttributeListeners("test", Attributes.INTEGER_TYPE).contains(testAttributeListener));
	Assert.assertTrue(entity.getAttributeListenerNames().contains("test"));
	Assert.assertTrue(entity.getAttributeListenerTypes("test").contains(Attributes.INTEGER_TYPE));

	entity.setAttribute("test", Attributes.INTEGER_TYPE, 10);
	Assert.assertTrue(testAttributeListener.present);
	Assert.assertEquals(10, testAttributeListener.val);

	entity.setAttribute("test", Attributes.INTEGER_TYPE, 42);
	entity.fireAttributeChanged("test", Attributes.INTEGER_TYPE);
	Assert.assertEquals(42, testAttributeListener.val);

	entity.removeAttribute("test", Attributes.INTEGER_TYPE);
	Assert.assertFalse(testAttributeListener.present);

	entity.removeAttributeListener("test", Attributes.INTEGER_TYPE, testAttributeListener);
	Assert.assertFalse(entity.getAttributeListeners("test", Attributes.INTEGER_TYPE)
		.contains(testAttributeListener));

	entity.addAttributeListener("test", Attributes.INTEGER_TYPE, testAttributeListener);
	Assert.assertTrue(entity.getAttributeListeners("test", Attributes.INTEGER_TYPE).contains(testAttributeListener));
	entity.removeAttributeListeners("test", Attributes.INTEGER_TYPE);
	Assert.assertFalse(entity.getAttributeListeners("test", Attributes.INTEGER_TYPE)
		.contains(testAttributeListener));

	entity.addAttributeListener("test", Attributes.INTEGER_TYPE, testAttributeListener);
	Assert.assertTrue(entity.getAttributeListeners("test", Attributes.INTEGER_TYPE).contains(testAttributeListener));
	entity.removeAttributeListeners();
	Assert.assertFalse(entity.getAttributeListeners("test", Attributes.INTEGER_TYPE)
		.contains(testAttributeListener));
    }

    @Test
    public void attributeTest() {
	entity = createDefaultEntity();
	entity.setAttribute("test", Attributes.INTEGER_TYPE, 10);
	Assert.assertEquals(10, (int) entity.getAttribute("test", Attributes.INTEGER_TYPE));
	Assert.assertTrue(entity.getAttributeTypes("test").contains(Attributes.INTEGER_TYPE));

	entity.setAttribute("test2", Attributes.DOUBLE_TYPE, 3.14);
	Assert.assertEquals(2, entity.getAttributeCount());

	final Set<String> entityAttributeNames = entity.getAttributeNames();
	Assert.assertTrue(entityAttributeNames.contains("test"));
	Assert.assertTrue(entityAttributeNames.contains("test2"));
	Assert.assertNotNull(entity.streamAttributes());

	entity.removeAttribute("test", Attributes.INTEGER_TYPE);
	Assert.assertFalse(entity.getAttributeNames().contains("test"));

	entity.removeAttributes();
	Assert.assertTrue(entity.getAttributeNames().isEmpty());
    }

    @Test
    public void buildEntityTest() {
	entity = createDefaultEntity();

	Assert.assertNotNull(entity);
    }

    public DefaultEntity createDefaultEntity() {
	final UUID id = new UUID(0, 0);
	final DefaultEntityFactory factory = new DefaultEntityFactory();
	final DefaultEntityContainer container = new DefaultEntityContainer();

	return new DefaultEntity(id, factory, container);
    }

    @Test(expected = IllegalStateException.class)
    public void deadEntityTest() {
	entity = createDefaultEntity();

	entity.markAsDead();
	Assert.assertFalse(entity.isAlive());
	Assert.assertNull(entity.getContainer());
	entity.newEntity(new UUID(0, 1), new DefaultAttributeContainer());
    }

    @Test
    public void entityContainerListenerTest() {
	entity = createDefaultEntity();
	final TestEntityContainerListener testEntityContainerListener = new TestEntityContainerListener();

	entity.addEntityContainerListener(testEntityContainerListener);
	Assert.assertTrue(entity.getEntityContainerListeners().contains(testEntityContainerListener));
	entity.removeEntityContainerListener(testEntityContainerListener);
	Assert.assertFalse(entity.getEntityContainerListeners().contains(testEntityContainerListener));

	entity.addEntityContainerListener(testEntityContainerListener);
	Assert.assertTrue(entity.getEntityContainerListeners().contains(testEntityContainerListener));
	entity.removeEntityContainerListeners();
	Assert.assertFalse(entity.getEntityContainerListeners().contains(testEntityContainerListener));
    }

    @Test
    public void getEntityTest() {
	entity = createDefaultEntity();
	entity.markAsAlive();

	entity.newEntity(new UUID(0, 1), new DefaultAttributeContainer());
	Assert.assertEquals(1, entity.getEntityCount());
	Assert.assertTrue(entity.getEntityIDs().contains(new UUID(0, 1)));
	Assert.assertNotNull(entity.getEntity(new UUID(0, 1)));
    }

    @Test
    public void killEntityTest() {
	entity = createDefaultEntity();
	entity.markAsAlive();

	entity.newEntity(new UUID(0, 0), new DefaultAttributeContainer());
	Assert.assertNotNull(entity.getEntity(new UUID(0, 0)));

	Assert.assertFalse(entity.kill());
	entity.newEntity(new UUID(0, 1), new DefaultAttributeContainer());
	entity.newEntity(new UUID(0, 2), new DefaultAttributeContainer());
	entity.newEntity(new UUID(0, 3), new DefaultAttributeContainer());

	Assert.assertNotNull(entity.getEntity(new UUID(0, 1)));
	Assert.assertNotNull(entity.getEntity(new UUID(0, 2)));
	Assert.assertNotNull(entity.getEntity(new UUID(0, 3)));

	Assert.assertTrue(entity.killEntity(new UUID(0, 1)));
	Assert.assertNull(entity.getEntity(new UUID(0, 1)));

	entity.killEntities();
	Assert.assertNull(entity.getEntity(new UUID(0, 2)));
	Assert.assertNull(entity.getEntity(new UUID(0, 3)));
    }

    @Test
    public void liveEntityTest() {
	final UUID id = new UUID(0, 0);
	final DefaultEntityFactory factory = new DefaultEntityFactory();
	final DefaultEntityContainer container = new DefaultEntityContainer();

	entity = new DefaultEntity(id, factory, container);

	entity.markAsAlive();
	entity.newEntity(new UUID(0, 1), new DefaultAttributeContainer());
	Assert.assertEquals(1, entity.getEntityCount());
	Assert.assertTrue(entity.isAlive());
	Assert.assertEquals(container, entity.getContainer());
    }

    @Test
    public void markAsTypeTest() {
	entity = createDefaultEntity();
	Assert.assertTrue(entity.markAsType(TestEntity.class));

	Assert.assertTrue(entity.getTags().contains(new EntityType(TestEntity.class)));

	Assert.assertFalse(entity.markAsType(TestEntity.class));

	entity.unmarkAsType(TestEntity.class);
	Assert.assertFalse(entity.getTags().contains(new EntityType(TestEntity.class)));
    }

    @Test(expected = IllegalStateException.class)
    public void newEntityTypeTest() {
	entity = createDefaultEntity();
	entity.markAsAlive();

	entity.newEntity(new UUID(0, 1), TestEntity.class, new DefaultAttributeContainer());
	Assert.assertEquals(1, entity.getEntityCount());

	entity.markAsDead();
	entity.newEntity(new UUID(0, 2), TestEntity.class, new DefaultAttributeContainer());
    }

    @Test
    public void receiveEntityTest() {
	entity = createDefaultEntity();

	final UUID id = new UUID(0, 1);
	final DefaultEntityFactory factory = new DefaultEntityFactory();
	final DefaultEntityContainer container = new DefaultEntityContainer();

	final DefaultEntity otherEntity = new DefaultEntity(id, factory, container);
	otherEntity.markAsAlive();

	entity.receiveEntity(otherEntity);

	otherEntity.transfer(container);
	Assert.assertEquals(otherEntity.getContainer(), container);
    }

    @Test(expected = IllegalStateException.class)
    public void scheduleDeadTest() {
	entity = createDefaultEntity();
	entity.markAsDead();

	entity.scheduleForActor(new TestAction(), 1, 10, TimeUnit.SECONDS);
    }

    @Test
    public void scheduleLiveTest() {
	entity = createDefaultEntity();
	entity.markAsAlive();

	Assert.assertEquals(new DefaultActionScheduler<>(entity).getEngine(), entity.getEngine());
	entity.scheduleForActor(new TestAction(), 1, 10, TimeUnit.SECONDS);
    }
}
