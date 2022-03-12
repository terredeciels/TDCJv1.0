package base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.range;

public class ABoard implements Constants {
    public int[] color = new int[64];
    public int[] piece = new int[64];
    public int side;
    public int xside;
    public int castle;
    public int ep;
    public List<Move> pseudomoves = new ArrayList<>();
    public int halfMoveClock;
    public int plyNumber;
    protected int fifty;
    protected UndoMove um = new UndoMove();

    IntPredicate pion = _case -> piece[_case] == PAWN;
    //IntPredicate Side = _case -> color[_case] == side;
    IntPredicate SideBlanc = _case -> side == BLANC;
    IntStream CASES = range(0, 64);

    IntPredicate side() {
        return c -> color[c] == side;
    }
}
