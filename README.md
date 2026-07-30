# GregTech Optimizations

Performance optimizations for GregTech Modern 7.5.3.

## What it changes

### Recipe lookup

#### In short, it adds a cache that speeds some things up in passive setups.

- Machines track a version counter for their input handlers. If a recipe search found
  nothing and no input changed since, the search is skipped.
- RecipeRunner caches the grouped handler lists per machine and rebuilds them only
  when the machine's handler topology changes.
- Empty handlers are skipped earlier, and recipes with no contents return early.

### Machine Controller Cover

The cover polls its redstone input every 40 ticks to fix that sometimes the cover could malfunction and stop machine without signal or the other way around.

### ME Stocking Bus/Hatch

ME stocking buses and hatches no longer poll the network. They get notified about changes from AE2 instead.

### ME Pattern Buffers

They now run one worker per proxy + 1 for the buffer itself, and have shared internal slots instead of one slot per pattern. This way machine has fewer busses to scan for recipes. They work the same from the players perspective because the slots allow the same pattern to be pushed and are distinct.

### Power failing

There is one option in the config. The default (false) keeps vanilla GregTech machine cover and power failing behavior. If you turn this on, all machines act like they have a machine cover with "disable power failing" turned on, until you place a cover and disable it yourself.