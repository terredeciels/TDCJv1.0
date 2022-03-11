package base;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.IntStream.range;

public class Board extends ABoard implements Constants {

    public List<Move> pseudomoves = new ArrayList<>();
    public int halfMoveClock, plyNumber, fifty;
    private UndoMove um = new UndoMove();

    public Board() {
    }

    public Board(Board board) {
        color = board.color;
        piece = board.piece;
        side = board.side;
        xside = board.xside;
        castle = board.castle;
        ep = board.ep;
        fifty = board.fifty;
        pseudomoves = new ArrayList<>();
        um = new UndoMove();

        plyNumber = board.plyNumber;
        halfMoveClock = board.halfMoveClock;

    }

    public void gen() {
        IntStream Cases = range(0, 64);
        Cases.filter(pieceAJouer()).forEach(_case -> {
            if (Pion(_case)) pion(_case);
            else piece(_case);
        });
        roque();
        ep();
    }


    private void piece(int case_cour) {
        int _case, pas;
        int offs = offsets[piece[case_cour]];
        for (pas = 0; pas < offs; ++pas) {
            _case = case_cour;
            while (true) {
                _case = caseSuiv(case_cour, _case, pas);
                if (caseInBoard(_case)) {
                    if (caseOccuper(_case)) {
                        if (couleurOpposer(_case)) gen_push(case_cour, _case, 1);
                        break;
                    }
                    gen_push(case_cour, _case, 0);
                    if (notSlide(case_cour)) break;
                } else break;
            }
        }
    }

    private void ep() {
        /* generate en passant moves */
        if (caseInBoard(ep)) if (side == BLANC) {
            if ((ep & 7) != 0 && color[ep + 7] == BLANC && piece[ep + 7] == PION) gen_push(ep + 7, ep, 21);
            if ((ep & 7) != 7 && color[ep + 9] == BLANC && piece[ep + 9] == PION) gen_push(ep + 9, ep, 21);
        } else {
            if ((ep & 7) != 0 && color[ep - 9] == NOIR && piece[ep - 9] == PION) gen_push(ep - 9, ep, 21);
            if ((ep & 7) != 7 && color[ep - 7] == NOIR && piece[ep - 7] == PION) gen_push(ep - 7, ep, 21);
        }
    }

    private void roque() {
        /* generate castle moves */
        if (side == BLANC) {
            if ((castle & 1) != 0) gen_push(E1, G1, 2);
            if ((castle & 2) != 0) gen_push(E1, C1, 2);
        } else {
            if ((castle & 4) != 0) gen_push(E8, G8, 2);
            if ((castle & 8) != 0) gen_push(E8, C8, 2);
        }
    }

    private void pion(int i) {
        if (side == BLANC) {
            if ((i & 7) != 0 && color[i - 9] == NOIR) gen_push(i, i - 9, 17);
            if ((i & 7) != 7 && color[i - 7] == NOIR) gen_push(i, i - 7, 17);
            if (color[i - 8] == VIDE) {
                gen_push(i, i - 8, 16);
                if (i >= 48 && color[i - 16] == VIDE) gen_push(i, i - 16, 24);
            }
        } else {
            if ((i & 7) != 7 && color[i + 9] == BLANC) gen_push(i, i + 9, 17);
            if ((i & 7) != 0 && color[i + 7] == BLANC) gen_push(i, i + 7, 17);
            if (color[i + 8] == VIDE) {
                gen_push(i, i + 8, 16);
                if (i <= 15 && color[i + 16] == VIDE) gen_push(i, i + 16, 24);
            }
        }
    }

    private boolean in_check(int s) {
        for (int i = 0; i < 64; ++i)
            if (piece[i] == KING && color[i] == s) return attack(i, s ^ 1);
        return true; // shouldn't get here
    }

    private boolean attack(int sq, int s) {
        for (int i = 0; i < 64; ++i)
            if (color[i] == s) if (piece[i] == PION) {
                if (s == BLANC) {
                    if ((i & 7) != 0 && i - 9 == sq || (i & 7) != 7 && i - 7 == sq) return true;
                } else if ((i & 7) != 0 && i + 7 == sq || (i & 7) != 7 && i + 9 == sq) return true;
            } else for (int j = 0; j < offsets[piece[i]]; ++j)
                for (int n = i; ; ) {
                    n = mailbox[mailbox64[n] + offset[piece[i]][j]];
                    if (n == -1) break;
                    if (n == sq) return true;
                    if (color[n] != VIDE || notSlide(i)) break;
                }
        return false;
    }

