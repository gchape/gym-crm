package tech.provokedynamic.gymcrm.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/** Stable API contract, independent of Spring Data's Page serialization. */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
    public static <T> PageResponse<T> from(Page<T> result) {
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }
}
