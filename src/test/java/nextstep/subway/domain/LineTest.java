package nextstep.subway.domain;

import nextstep.subway.exception.InvalidDistanceException;
import nextstep.subway.exception.InvalidSectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineTest {
    private Station A역;
    private Station B역;
    private Station C역;
    private Station D역;
    private Station E역;
    private Station F역;

    @BeforeEach
    void setUp() {
        A역 = new Station(1L, "A역");
        B역 = new Station(2L, "B역");
        C역 = new Station(3L, "C역");
        D역 = new Station(4L, "D역");
        E역 = new Station(5L, "E역");
        F역 = new Station(6L, "F역");
    }

    @Test
    void 노선_생성_테스트() {
        // given
        Line line = new Line("2호선", "초록", 10, A역, B역);

        // when, then
        assertThat(line.getUpStation()).isEqualTo(A역);
        assertThat(line.getDownStation()).isEqualTo(B역);
    }

    @Test
    void 구간_추가_상행역으로() {
        // given
        Line line = new Line("2호선", "초록", 10, A역, C역);

        // when
        line.insertSection(new Section(3, A역, B역));

        // then
        assertThat(line.getUpStation()).isEqualTo(A역);
        assertThat(line.getDownStation()).isEqualTo(C역);
    }

    @Test
    void 구간_추가_하행역으로() {
        // given
        Line line = new Line("2호선", "초록", 10, A역, C역);

        // when
        line.insertSection(new Section(3, B역, C역));

        // then
        assertThat(line.getUpStation()).isEqualTo(A역);
        assertThat(line.getDownStation()).isEqualTo(C역);
    }

    @Test
    void 구간_추가_상행종점역_변경() {
        // given
        Line line = new Line("2호선", "초록", 10, A역, C역);

        // when
        line.insertSection(new Section(3, B역, A역));

        // then
        assertThat(line.getUpStation()).isEqualTo(B역);
        assertThat(line.getDownStation()).isEqualTo(C역);
    }

    @Test
    void 구간_추가_하행종점역_변경() {
        // given
        Line line = new Line("2호선", "초록", 10, A역, C역);

        // when
        line.insertSection(new Section(3, C역, B역));

        // then
        assertThat(line.getUpStation()).isEqualTo(A역);
        assertThat(line.getDownStation()).isEqualTo(B역);
    }

    @Test
    void 다양하게_구간_추가() {
        // given
        Line line = new Line("2호선", "초록", 15, A역, D역);

        // when
        line.insertSection(new Section(4, A역, B역));
        line.insertSection(new Section(3, C역, D역));
        line.insertSection(new Section(15, E역, A역));
        line.insertSection(new Section(30, D역, F역));

        // then
        assertThat(line.getUpStation()).isEqualTo(E역);
        assertThat(line.getDownStation()).isEqualTo(F역);
    }

    @Test
    void 구간_추가_상행역으로_거리오류() {
        // given
        Line line = new Line("2호선", "초록", 10, A역, C역);

        // when, then
        assertThatThrownBy(() -> {
            line.insertSection(new Section(10, A역, B역));
        }).isInstanceOf(InvalidDistanceException.class);
    }

    @Test
    void 구간_추가_하행역으로_거리오류() {
        // given
        Line line = new Line("2호선", "초록", 10, A역, C역);

        // when
        assertThatThrownBy(() -> {
            line.insertSection(new Section(13, B역, C역));
        }).isInstanceOf(InvalidDistanceException.class);
    }

    @Test
    void 노선에_이미_추가된_지하철역_구간_추가() {
        // given
        Line line = new Line("2호선", "초록", 10, A역, C역);

        // when
        assertThatThrownBy(() -> {
            line.insertSection(new Section(5, A역, C역));
        }).isInstanceOf(InvalidSectionException.class);
    }

    @Test
    void 노선에_모두_추가안된_지하철역_구간_추가() {
        // given
        Line line = new Line("2호선", "초록", 10, A역, B역);

        // when
        assertThatThrownBy(() -> {
            line.insertSection(new Section(10, C역, D역));
        }).isInstanceOf(InvalidSectionException.class);
    }
}
