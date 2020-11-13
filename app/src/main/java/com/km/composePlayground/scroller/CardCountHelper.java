package com.km.composePlayground.scroller;


import androidx.annotation.FloatRange;

/**
 * A helper class to calculate the card count and card width of a fixed-by-grid cluster based on
 * desired card width.
 *
 * <p>Any changes made to this class must be applied to go/phonesky-card-count-calculator.
 */
public final class CardCountHelper {

    private static final int MIN_UNIT_CARD_COUNT = 3;

    private CardCountHelper() {}

    /**
     * Returns the number of cards to display in a cluster based on desired card width. The result is
     * at least 3 in order to display 3x cards.
     *
     * <p>The formula is:
     *
     * <p>max(3, round(widthForChildren/desiredUnitCardWidth - peekingAmount)) + peekingAmount
     *
     * @param desiredUnitCardWidth the desired width of a 1x card.
     * @param widthForChildren the width in the cluster to display the children. For example, cluster
     *     padding should be subtracted from this value.
     * @param peekingAmount the peeking amount of the cluster. This value will be strictly enforced.
     * @return the number of cards with the peeking amount.
     */
    public static float getCardCount(
            int desiredUnitCardWidth,
            int widthForChildren,
            @FloatRange(from = 0f, to = 1f) float peekingAmount) {
        float countForDesireWidthCard = ((float) widthForChildren) / desiredUnitCardWidth;
        int wholeCount = Math.round(countForDesireWidthCard - peekingAmount);
        return Math.max(wholeCount, MIN_UNIT_CARD_COUNT) + peekingAmount;
    }

    /**
     * Returns the actual width of a 1x card to display in a cluster based on desired card width.
     *
     * @param desiredUnitCardWidth the desired width of a 1x card.
     * @param widthForChildren the width in the cluster to display the children. For example, cluster
     *     padding should be subtracted from this value.
     * @param peekingAmount the peeking amount of the cluster. This value will be strictly enforced.
     * @return the width of a 1x card.
     */
    public static int getUnitCardWidth(
            int desiredUnitCardWidth,
            int widthForChildren,
            @FloatRange(from = 0f, to = 1f) float peekingAmount) {
        return Math.round(
                widthForChildren / getCardCount(desiredUnitCardWidth, widthForChildren, peekingAmount));
    }

    /**
     * Returns the number of cards to display for the case where there is no peeking card at the end.
     * In this case we have to deal with the possibility of having padding between the cards, but we
     * forbid padding before/after the cards | | | | | card | padding | card | padding | card |
     * padding | card | | | | | | |--------------------widthForChildren-----------------------| | The
     * output is the result of solving for N in the equation: N*minimumCardWidth + (N-1)*padding <
     * widthForChildren where N is the number of cards to show.
     *
     * @param minWidthPerCard the minimum width of each card
     * @param widthForChildren amount of available width to put the cards in
     * @param padding between the cards
     * @return largest number of cards that can fit in the available space
     */
    public static int getMaxCardCountForMinWidthPerCard(
            int minWidthPerCard, int widthForChildren, int padding) {
        return Math.max((widthForChildren + padding) / (minWidthPerCard + padding), 1);
    }

    /**
     * Returns the number of cards to display for the case where there is no peeking card at the end.
     * In this case we have to deal with the possibility of having padding between the cards. In the
     * edge case where no cards fit, just return the initial value.
     *
     * @param count of cards to display
     * @param widthForChildren amount of available width to put the cards in
     * @param padding between the cards
     * @return width the cards should take to fill the space
     */
    public static int getCardWidthFromMinimumWidth(int count, int widthForChildren, int padding) {
        return count == 0 ? widthForChildren : (widthForChildren - (count - 1) * padding) / count;
    }
}

