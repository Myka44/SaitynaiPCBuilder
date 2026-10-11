package lt.komplektatorius.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageRequestFactoryTest {
    @Test
    void appliesDefaultSort() {
        Pageable result = PageRequestFactory.normalize(PageRequest.of(0, 20), Set.of("updatedAt"));

        assertThat(result.getSort().getOrderFor("updatedAt")).isNotNull();
        assertThat(result.getSort().getOrderFor("updatedAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void rejectsPageSizeAboveOneHundred() {
        assertThatThrownBy(() -> PageRequestFactory.normalize(PageRequest.of(0, 101), Set.of("name")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsUnknownSortField() {
        Pageable input = PageRequest.of(0, 20, Sort.by("passwordHash"));

        assertThatThrownBy(() -> PageRequestFactory.normalize(input, Set.of("name")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
