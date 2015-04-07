## [JALSE](https://ellzord.github.io/JALSE) - Java Artificial Life Simulation Engine
JALSE is a lightweight framework for simple simulation written in Java 8. The framework provides dynamic yet typed entities that can be processed concurrently. The underlying implementation is up to you - by default its there but it can be replaced.

By [Elliot Ford](https://twitter.com/ellzord)

### Why use JALSE?
1. Its free and works right out of the box!
2. You can a self-managed living data model for your simulation (or game) easily.
3. Entities are soft-typed (add and remove at runtime) and can be used as any type (without error).
4. Entities can be filtered and processed easily (by type, ID or attributes).
5. JALSE is backed by an ActionEngine that can schedule run-once and periodic tasks (several implementations).

### Getting started
* [Download](https://github.com/Ellzord/JALSE/releases) or fork JALSE.
* Check out the [Wiki](https://github.com/Ellzord/JALSE/wiki).
* See the [API docs](http://ellzord.github.io/JALSE/docs/).
* Have a look at the example projects ([HappyCows](https://github.com/Ellzord/JALSE-HappyCows) and [Messengers](https://github.com/Ellzord/JALSE-Messengers)).

### Code Snippets
Creating and using a simple entity type:
```java
public interface Friend extends Entity {

  @GetAttribute("name")
  String getName();
  
  @SetAttribute("name")
  void setName(String name);
}

Friend f = jalse.newEntity(Friend.class);
f.setName("Ellzord");

assert("Ellzord".equals(f.getName()));
```

Feeding all animals (not just the birds):
```java
public interface Animal extends Entity{}
public interface FlyingAnimal extends Animal{}

jalse.streamEntitiesOfType(Animal.class).foreach(/* Feed */);
```

Replacing fallen enemies:
```java
jalse.addEntityListener(new EntityAdapter() {

  public void entityKilled(EntityEvent event) {
    if (event.getEntity().isMarkedAsType(Evil.class)) {
      /* Spawn more */
    }
  }
});
```

Adrenaline when life is at risk:
```java
entity.addAttributeListener("danger", Attributes.BOOLEAN_TYPE, new AttributeAdapter<Boolean>(){

  public void attributeAdded(AttributeEvent<Boolean> event) {
    if (event.getValue()) {
      /* Run like hell */
    }
  }
});
```

Managing a crash landing:
```java
StrandedSurvivors living = jalse.newEntity(StrandedSurvivors.class);
for (int i = 0; i < 10; i++) {
  living.newSurvivor();
}

living.scheduleForActor(new Action<StrandedSurvivors>() {

  public void perform(ActionContext<StrandedSurvivors> context) {
    context.getActor().streamSurvivors().foreach(Survivor::lookForFood);
  }
}, 0, 1, TimeUnit.SECONDS);
```

On the fly values:
```java
entity.addAttribute("falling", Attributes.BOOLEAN_TYPE, true);

...

entity.removeAttribute("falling", Attribute.BOOLEAN_TYPE);
entity.addAttribute("death", Attributes.newTypeOf(Date.class), new Date());
```

### Model key
![GitHub Logo](/jalse-model-key.png)

### Model
![GitHub Logo](/jalse-model.png)

### Going forward
JALSE is still in development - to find out what is on the horizon see [Enhancements](https://github.com/Ellzord/JALSE/issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement) and [Future changes](https://github.com/Ellzord/JALSE/wiki/Future-changes)!

### Licence
See [LICENCE](https://github.com/Ellzord/JALSE/blob/master/LICENSE).

### Shout-outs
I use [JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html) for performance tuning.
