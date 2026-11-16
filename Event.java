package lspu_hnn_core;


/** One historical event. The year is the BST key, so years are unique. */
public record Event(int year, String title) {
}
