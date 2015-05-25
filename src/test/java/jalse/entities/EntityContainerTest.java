package jalse.entities;

import static jalse.entities.Entities.asType;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class EntityContainerTest {

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

    EntityContainer container = null;

    @Test
    public void addEntityTest() {
	container = new DefaultEntityContainer();

	Assert.assertFalse(container.hasEntities());
	container.newEntity();
	container.newEntity(new UUID(0, 0));
	Assert.assertEquals(2, container.getEntityCount());
	Assert.assertTrue(container.hasEntities());
	Assert.assertNotNull(container.getEntities());
	Assert.assertTrue(container.hasEntity(new UUID(0, 0)));
	Assert.assertFalse(container.hasEntity(new UUID(0, 100)));

	Optional<Entity> optEntity = container.getOptEntity(new UUID(0, 100));
	Assert.assertFalse(optEntity.isPresent());
	optEntity = container.getOptEntity(new UUID(0, 0));
	Assert.assertTrue(optEntity.isPresent());
    }

    @After
    public void after() {
	container = null;
    }

    @Test
    public void entityClassTest() {
	container = new DefaultEntityContainer();

	container.newEntity();
	container.newEntity(new UUID(0, 0));
	container.newEntity(TestEntity.class);
	container.newEntity(new UUID(0, 1), TestEntity.class);

	final HashSet<DefaultEntity> entities = (HashSet<DefaultEntity>) container
		.getEntitiesOfType(DefaultEntity.class);
	for (final Entity entity : entities) {
	    Assert.assertEquals(DefaultEntity.class, entity.getClass());
	}
	final Stream<DefaultEntity> entityStream = container.streamEntitiesOfType(DefaultEntity.class);
	entityStream.forEach(e -> Assert.assertEquals(DefaultEntity.class, e.getClass()));

	final Class<? extends TestEntity> type = asType(container.getEntity(new UUID(0, 0)), TestEntity.class)
		.getClass();
	Assert.assertEquals(type, container.getEntityAsType(new UUID(0, 0), TestEntity.class).getClass());
	Assert.assertNull(container.getEntityAsType(new UUID(0, 100), TestEntity.class));

	final HashSet<TestEntity> testEntities = (HashSet<TestEntity>) container.getEntitiesAsType(TestEntity.class);
	for (final Entity entity : testEntities) {
	    Assert.assertEquals(type, entity.getClass());
	}
	final Stream<TestEntity> testEntityStream = container.streamEntitiesAsType(TestEntity.class);
	testEntityStream.forEach(e -> Assert.assertEquals(type, e.getClass()));

	final Optional<TestEntity> optEntity = container.getOptEntityAsType(new UUID(0, 0), TestEntity.class);
	Assert.assertTrue(optEntity.isPresent());
	Assert.assertEquals(type, optEntity.get().getClass());
    }

    @Test
    public void entityContainerListenerTest() {
	container = new DefaultEntityContainer();

	final UUID id = new UUID(0, 0);
	container.newEntity(id);

	final TestEntityContainerListener entityContainerListener = new TestEntityContainerListener();
	Assert.assertFalse(container.hasEntityContainerListener(entityContainerListener));

	container.addEntityContainerListener(entityContainerListener);
	Assert.assertTrue(container.hasEntityContainerListener(entityContainerListener));
    }

    @Test
    public void transferTest() {
	container = new DefaultEntityContainer();

	final UUID id = new UUID(0, 0);
	container.newEntity(id);

	final DefaultEntityContainer otherContainer = new DefaultEntityContainer();
	Assert.assertTrue(container.transferAllEntities(otherContainer).isEmpty());
	Assert.assertFalse(container.hasEntities());

	final HashSet<UUID> idSet = new HashSet<UUID>();
	idSet.add(new UUID(0, 1));
	idSet.add(new UUID(0, 2));
	container.newEntity(new UUID(0, 1));
	container.newEntity(new UUID(0, 2));
	container.transferEntities(idSet, otherContainer).isEmpty();
	Assert.assertFalse(container.hasEntities());

	otherContainer.transferEntities(p -> !p.getID().equals(new UUID(0, 0)), container);
	Assert.assertTrue(otherContainer.getEntityIDs().contains(new UUID(0, 0)));
	Assert.assertFalse(otherContainer.getEntityIDs().contains(new UUID(0, 1)));
	Assert.assertFalse(container.getEntityIDs().contains(new UUID(0, 0)));
	Assert.assertTrue(container.getEntityIDs().contains(new UUID(0, 1)));
    }
}
