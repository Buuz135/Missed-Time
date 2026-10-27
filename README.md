# MissedTime

Ever close a world while waiting for crops to grow, furnaces to finish, or a farm to produce something? MissedTime lets
you bring a little of that lost time back when you return.

When you leave a singleplayer world, MissedTime records the real-world time. The next time you join, it works out how
long you were away and asks whether you want to recover it. If you accept, that offline time is converted into extra
world ticks and played back as a short, accelerated simulation.

The simulation begins smoothly, ramps up to the configured speed, and keeps ticking the loaded parts of every dimension
until the recovered time is used up. Crops, furnaces, block entities, mobs, redstone, scheduled ticks, and compatible
mod events all get a chance to run. MissedTime does not force-load chunks, so anything outside the currently loaded area
stays untouched.

## Staying in control

While recovery is running, the action-bar display shows the progress, remaining ticks, current multiplier, tick rate,
and estimated time left. You can adjust the simulation without opening a menu:

- `P` pauses or resumes recovery. The world is frozen while paused.
- `+` increases the speed multiplier.
- `-` decreases the speed multiplier. The lowest setting is 0x.
- `End` stops recovery and discards the remaining time.

Changing speed also plays a note-block sound whose pitch matches the selected multiplier, which makes it easy to tell
where you are without watching the display.

## Protected recovery mode

By default, MissedTime places you in a protected recovery state while the world catches up. You are invulnerable, able
to fly without passing through blocks, and kept within 32 blocks of where recovery began. This gives you a safe way to
watch the simulation without wandering away from the loaded area.

If you would rather keep normal control of your player, set `recoveryParticipantEnabled=false` in the configuration.

## Configuration

MissedTime creates `config/missedtime.properties` with these options:

- `acceleratedTicksPerOfflineMinute` controls how many ticks are recovered for each real minute away.
- `acceleratedTicksPerSecond` sets the base playback speed. The in-game speed controls multiply this value.
- `minimumOfflineMinutes` sets how long you must be away before the recovery prompt appears.
- `maximumOfflineHours` limits how much offline time can be collected at once.
- `rampUpSeconds` controls how long the simulation takes to reach its selected speed.
- `spectatorRadius` sets how far the protected participant can travel from the starting point.
- `recoveryParticipantEnabled` enables or disables the protected recovery state and is `true` by default.

## Testing a simulation

Players with operator permissions can start a recovery manually with:

```text
/missedtime simulate <minutes>
```

This treats the supplied value as the number of minutes the world was offline, making it useful for testing settings
without closing the game.

The mod is intentionally singleplayer-only. It does not run recovery or expose its commands on dedicated servers.
