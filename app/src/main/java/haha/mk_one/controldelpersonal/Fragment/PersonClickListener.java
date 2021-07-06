package haha.mk_one.controldelpersonal.Fragment;

/**
 * Listener for person click events in the grid of persons
 *
 * @author bherbst
 */
public interface PersonClickListener {
    /**
     * Called when a person is clicked
     * @param holder The ViewHolder for the clicked person
     * @param position The position in the grid of the person that was clicked
     */
    void onPersonClicked(PersonViewHolder holder, int position);
}
