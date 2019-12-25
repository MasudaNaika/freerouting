package autoroute;

import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;

/**
 *
 * @author masuda, Masuda Naika
 */
public class MazeListElementSet extends ObjectAVLTreeSet<MazeListElement> {
    
    private int id = Integer.MIN_VALUE;

    @Override
    public boolean add(MazeListElement k) {
        k.id = id++;
        return super.add(k);
    }

}
