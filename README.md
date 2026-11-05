# Healthier Sleep

Fabric 1.21.1. Hunger no longer heals you. Health comes back while you sleep.

## Rules

- The vanilla "full hunger bar heals you" regeneration is off. Starvation damage, potions, golden apples and everything else that heals are untouched.
- Sleeping heals you, but only once you are fully asleep (the same moment vanilla counts you as sleeping through the night).
- Your food bar must be at least 75% full. At 75% a night heals 1 heart, at 100% it heals 5 hearts, linearly in between.
- With Inconvenient Temperature installed, your body temperature must be in the middle half of the bar. The outer quarter on each side blocks healing.
- With Dehydration installed, your thirst bar must be at least half full.
- The `naturalRegeneration` gamerule off disables sleep healing too.

## The night

The night no longer skips. When enough players are sleeping (the vanilla `playersSleepingPercentage` rule), the ambient clock runs at 6x speed until morning, then everyone wakes up and rain is cleared as in vanilla. Mobs, crops and entities keep ticking at normal speed.

Healing is paid per tick of ambient time you actually slept, so it is smooth and has no hidden rounding:

- Leaving the bed stops it where it is.
- Going back to bed continues from there.
