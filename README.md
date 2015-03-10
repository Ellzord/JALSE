## JALSE - Java Artificial Life Simulation Engine

JALSE is a lightweight framework for simple simulation written in Java 8.

### Summary of features
* Self-managed concurrent work engine.
  * Actions can be scheduled and cancelled.
  * Scheduled Actions can be periodic.
  * Actions can be run as part of a chain (to ensure execution order).
* Entity tree data structure.
  * Have unique IDs.
  * Can have Attributes (typed values).
    * Can use existing or final classes.
    * Add/Change/Remove events can have trigger listeners.
  * Can have child entities (tree).
    * Can filter and stream children for processing.
  * Can be marked as an Entity Type (at runtime).
    * Can define get/set methods (bean).
    * Supports inheritence.
    * Can be used to mark or group entities for processing.
  * Create/Kill events can have trigger listeners.
  * Can schedule its own work.
* Useful utilities
  * Can "cast" any Entity as any Entity Type.
  * Can add recursive trigger listener suppliers.
  * Can recursively walk through entity tree.

### How to use
Check out the [Wiki](https://github.com/Ellzord/JALSE/wiki) for more information or have a look at [HappyCows](https://github.com/Ellzord/JALSE-HappyCows) for an example project.

### Going forward
JALSE is still in development - to find out what is on the horizon see [Enhancements](https://github.com/Ellzord/JALSE/issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement) and [Future changes](https://github.com/Ellzord/JALSE/wiki/Future-changes)!

### Shout-outs
I use [JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html) for performance tuning.
