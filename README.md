# MinersAdvantage
![](https://raw.githubusercontent.com/DuelMonster/MinersAdvantage/MC_1.15.2/src/main/resources/MinersAdvantage_Banner.png)

## Overview

MinersAdvantage is the "I came to mine, not file paperwork" mod.

It is another full rewrite of the old MC 1.20 era version: keeping the same beloved quality-of-life components, remove the historical pain points, and make the whole thing saner for both singleplayer and multiplayer. You still get massive time savings when mining, farming, and tree clearing, but with modern Fabric and NeoForge support and cleaner behavior under server load.

If you have ever thought "I only wanted a few stacks of stone, why am I still here 45 minutes later", this mod was made for you.

## Features

- Captivation: automatically pulls nearby items and XP to you (magnet mode style).
- Cropination: harvests/replants mature crops with the right-click of a hoe.
- Cultivation: boosts farming behavior by auto-tilling valid farmland around valid water/hydration conditions.
- Excavation: mines connected blocks in an area instead of one block at a time.
- Pathanation: builds path trails quickly using your shovel workflow.
- Illumination: places torches while you mine so darkness stops ambushing you.
- Lumbination: chops full trees with trunk and leaf-aware logic.
- Shaftanation: cuts configurable strip-mine shafts.
- Substitution: auto-picks the best hotbar tool based on context and rules.
- Veination: mines complete ore veins with configurable controls.
- Ventilation: digs vertical shafts (up or down) with optional ladder placement.
- Server-aware syncing: gameplay settings are synchronized and can be server-authoritative in multiplayer.
- Multi-loader + multi-version support: Fabric and NeoForge builds are produced from one shared codebase.

## How It Works

### Captivation

Captivation watches for item entities and XP orbs in your configured radius and pulls in what is allowed by your list mode. You can run it as a whitelist or blacklist model, and it can also apply an unconditional blacklist so specific items never get vacuumed up.

### Cropination

Cropination has two main farming quality-of-life features: crop harvesting with immediate replanting when right-clicking a hoe on your crops, and optional seed harvesting that lets you choose whether you optimize for clean output or seed stockpile growth.

### Cultivation

Cultivation focuses on automatic tilling with hydration-driven farming checks so farmland automation only happens where water support is valid. It exists to make farming automation feel intentional instead of "randomly angry".

### Excavation

Excavation breaks matching blocks in a configurable volume with per-tick processing limits, block-variant handling, and list filtering logic. It supports hold/toggle style usage and cooperates with other systems like Illumination and Veination when those shared options are enabled.

### Pathanation

Pathanation converts grass/dirt routing into quick path creation with configurable width and length. It is effectively your "build roads without carpal tunnel" button and works on all types of dirt.

### Illumination

Illumination monitors local light and places torches when your threshold is crossed. You keep mining, it handles the "surprise creeper corner" prevention program.

### Lumbination

Lumbination traverses trunk and leaf space to identify the full tree and remove it according to your policy settings. You can control leaf handling, durability behavior, and sapling replanting so it matches your preferred balance between speed and sustainability.

### Shaftanation

Shaftanation digs straight mining corridors using configurable width, height, depth, and per-tick pace. You choose the shaft shape budget, it performs the repetitive labor.

### Substitution

Substitution evaluates the block/entity context and swaps to the best tool in your hotbar, then can optionally switch back. The rule profile supports action-aware target matching and advanced expression-based prioritization for players who like precise control.

### Veination

Veination expands ore mining into full vein extraction with distance limits and optional timing modifiers. It can also control whether sneaking is required and where harvested ore drops are placed.

### Ventilation

Ventilation creates vertical access shafts with configurable dimensions and throughput. Optional ladder placement helps turn that shaft into an actually usable route instead of a stylish pit.

### Multiplayer Sync and Enforcement

Client-only visual settings remain local, while gameplay-affecting settings are synchronized from server state in multiplayer. In short: your outline colors are your business, server gameplay rules are the server's business.

## Installation

### Fabric

1. Install a supported Minecraft version and Fabric Loader.
2. Install Fabric API for the same game version.
3. Download the MinersAdvantage Fabric jar for your target version.
4. Place the jar in your `mods` folder.
5. Launch the game.

### NeoForge

1. Install a supported Minecraft version and NeoForge.
2. Download the MinersAdvantage NeoForge jar for your target version.
3. Place the jar in your `mods` folder.
4. Launch the game.

### Multiplayer Note

For feature parity and consistent behavior, install MinersAdvantage on both client and server.

## Configuration

MinersAdvantage stores configuration as TOML in split roots:

- Client file: `minersadvantage-client-config.toml`
- Server gameplay file: `minersadvantage-server-config.toml`

Client options stay local. Gameplay options are server-authoritative in multiplayer.

### Client Config (`minersadvantage-client-config.toml`)

| Option                         | Type    | Default    | Description                                    |
| ------------------------------ | ------- | ---------- | ---------------------------------------------- |
| disable_particle_effects       | boolean | false      | Disables feature particle effects when true.   |
| debug_logging                  | boolean | false      | Enables verbose MinersAdvantage debug logging. |
| outline_foreground_color_argb  | color   | 0xFF40D9C0 | ARGB color used for foreground outlines.       |
| outline_see_through_color_argb | color   | 0x4B40D9C0 | ARGB color used for see-through outlines.      |

### Common Gameplay (`minersadvantage-server-config.toml`)

| Option                   | Type    | Default | Range                                | Description                                                                   |
| ------------------------ | ------- | ------- | ------------------------------------ | ----------------------------------------------------------------------------- |
| common.tps_guard         | boolean | true    | -                                    | Reduces aggressive behavior under low TPS conditions.                         |
| common.gather_drops      | boolean | false   | -                                    | Enables drop gathering helpers where applicable.                              |
| common.auto_illuminate   | boolean | true    | -                                    | Allows compatible features to trigger Illumination behavior.                  |
| common.mine_veins        | boolean | true    | -                                    | Allows compatible features to trigger Veination behavior.                     |
| common.blocks_per_tick   | int     | 1       | input: 1 to 1024, effective: 1 to 64 | Shared per-tick worker budget cap before feature-specific limits are applied. |
| common.enable_tick_delay | boolean | true    | -                                    | Enables tick-delay pacing.                                                    |
| common.tick_delay        | int     | 5       | input: 0 to 200, effective: 0 to 40  | Extra ticks between processing windows when tick delay is enabled.            |
| common.block_radius      | int     | 3       | input: 1 to 128, effective: 1 to 16  | Shared search radius for relevant feature operations.                         |

Effective per-feature worker budget is `min(common.blocks_per_tick, <feature>.processes_per_tick)`.

### Captivation (`minersadvantage-server-config.toml`)

| Option                              | Type         | Default                                     | Range    | Description                                                 |
| ----------------------------------- | ------------ | ------------------------------------------- | -------- | ----------------------------------------------------------- |
| captivation.enabled                 | boolean      | true                                        | -        | Enables or disables Captivation.                            |
| captivation.allow_in_gui            | boolean      | false                                       | -        | Allows capture behavior while GUI screens are open.         |
| captivation.radius_horizontal       | int          | 16                                          | 1 to 128 | Horizontal pickup radius.                                   |
| captivation.radius_vertical         | int          | 16                                          | 1 to 128 | Vertical pickup radius.                                     |
| captivation.is_whitelist            | boolean      | false                                       | -        | Treats `blacklist` as a whitelist when true.                |
| captivation.unconditional_blacklist | boolean      | false                                       | -        | Always excludes listed entries, even under whitelist logic. |
| captivation.blacklist               | list<string> | ["minecraft:rotten_flesh", "minecraft:egg"] | -        | Item IDs used for whitelist/blacklist filtering.            |

### Cropination (`minersadvantage-server-config.toml`)

| Option                    | Type    | Default | Description                                |
| ------------------------- | ------- | ------- | ------------------------------------------ |
| cropination.enabled       | boolean | true    | Enables or disables Cropination.           |
| cropination.harvest_seeds | boolean | true    | Drops seeds during auto-harvest when true. |

### Cultivation (`minersadvantage-server-config.toml`)

| Option                         | Type    | Default | Range   | Description                                   |
| ------------------------------ | ------- | ------- | ------- | --------------------------------------------- |
| cultivation.enabled            | boolean | true    | -       | Enables or disables Cultivation.              |
| cultivation.hydration_distance | int     | 4       | 1 to 16 | Water-distance check used by hydration logic. |

### Excavation (`minersadvantage-server-config.toml`)

| Option                           | Type         | Default | Range                               | Description                                              |
| -------------------------------- | ------------ | ------- | ----------------------------------- | -------------------------------------------------------- |
| excavation.enabled               | boolean      | true    | -                                   | Enables or disables Excavation.                          |
| excavation.width                 | int          | 3       | 1 to 127                            | Excavation width.                                        |
| excavation.height                | int          | 3       | 1 to 127                            | Excavation height.                                       |
| excavation.depth                 | int          | 3       | 1 to 64                             | Excavation depth.                                        |
| excavation.processes_per_tick    | int          | 10      | input: 1 to 512, effective: 1 to 64 | Processing throughput per tick.                          |
| excavation.toggle_mode           | boolean      | false   | -                                   | Uses toggle behavior instead of hold behavior when true. |
| excavation.ignore_block_variants | boolean      | false   | -                                   | Treats block variants as equivalent for match logic.     |
| excavation.is_block_whitelist    | boolean      | false   | -                                   | Treats `block_blacklist` as a whitelist when true.       |
| excavation.block_blacklist       | list<string> | []      | -                                   | Block IDs used for blacklist/whitelist filtering.        |

### Pathanation (`minersadvantage-server-config.toml`)

| Option                  | Type    | Default | Range   | Description                        |
| ----------------------- | ------- | ------- | ------- | ---------------------------------- |
| pathanation.enabled     | boolean | true    | -       | Enables or disables Pathanation.   |
| pathanation.path_length | int     | 6       | 1 to 64 | Length of generated path segments. |
| pathanation.path_width  | int     | 3       | 1 to 9  | Width of generated path segments.  |

### Illumination (`minersadvantage-server-config.toml`)

| Option                          | Type    | Default | Range   | Description                                    |
| ------------------------------- | ------- | ------- | ------- | ---------------------------------------------- |
| illumination.enabled            | boolean | true    | -       | Enables or disables Illumination.              |
| illumination.radius_horizontal  | int     | 8       | 1 to 64 | Horizontal search radius for placement checks. |
| illumination.radius_vertical    | int     | 4       | 1 to 64 | Vertical search radius for placement checks.   |
| illumination.lowest_light_level | int     | 1       | 0 to 15 | Placement threshold for low-light detection.   |
| illumination.use_block_light    | boolean | true    | -       | Uses block-light evaluation path when true.    |

### Lumbination (`minersadvantage-server-config.toml`)

| Option                                  | Type         | Default | Range                               | Description                                                   |
| --------------------------------------- | ------------ | ------- | ----------------------------------- | ------------------------------------------------------------- |
| lumbination.enabled                     | boolean      | true    | -                                   | Enables or disables Lumbination.                              |
| lumbination.max_trunk_range             | int          | 32      | 1 to 128                            | Maximum trunk search range.                                   |
| lumbination.max_leaf_range              | int          | 6       | 1 to 32                             | Maximum leaf search range.                                    |
| lumbination.processes_per_tick          | int          | 8       | input: 1 to 512, effective: 1 to 64 | Processing throughput per tick.                               |
| lumbination.chop_tree_below             | boolean      | true    | -                                   | Continues chopping blocks below starting point when true.     |
| lumbination.destroy_leaves              | boolean      | true    | -                                   | Removes leaves during tree processing when true.              |
| lumbination.leaves_affect_durability    | boolean      | false   | -                                   | Applies tool durability costs for leaves when true.           |
| lumbination.replant_saplings            | boolean      | true    | -                                   | Attempts sapling replanting where possible.                   |
| lumbination.use_canopy_tool             | boolean      | true    | -                                   | Uses canopy detection/handling logic for broader tree shapes. |
| lumbination.ignore_player_placed_leaves | boolean      | true    | -                                   | Ignores player-placed leaves when identifying natural trees.  |
| lumbination.logs                        | list<string> | []      | -                                   | Additional trunk/log block IDs.                               |
| lumbination.leaves                      | list<string> | []      | -                                   | Additional leaf block IDs.                                    |
| lumbination.axes                        | list<string> | []      | -                                   | Allowed tools list for lumbination behavior.                  |

### Shaftanation (`minersadvantage-server-config.toml`)

| Option                          | Type    | Default | Range                               | Description                                           |
| ------------------------------- | ------- | ------- | ----------------------------------- | ----------------------------------------------------- |
| shaftanation.enabled            | boolean | true    | -                                   | Enables or disables Shaftanation.                     |
| shaftanation.depth              | int     | 16      | 1 to 256                            | Shaft length/depth.                                   |
| shaftanation.processes_per_tick | int     | 10      | input: 1 to 512, effective: 1 to 64 | Processing throughput per tick.                       |
| shaftanation.width              | int     | 1       | 1 to 7                              | Shaft width.                                          |
| shaftanation.height             | int     | 2       | 1 to 5                              | Shaft height.                                         |
| shaftanation.torch_placement    | enum    | FLOOR   | enum value                          | Torch placement mode (`FLOOR` and other enum values). |

### Substitution (`minersadvantage-server-config.toml`)

| Option                             | Type         | Default | Description                                                 |
| ---------------------------------- | ------------ | ------- | ----------------------------------------------------------- |
| substitution.enabled               | boolean      | true    | Enables or disables Substitution.                           |
| substitution.allow_mending         | boolean      | false   | Allows mending tools in candidate selection when true.      |
| substitution.prioritize_silk_touch | boolean      | false   | Prioritizes silk-touch style outcomes when true.            |
| substitution.switch_back           | boolean      | true    | Switches back to previous slot after action when true.      |
| substitution.favour_fortune        | boolean      | true    | Prefers fortune outcomes where appropriate.                 |
| substitution.ignore_if_valid_tool  | boolean      | true    | Skips substitution if currently held tool is already valid. |
| substitution.ignore_passive_mobs   | boolean      | true    | Prevents passive-mob attack swapping behavior when true.    |
| substitution.blacklist             | list<string> | []      | Item IDs excluded from substitution consideration.          |

### Veination (`minersadvantage-server-config.toml`)

| Option                                  | Type         | Default | Range        | Description                                      |
| --------------------------------------- | ------------ | ------- | ------------ | ------------------------------------------------ |
| veination.enabled                       | boolean      | true    | -            | Enables or disables Veination.                   |
| veination.max_vein_distance             | int          | 12      | 1 to 64      | Maximum connected vein distance.                 |
| veination.ores                          | list<string> | []      | -            | Explicit ore block allowlist/targets.            |
| veination.ore_harvest_without_sneak     | boolean      | true    | -            | Allows ore harvesting without sneak requirement. |
| veination.drop_ores_at_first_block      | boolean      | true    | -            | Drops output near the first broken ore block.    |
| veination.increase_harvest_time_per_ore | boolean      | true    | -            | Scales mining time with vein size when true.     |
| veination.harvest_time_modifier         | double       | 0.2     | 0.01 to 10.0 | Modifier applied to scaled harvest time.         |
| veination.pickaxe_blacklist             | list<string> | []      | -            | Disallowed pickaxes for veination behavior.      |

### Ventilation (`minersadvantage-server-config.toml`)

| Option                         | Type    | Default | Range                               | Description                                      |
| ------------------------------ | ------- | ------- | ----------------------------------- | ------------------------------------------------ |
| ventilation.enabled            | boolean | true    | -                                   | Enables or disables Ventilation.                 |
| ventilation.width              | int     | 1       | 1 to 32                             | Vent width.                                      |
| ventilation.height             | int     | 16      | 1 to 64                             | Vent height envelope.                            |
| ventilation.depth              | int     | 1       | 1 to 64                             | Vent depth/length.                               |
| ventilation.processes_per_tick | int     | 8       | input: 1 to 512, effective: 1 to 64 | Processing throughput per tick.                  |
| ventilation.place_ladders      | boolean | true    | -                                   | Places ladders during vent carving when enabled. |

### Advanced Substitution Rule Profile (Runtime Data)

Substitution includes an advanced `selectionRules` profile in runtime config defaults. It is action-aware and expression-aware, and each rule can include the fields below.

| Field            | Type    | Description                                                              |
| ---------------- | ------- | ------------------------------------------------------------------------ |
| action           | enum    | `BREAK`, `INTERACT`, `ATTACK`, `STAT_CHANGE`, or `ANY`.                  |
| targetKind       | enum    | `BLOCK_TAG`, `ENTITY_TYPE`, or `ANY`.                                    |
| targetId         | string  | Tag/entity identifier for the selected target kind.                      |
| requiredToolKind | string  | Tool family hint (for example `pickaxe`, `axe`, `shovel`, `hoe`).        |
| targetPriority   | int     | Priority for choosing among target matches.                              |
| toolPriority     | int     | Priority for choosing among tool candidates.                             |
| preferSilkTouch  | boolean | Prefer silk-touch style outcomes for the rule.                           |
| preferFortune    | boolean | Prefer fortune style outcomes for the rule.                              |
| targetExpression | string  | Optional target expression with `AND`/`OR`/`NOT` and parentheses.        |
| toolExpression   | string  | Optional tool expression with `AND`/`OR`/`NOT` and parentheses.          |
| minSilkTouch     | int     | Minimum Silk Touch level required.                                       |
| minFortune       | int     | Minimum Fortune level required.                                          |
| requireMending   | boolean | Requires mending on selected tool when true.                             |
| denyMending      | boolean | Denies mending tools when true (`requireMending` wins if both are true). |

## Compatibility

| Topic              | Status                                                                      |
| ------------------ | --------------------------------------------------------------------------- |
| Minecraft versions | `1.21.11`, `26.1.2`, and `26.2` nodes are maintained.                       |
| Loaders            | Fabric and NeoForge are both first-class targets.                           |
| Known conflicts    | No hard conflicts are currently documented. If you find one, open an issue. |

## Technical Documentation

For full technical details, see [TECHNICAL.md](TECHNICAL.md).

## License

MIT - see [LICENSE.md](LICENSE.md) for the full text.

## Credits

- DuelMonster for the original mod and modern rewrite.
- Stonecutter, Modstitch, Fabric, NeoForge, Cloth Config, and ModMenu maintainers.