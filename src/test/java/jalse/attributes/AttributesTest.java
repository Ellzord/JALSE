package jalse.attributes;

import jalse.entities.Entity;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class AttributesTest {

    AttributeType<?> attributeType = null;

    @After
    public void after() {
	attributeType = null;
    }

    @Test
    public void attributeTypeTest() {
	Assert.assertEquals(new AttributeType<Boolean>() {}, Attributes.BOOLEAN_TYPE);
	Assert.assertEquals(new AttributeType<Integer>() {}, Attributes.INTEGER_TYPE);
	Assert.assertEquals(new AttributeType<String>() {}, Attributes.STRING_TYPE);
	Assert.assertEquals(new AttributeType<Double>() {}, Attributes.DOUBLE_TYPE);
	Assert.assertEquals(new AttributeType<Character>() {}, Attributes.CHARACTER_TYPE);
	Assert.assertEquals(new AttributeType<Long>() {}, Attributes.LONG_TYPE);
	Assert.assertEquals(new AttributeType<Byte>() {}, Attributes.BYTE_TYPE);
	Assert.assertEquals(new AttributeType<Float>() {}, Attributes.FLOAT_TYPE);
	Assert.assertEquals(new AttributeType<Short>() {}, Attributes.SHORT_TYPE);
	Assert.assertEquals(new AttributeType<Object>() {}, Attributes.OBJECT_TYPE);

	Assert.assertEquals(new AttributeType<Entity>() {}, Attributes.newTypeOf(Entity.class));
	Assert.assertEquals(new AttributeType<Entity>() {}, Attributes.newUnknownType(Entity.class));
    }

    @Test
    public void emptyContainerTest() {
	Assert.assertTrue(Attributes.EMPTY_ATTRIBUTECONTAINER instanceof UnmodifiableDelegateAttributeContainer);
	Assert.assertTrue(Attributes.emptyAttributeContainer() instanceof UnmodifiableDelegateAttributeContainer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyStringTest() {
	Attributes.requireNotEmpty("");
    }

    @Test
    public void namedAttributeTypeTest() {
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.BOOLEAN_TYPE),
		Attributes.newNamedBooleanType("test"));
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.INTEGER_TYPE),
		Attributes.newNamedIntegerType("test"));
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.STRING_TYPE),
		Attributes.newNamedStringType("test"));
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.DOUBLE_TYPE),
		Attributes.newNamedDoubleType("test"));
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.CHARACTER_TYPE),
		Attributes.newNamedCharacterType("test"));
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.LONG_TYPE), Attributes.newNamedLongType("test"));
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.BYTE_TYPE), Attributes.newNamedByteType("test"));
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.FLOAT_TYPE),
		Attributes.newNamedFloatType("test"));
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.SHORT_TYPE),
		Attributes.newNamedShortType("test"));
	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.OBJECT_TYPE),
		Attributes.newNamedObjectType("test"));

	Assert.assertEquals(new NamedAttributeType<>("test", Attributes.newTypeOf(Entity.class)),
		Attributes.newNamedTypeOf("test", Entity.class));
	Assert.assertEquals(new NamedAttributeType<>("test", new AttributeType<Object>(Entity.class) {}),
		Attributes.newNamedUnknownType("test", Entity.class));
    }

    @Test
    public void notEmptyStringTest() {
	Assert.assertEquals("test", Attributes.requireNotEmpty("test"));
    }

    @Test
    public void unmodifiableAttributeContainerTest() {
	final AttributeContainer container = new DefaultAttributeContainer();

	Assert.assertTrue(Attributes.unmodifiableAttributeContainer(container) instanceof UnmodifiableDelegateAttributeContainer);
    }
}
