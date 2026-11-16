package lspu_hnn_core;


/**
 * One animation step for a tree operation. The GUI plays a list of these one by one.
 * key is the year of the node to highlight (or -1 when the step has no node).
 */
public record Step(Kind kind, int key, String message) {

    public enum Kind {
        /** Node is being looked at (yellow). */
        VISIT,
        /** Node was found or matched (green). */
        FOUND,
        /** Search ended without a match. */
        NOT_FOUND,
        /** Node that will be removed (red). */
        TARGET,
        /** In-order successor used in a two-children delete (orange). */
        SUCCESSOR,
        /** Text only, no node. */
        INFO
    }
}

