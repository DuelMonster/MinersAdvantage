# 📝 Humanized Comment Style Guide

This guide enforces the comedic, humanized comment style across Forget-Me-Crops. Every class, method, logic block, and significant line must have comments in this style.

## Style Requirements

### Tone
- **Humanized & conversational** - Write like you're talking to a friend, not a documentation generator
- **Light comedic flair** - Witty, playful, mildly sarcastic when appropriate
- **Explain WHY** - Not just what the code does, but why it exists and why that matters
- **Professional clarity** - Still clear, precise, and technically accurate

### Coverage
- Every class (class-level Javadoc)
- Every public/package-private method
- Every major logic block (if/else, loops, complex operations)
- Every significant line of code that isn't immediately obvious
- All existing comments must be rewritten to match this style

## Examples

### Class Comments

#### ❌ Before (Formal)
```java
/**
 * Manages the gradual processing of discovered farm anchors.
 */
public final class CatchupManager {
```

#### ✅ After (Humanized)
```java
/**
 * 🧑‍🌾 CatchupManager: The patient farm auditor who refuses to overwhelm your server!
 * <p>
 * When chunks load and suddenly you've got 500 item frames to validate, CatchupManager
 * is the hero who says "hey, let's not incinerate the TPS in one tick." Queues them up
 * gradual-like and processes them spread across multiple ticks. Your server's stability
 * appreciates this thoughtful behavior. So do your players.
 * </p>
 */
public final class CatchupManager {
```

### Method Comments

#### ❌ Before (Formal)
```java
/**
 * Enqueues a list of vanilla ItemFrame positions.
 * @param level the server level
 * @param dimId dimension identifier
 * @param positions list of positions to enqueue
 */
public static void enqueueVanillaPositions(ServerLevel level, String dimId, List<BlockPos> positions) {
```

#### ✅ After (Humanized)
```java
/**
 * Queue up a batch of vanilla item frames for lazy processing.
 * <p>
 * "Lazy" here means we're not validating them all RIGHT NOW — we're queuing them and
 * spreading the work across many ticks. Chunk loads are grateful for this restraint.
 * </p>
 * @param level the server level (used implicitly via dimId for future lookups)
 * @param dimId which dimension these frames live in
 * @param positions list of frame positions to validate eventually (not immediately, thank goodness)
 */
public static void enqueueVanillaPositions(ServerLevel level, String dimId, List<BlockPos> positions) {
```

### Logic Block Comments

#### ❌ Before (Formal)
```java
// Check if position is valid
if (position != null) {
    // Register frame
    registerFrame(position);
}
```

#### ✅ After (Humanized)
```java
// Make sure we actually have a position to work with (you'd be surprised how often null shows up uninvited)
if (position != null) {
    // Lock it in: this frame is now officially part of our registry
    registerFrame(position);
}
```

### Significant Line Comments

#### ❌ Before (None)
```java
int CATCHUP_TICKS = 40;
```

#### ✅ After (Humanized)
```java
// Spread the initial chunk-load discovery work across 40 ticks to avoid a brutal TPS spike
// (40 ticks ≈ 2 seconds of graceful, distributed validation instead of one horrible spike)
int CATCHUP_TICKS = 40;
```

## Special Cases

### Constants
```java
// How many ticks between farm scans. 300 ticks ≈ 15 seconds — frequent enough to feel alive,
// slow enough not to reduce the server to a smoking crater. Balance is everything.
private static final int TICK_INTERVAL_DEFAULT = 300;
```

### Flags
```java
// Have we already logged the startup snapshot? We only want to announce ourselves once
// (being verbose on every tick is how you end up with log files measured in gigabytes)
private static boolean tickSnapshotLogged = false;
```

### Complex Conditions
```java
// If the chest is full AND we're running low on patience (exceeded retry cooldown),
// it's time to give up and try again next cycle. The chest will drain eventually.
if (ctx.isChestFull() && tickCounter >= Config.getChestFullCooldownTicks()) {
    return; // Abort this scan, let other crops get a turn
}
```

## Consistency Checklist

Before committing, verify:
- [ ] Every class has a humanized Javadoc header
- [ ] Every public/package method has a comment explaining its purpose
- [ ] Major logic blocks (if/else, loops) have inline comments
- [ ] "Magic numbers" and constants have explanatory comments
- [ ] Comments explain WHY, not just WHAT
- [ ] Tone is conversational, witty, and human-sounding
- [ ] All comments are technically accurate
- [ ] No old-style formal comments remain

## Going Forward

Apply this style to all new code. For existing comments that don't match this style, rewrite them during any refactoring or cleanup pass. Treat this as part of code quality, not optional.
