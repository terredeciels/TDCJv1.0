package base;

import java.util.function.IntPredicate;

import static base.Constants.PION;

public class ABoard {
    public int[] color = new int[64];
    public int[] piece = new int[64];
    public int side, xside;
    public int castle, ep;
    protected boolean Pion(int i) {
        return piece[i] == PION;
    }
    protected IntPredicate pieceAJouer() {
        return i -> color[i] == side;
    }

    protected int caseSuiv(int case_cour, int _case, int j) {
        return Constants.mailbox[Constants.mailbox64[_case] + Constants.offset[piece[case_cour]][j]];
    }

    protected boolean caseInBoard(int _case) {
        return _case != -1;
    }

    protected boolean couleurOpposer(int _case) {
        return color[_case] == xside;
    }

    protected boolean notSlide(int case_cour) {
        return !Constants.slide[piece[case_cour]];
    }

    protected boolean caseOccuper(int _case) {
        return color[_case] != Constants.VIDE;
    }
}
