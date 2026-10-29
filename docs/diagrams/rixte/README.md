<!-- @@author Rixte -->
# Rixte's feature diagrams

These ten diagrams support the [Developer Guide](../../DeveloperGuide.md).
Each feature README records its source files and regeneration commands.

| Feature | Diagrams | Main purpose |
| --- | --- | --- |
| [Confirmation](confirm-plan/README.md) | 1 sequence, 1 activity | Ask before executing a plan change; follow reset confirmation and cancellation decisions. |
| [Reset](reset-plan/README.md) | 1 sequence, 1 state machine | Clear plan-specific state after agreement and explain plan/grocery lifecycle states. |
| [Recipe editing](edit-recipe/README.md) | 1 class, 1 sequence | Retain validated edits and format before replacement. |
| [Shortfall checking](shortfall/README.md) | 1 class, 1 sequence | Read current planned days and compare explicit targets. |
| [Plan viewing](view-plan/README.md) | 1 sequence | Read the latest meals and targets. |
| [Grocery viewing](view-list/README.md) | 1 sequence | Distinguish absent groceries from an empty generated list. |

## Course notation and review

Use the course's [modeling chapter](https://nus-cs2113-ay2627-s1.github.io/website/se-book-adapted/chapters/modeling.html)
and the [Week 11 DG bug criteria](https://nus-cs2113-ay2627-s1.github.io/website/schedule/week11/tutorial.html).
The chapter's embedded lessons cover class associations, multiplicities, dependencies,
sequence calls, activations, self-invocation, and `alt`, `opt`, and `loop` frames.

- Include `shortfall/readable.iuml` using the appropriate relative path. Its `hide circle`
  removes C/I spots, and `skinparam style strictuml` selects conventional arrowheads.
- Draw synchronous Java calls with solid lines and filled heads, and returns with dashed
  lines. An activation starts at its call and ends at its return, remaining unbroken.
  Alternative branches must not leave an activation running after an early return.
- Label sequence participants as `object:Class` or `:Class`, without underlining. Calls to
  static methods target a class participant; the sequence diagrams expand instance calls.
- Use solid navigable associations for stored references and dashed dependencies for
  temporary use. Put multiplicities and roles at the appropriate association end; do not
  repeat the same reference as an attribute or add a redundant dependency arrow.
- Keep guards, loops, and shown call order consistent with the code. Unroll compound calls
  when those operations are expanded; explain omitted library calls and construction in captions.
- Use folded-corner notes for condensed details, keeping notes clear of activation bars.
  Inspect arrow labels, role names, and multiplicities for collisions after rendering.
- State each diagram's purpose beside it. Keep validation and secondary failures in prose
  or tables when another diagram would repeat the same workflow. Check text at normal guide
  width, not just in an enlarged diagram preview.
- Use guarded decision/merge nodes for the reset activity; label every decision exit.
  Use named stable states and event/guard transitions for the lifecycle. Explain omitted
  status-preserving transitions in prose rather than adding repetitive self-loops.

After an edit, use Java 25 to syntax-check and regenerate each affected SVG. Verify methods,
fields, guards, and order against the Java implementation; a successful render alone cannot
establish that the diagram is accurate. Check source links and visually inspect the result.
<!-- @@author -->
