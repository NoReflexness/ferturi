# Ferturi

Android app for figuring out how to mix a fertilizer container so a venturi
injector hits the recommended dilution in your main water line.

You give it:

- The volume of the **mix container** the venturi pulls from.
- One or more **calibration** measurements: at a given valve setting (0 – 5),
  how much fluid was drawn from the container vs. how much main-line output
  came out the other end.
- A **product** with its vendor-recommended dilution (mL of raw fertilizer per
  litre of water).

It tells you:

- How much **raw fertilizer** to put into the container, and how much water
  to top it up with.
- The resulting **container concentration** and venturi **draw ratio**.
- If the preferred valve setting can't reach the target, an alternative valve
  setting (interpolated from your other calibrations).
- If you've timed how long it takes to fill 10 L: an estimate of the
  main-line flow rate and how long the container will last.

The app uses **Material 3** with **automatic light/dark theme** detection
and Material You **dynamic colours** on Android 12+.

## The math (short version)

For a calibration measurement at valve V we know

```
draw_ratio(V) = mix_drawn_L / output_L
```

The venturi blends fluid from the container into the main line, so the
final concentration in the main-line water is

```
final = container_concentration × draw_ratio(V)
```

We want `final` to equal the product's recommended ratio `r`. Solving for
the container concentration:

```
C = r / draw_ratio(V)
```

The recipe for a container of volume `V_c`:

```
raw   = C × V_c
water = V_c − raw
```

Feasible when `0 < C ≤ 1`. If your preferred valve setting gives `C > 1`
(impossible — would need more than a container of pure raw fertilizer)
or some other awkward number, the app suggests another valve setting using
linear interpolation between your existing calibration rows.

## Building locally

You need JDK 17 and the Android SDK.

```bash
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Building via GitHub Actions

Every push to `main` (and every PR) builds debug and unsigned-release APKs
and uploads them as workflow artifacts. Tagging a commit `vX.Y.Z` also
attaches the APKs to a GitHub Release.

To grab the latest APK:

1. Open the **Actions** tab on GitHub.
2. Click the most recent successful **Build APK** run.
3. Download the `ferturi-debug-apk` artifact.

To install: enable "Install unknown apps" for your file manager, then tap
the APK.

## Project layout

- `app/src/main/java/com/noreflexness/ferturi/`
  - `domain/MixCalculator.kt` — pure-Kotlin calculation core.
  - `data/` — models and DataStore-backed persistence.
  - `ui/` — Compose UI, theme, and view-model.
- `.github/workflows/build.yml` — CI that builds the APK.
