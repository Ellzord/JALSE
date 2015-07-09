package jalse.entities;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jalse.actions.Actions;
import jalse.actions.ForkJoinActionEngine;

public class DefaultEntityFactoryTest {

    private EntityContainer targetContainer;
    private EntityContainer sourceContainer;

    @Before
    public void before() {
	targetContainer = new DefaultEntityContainer.Builder().build();
	sourceContainer = new DefaultEntityContainer.Builder().build();
    }

    @Test
    public void createDefaultEntityFactory() {
	final DefaultEntityFactory factory = new DefaultEntityFactory();
	Assert.assertNotNull(factory);
	Assert.assertEquals(Integer.MAX_VALUE, factory.getEntityLimit());
	Assert.assertEquals(0, factory.getEntityCount());
	Assert.assertEquals(ForkJoinActionEngine.commonPoolEngine(), factory.getEngine());
    }

    @Test(expected = IllegalStateException.class)
    public void entityLimitTest() {
	final DefaultEntityFactory factory = new DefaultEntityFactory(1);

	factory.newEntity(new UUID(0, 1), targetContainer);
	Assert.assertEquals(1, factory.getEntityLimit());
	Assert.assertEquals(1, factory.getEntityCount());

	// Try adding another entity when entityCount is at limit.
	factory.newEntity(new UUID(0, 2), targetContainer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failExportEntityTest() {
	final DefaultEntityFactory factory = new DefaultEntityFactory();

	final DefaultEntity entity = factory.newEntity(new UUID(0, 1), targetContainer);
	final DefaultEntity childEntity = (DefaultEntity) entity.newEntity();

	factory.exportEntity(entity);
	Assert.assertNull(entity.getContainer());
	Assert.assertEquals(Actions.emptyActionEngine(), entity.getEngine());
	Assert.assertEquals(Actions.emptyActionEngine(), childEntity.getEngine());

	// Try exporting the same entity twice.
	factory.exportEntity(entity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEntityLimitTest() {
	new DefaultEntityFactory(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonUniqueEntityTest() {
	final DefaultEntityFactory factory = new DefaultEntityFactory();

	factory.newEntity(new UUID(0, 1), targetContainer);
	Assert.assertEquals(1, factory.getEntityCount());

	// Try using same UUID again.
	factory.newEntity(new UUID(0, 1), targetContainer);
    }

    @Test
    public void tryImportEntityTest() {
	final DefaultEntityFactory factory = new DefaultEntityFactory();
	final DefaultEntity entity = (DefaultEntity) sourceContainer.newEntity();
	final DefaultEntity childEntity = (DefaultEntity) entity.newEntity();

	Assert.assertTrue(factory.tryImportEntity(entity, targetContainer));
	Assert.assertEquals(targetContainer, entity.getContainer());
	Assert.assertEquals(factory.getEngine(), entity.getEngine());
	Assert.assertEquals(factory.getEngine(), childEntity.getEngine());

	// Try importing the same entity twice.
	Assert.assertFalse(factory.tryImportEntity(entity, targetContainer));
    }

    @Test
    public void tryKillEntityTest() {
	final DefaultEntityFactory factory = new DefaultEntityFactory();
	final Entity entity1 = factory.newEntity(new UUID(0, 1), targetContainer);
	final DefaultEntity entity2 = factory.newEntity(new UUID(0, 2), targetContainer);

	Assert.assertTrue(factory.tryKillEntity(entity1));
	// Try killing the same entity twice.
	Assert.assertFalse(factory.tryKillEntity(entity1));

	entity2.markAsDead();
	// Try killing a dead entity.
	Assert.assertFalse(factory.tryKillEntity(entity2));
    }

    @Test
    public void tryTakeFromTreeTest() {
	final DefaultEntityFactory factory = new DefaultEntityFactory();
	final Entity entityInFactoryTree = factory.newEntity(new UUID(0, 1), sourceContainer);
	final Entity entityNotInTree = targetContainer.newEntity();

	Assert.assertTrue(factory.tryTakeFromTree(entityInFactoryTree, targetContainer));
	// Try taking an entity not in tree from tree.
	Assert.assertFalse(factory.tryTakeFromTree(entityNotInTree, targetContainer));
    }
}
