package ch.holygames;

public record OrderedInputRow(long order, InputRow inputRow) {

    public OrderedInputRow withOrder(long newOrder) {
        return new OrderedInputRow(newOrder, inputRow);
    }
}