    private void gen_push(int from, int to, int bits) {
        if ((bits & 16) != 0) if (side == BLANC) {
            if (to <= H8) {
                gen_promote(from, to, bits);
                return;
            }
        } else if (to >= A1) {
            gen_promote(from, to, bits);
            return;
        }
        pseudomoves.add(new Move((byte) from, (byte) to, (byte) 0, (byte) bits));

    }

    private void gen_promote(int from, int to, int bits) {
        for (int i = KNIGHT; i <= QUEEN; ++i)
            pseudomoves.add(new Move((byte) from, (byte) to, (byte) i, (byte) (bits | 32)));
    }

    public boolean makemove(Move m) {
        if ((m.bits & 2) != 0) {
            int from;
            int to;

            if (in_check(side)) return false;
            switch (m.to) {
                case 62 -> {
                    if (color[F1] != VIDE || color[G1] != VIDE || attack(F1, xside) || attack(G1, xside))
                        return false;
                    from = H1;
                    to = F1;
                }
                case 58 -> {
                    if (color[B1] != VIDE || color[C1] != VIDE || color[D1] != VIDE || attack(C1, xside) || attack(D1, xside))
                        return false;
                    from = A1;
                    to = D1;
                }
                case 6 -> {
                    if (color[F8] != VIDE || color[G8] != VIDE || attack(F8, xside) || attack(G8, xside))
                        return false;
                    from = H8;
                    to = F8;
                }
                case 2 -> {
                    if (color[B8] != VIDE || color[C8] != VIDE || color[D8] != VIDE || attack(C8, xside) || attack(D8, xside))
                        return false;
                    from = A8;
                    to = D8;
                }
                default -> { // shouldn't get here
                    from = -1;
                    to = -1;
                }
            }
            color[to] = color[from];
            piece[to] = piece[from];
            color[from] = VIDE;
            piece[from] = VIDE;
        }

        /* back up information, so we can take the move back later. */
        um.mov = m;
        um.capture = piece[m.to];
        um.castle = castle;
        um.ep = ep;
        um.fifty = fifty;

        castle &= castle_mask[m.from] & castle_mask[m.to];

        if ((m.bits & 8) != 0) {
            if (side == BLANC) ep = m.to + 8;
            else ep = m.to - 8;
        } else ep = -1;
        if ((m.bits & 17) != 0) fifty = 0;
        else ++fifty;

        /* move the piece */
        color[m.to] = side;
        if ((m.bits & 32) != 0) piece[m.to] = m.promote;
        else piece[m.to] = piece[m.from];
        color[m.from] = VIDE;
        piece[m.from] = VIDE;

        /* erase the pawn if this is an en passant move */
        if ((m.bits & 4) != 0) if (side == BLANC) {
            color[m.to + 8] = VIDE;
            piece[m.to + 8] = VIDE;
        } else {
            color[m.to - 8] = VIDE;
            piece[m.to - 8] = VIDE;
        }

        side ^= 1;
        xside ^= 1;
        if (in_check(xside)) {
            takeback();
            return false;
        }

        return true;
    }

    public void takeback() {

        side ^= 1;
        xside ^= 1;

        Move m = um.mov;
        castle = um.castle;
        ep = um.ep;
        fifty = um.fifty;

        color[m.from] = side;
        if ((m.bits & 32) != 0) piece[m.from] = PION;
        else piece[m.from] = piece[m.to];
        if (um.capture == VIDE) {
            color[m.to] = VIDE;
            piece[m.to] = VIDE;
        } else {
            color[m.to] = xside;
            piece[m.to] = um.capture;
        }
        if ((m.bits & 2) != 0) {
            int from;
            int to;

            switch (m.to) {
                case 62 -> {
                    from = F1;
                    to = H1;
                }
                case 58 -> {
                    from = D1;
                    to = A1;
                }
                case 6 -> {
                    from = F8;
                    to = H8;
                }
                case 2 -> {
                    from = D8;
                    to = A8;
                }
                default -> { // shouldn't get here
                    from = -1;
                    to = -1;
                }
            }
            color[to] = side;
            piece[to] = ROOK;
            color[from] = VIDE;
            piece[from] = VIDE;
        }
        if ((m.bits & 4) != 0) {
            if (side == BLANC) {
                color[m.to + 8] = xside;
                piece[m.to + 8] = PION;
            } else {
                color[m.to - 8] = xside;
                piece[m.to - 8] = PION;
            }
        }
    }


}
