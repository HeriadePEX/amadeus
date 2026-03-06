package com.kalixys.amadeus.api.soap;

public record PaginationMetadata(
    int page,
    int size,
    int returnedCount,
    boolean hasNext,
    String nextCursor
) {
}
