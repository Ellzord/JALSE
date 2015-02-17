## JALSE - Java Artificial Life Simulation Engine

JALSE is a lightweight framework for simple simulation written in Java 8.

### Summary of features

#### JALSE
* Self-managed concurrent work engine.
* Can schedule Actions to run in the future.
* Can have periodic Actions.
* Can cancel work.
* Can set first/last run actions.
* Can chain Actions that need to run in order.
* Can Stream and filter Clusters.

#### Clusters
* Have unique IDs.
* Can have Attributes.
* Used to Group agents.
* Updates can have trigger listeners.
* Can automatically apply tigger listeners to Agents.
* Can have Actions scheduled against them.
* Can Stream and filter Agents.

#### Agents
* Have unique IDs.
* Can have Attributes.
* Can write bean style interfaces to use as Agent types.
* Can be used as different Agent types.
* Updates can have trigger listeners.
* Can have Actions scheduled against them.

### How to use
Check out the [Wiki](https://github.com/Ellzord/JALSE/wiki) for more information or have a look at [HappyCows](https://github.com/Ellzord/JALSE-HappyCows) for an example project.

### Going forward
JALSE is still in development - to find out what is on the horizon see [Enhancements](https://github.com/Ellzord/JALSE/issues?q=is%3Aopen+is%3Aissue+label%3Aenhancement) and [Future changes](https://github.com/Ellzord/JALSE/wiki/Future-changes)!

### Shout-outs
I use [JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html) for performance tuning.
