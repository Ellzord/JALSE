package jalse;

import jalse.actions.Action;
import jalse.actions.ActionContext;
import jalse.actions.ForkJoinActionEngine;
import jalse.actions.ThreadPoolActionEngine;
import jalse.attributes.DefaultAttributeContainer;
import jalse.entities.DefaultEntityFactory;
import jalse.entities.Entity;
import jalse.entities.EntityEvent;
import jalse.entities.EntityListener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class DefaultJALSETest {

    private class TestAction implements Action<JALSE> {

	public int actCount = 0;

	@Override
	public void perform(final ActionContext<JALSE> context) throws InterruptedException {
	    actCount++;
	}
    }

    private interface TestEntity extends Entity {}

    private class TestEntityListener implements EntityListener {

	public boolean created = false;
	public boolean killed = false;

	@Override
	public void entityCreated(final EntityEvent event) {
	    created = true;
	}

	@Override
	public void entityKilled(final EntityEvent event) {
	    killed = true;
	}

	@Override
	public void entityReceived(final EntityEvent event) {}

	@Override
	public void entityTransferred(final EntityEvent event) {}
    }

    JALSE jalse = null;

    @After
    public void after() {
	jalse = null;
    }

    @Test
    public void bindingsTest() {
	jalse = new DefaultJALSE(new UUID(0, 0));
	Assert.assertTrue(jalse.getBindings().toMap().isEmpty());
    }

    @Test
    public void contextTest() {
	jalse = new DefaultJALSE(new UUID(0, 1));
	Assert.assertNotNull(jalse.newContext(new TestAction()));
    }

    @Test
    public void contructorTest() {
	jalse = new DefaultJALSE(new UUID(0, 0));
	Assert.assertNotNull(jalse);

	jalse = new DefaultJALSE(new UUID(0, 0), new ForkJoinActionEngine());
	Assert.assertNotNull(jalse);

	jalse = new DefaultJALSE(new UUID(0, 0), new ForkJoinActionEngine(), new DefaultEntityFactory());
	Assert.assertNotNull(jalse);
    }

    @Test
    public void entityListenerTest() {
	jalse = new DefaultJALSE(new UUID(0, 0));
	final TestEntityListener listener = new TestEntityListener();

	jalse.addEntityListener(listener);
	Assert.assertTrue(jalse.getEntityListeners().contains(listener));

	Assert.assertFalse(listener.created);
	Assert.assertFalse(listener.killed);

	jalse.newEntity(new UUID(0, 1));
	Assert.assertTrue(listener.created);

	jalse.killEntity(new UUID(0, 1));
	Assert.assertTrue(listener.killed);

	jalse.removeEntityListener(listener);
	Assert.assertFalse(jalse.getEntityListeners().contains(listener));

	jalse.addEntityListener(new TestEntityListener());
	jalse.addEntityListener(new TestEntityListener());
	jalse.removeEntityListeners();
	Assert.assertTrue(jalse.getEntityListeners().isEmpty());
    }

    @Test
    public void killEntityTest() {
	jalse = new DefaultJALSE(new UUID(0, 1));

	final Entity entity1 = jalse.newEntity(new UUID(0, 1));
	final Entity entity2 = jalse.newEntity(new UUID(0, 2));
	final Entity entity3 = jalse.newEntity(new UUID(0, 3));

	Assert.assertTrue(entity1.isAlive());
	Assert.assertTrue(entity2.isAlive());
	Assert.assertTrue(entity3.isAlive());

	jalse.killEntity(new UUID(0, 1));
	Assert.assertFalse(entity1.isAlive());
	Assert.assertTrue(entity2.isAlive());
	Assert.assertTrue(entity3.isAlive());

	jalse.killEntities();
	Assert.assertFalse(entity1.isAlive());
	Assert.assertFalse(entity2.isAlive());
	Assert.assertFalse(entity3.isAlive());
    }

    @Test
    public void newEntityTest() {
	jalse = new DefaultJALSE(new UUID(0, 0));

	final UUID id = new UUID(0, 1);
	final Entity entity = jalse.newEntity(id, TestEntity.class, new DefaultAttributeContainer());
	Assert.assertEquals(1, jalse.getEntityCount());
	Assert.assertTrue(jalse.getEntityIDs().contains(id));
	Assert.assertEquals(entity, jalse.getEntity(id));

	final UUID otherID = new UUID(0, 2);
	entity.newEntity(otherID);
	Assert.assertEquals(1, jalse.getEntityCount());
	Assert.assertEquals(2, jalse.getTreeCount());
	Assert.assertTrue(jalse.getIDsInTree().contains(id));
	Assert.assertTrue(jalse.getIDsInTree().contains(otherID));

	final UUID thirdID = new UUID(0, 3);
	final DefaultJALSE jalse2 = new DefaultJALSE(new UUID(0, 1));
	final Entity otherEntity = jalse2.newEntity(thirdID);

	jalse.receiveEntity(otherEntity);
	Assert.assertTrue(jalse.getEntityIDs().contains(thirdID));

	Stream<Entity> entityStream = jalse.streamEntities();
	Assert.assertTrue(entityStream.filter(e -> e.getID().equals(id)).findAny().isPresent());

	entityStream = jalse.streamEntityTree();
	Assert.assertTrue(entityStream.filter(e -> e.getID().equals(otherID)).findAny().isPresent());
    }

    @Test
    public void pauseTest() {
	jalse = new DefaultJALSE(new UUID(0, 0), new ThreadPoolActionEngine(1), new DefaultEntityFactory());
	Assert.assertFalse(jalse.isPaused());

	jalse.pause();
	Assert.assertTrue(jalse.isPaused());

	jalse.resume();
	Assert.assertFalse(jalse.isPaused());
    }

    @Test
    public void scheduleTest() {
	jalse = new DefaultJALSE(new UUID(0, 0));
	final TestAction action = new TestAction();
	jalse.scheduleForActor(action, 0, 1, TimeUnit.MILLISECONDS);
	try {
	    Thread.sleep(10);
	} catch (final InterruptedException e) {
	    Assert.fail();
	}
	Assert.assertNotEquals(0, action.actCount);

	jalse.cancelAllScheduledForActor();
	final int actCount = action.actCount;

	try {
	    Thread.sleep(10);
	} catch (final InterruptedException e) {
	    Assert.fail();
	}
	Assert.assertEquals(actCount, action.actCount);
    }

    @Test
    public void stopTest() {
	jalse = new DefaultJALSE(new UUID(0, 0), new ThreadPoolActionEngine(1), new DefaultEntityFactory());
	Assert.assertFalse(jalse.isStopped());

	jalse.stop();
	Assert.assertTrue(jalse.isStopped());
    }

    @Test
    public void tagTest() {
	jalse = new DefaultJALSE(new UUID(0, 0));
	Assert.assertTrue(jalse.getTags().isEmpty());
    }

    @Test
    public void transferEntityTest() {
	jalse = new DefaultJALSE(new UUID(0, 0));
	final DefaultJALSE otherJALSE = new DefaultJALSE(new UUID(0, 1));

	final UUID id = new UUID(0, 2);
	jalse.newEntity(id);
	jalse.transferEntity(id, otherJALSE);

	Assert.assertFalse(jalse.getEntityIDs().contains(id));
	Assert.assertTrue(otherJALSE.getEntityIDs().contains(id));
    }
}
