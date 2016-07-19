Rail System
===========
The rail system is described in this document.

## 1. Rail
A Rail is the smallest segment of a track. In Factorio, the signals are placed between rails.
We're not so lucky in Minecraft, where it'd be _really_ awkward for us to have half-rails.
Therefore, Signals are placed on Rails, and the attached Rail has two Rail Segments.

## 2. Rail Segments
Rail Segments are the next step up from a Rail. Each Segment is made from one or more Rails, stored in a graph.
The Segments are defined by Signals at their ends. Placing down Signals will divide the Rails
into Segments.

An example:

- Initial track: `11`; Has 1 Segment (`1`).
- Track with Signal: `1S2` Has 2 Segments (`1`, `2`).

Notice that initially a track has only 1 Segment. When a Signal is placed, it **usually** splits.
However, in the case of a loop, it will create an always-blocked Segment. In Factorio, this results
in a segment that doesn't actually block, and the signal cycles its lights.

## 3. Rail Signals
Rail Signals divide sections of Rail into Segments. They have lights which reflect the blocked-state
of the Segment they guard.

## 4. Coding Stuff
When a Rail is placed down, we first check for neighbor Rails. If there are any, we inherit the
Segment of that Rail. If there are multiple Rails, we inherit any one of the Segments, and pass it
to all neighbor Rails that have different Segments. Then we check for Rail Signals, and if there
is any, we perform the same actions as when a Rail Signal is placed.

When a Rail Signal is placed down, we check for nearby Rails, attach to one of them
(this can be changed after placement), then perform a splitting mechanism on the Segment.
This is described in the next section.

### 4.1. Splitting

1. Store each Segment as a graph of Rails.
    - Vertices are Rails, edges are connected Rails.
2. Check for the simple no-signal cases (Segment has no Signals)
    1. If `loopFind` is successful
        - Add the signal to the Segment's Signal list.
    2. Otherwise, if neither `$first` nor `$last` has any neighbor rails besides the one leading into it,
