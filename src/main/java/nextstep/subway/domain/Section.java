package nextstep.subway.domain;

import javax.persistence.*;

@Entity
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Distance distance;

    @OneToOne
    @JoinColumn(name = "up_station_id")
    private Station upStation;

    @OneToOne
    @JoinColumn(name = "down_station_id")
    private Station downStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id")
    private Line line;

    protected Section() {
    }

    public Section(int distance, Station upStation, Station downStation) {
        this.distance = new Distance(distance);
        this.upStation = upStation;
        this.downStation = downStation;
    }

    public Section(Distance distance, Station upStation, Station downStation, Line line) {
        this.distance = distance;
        this.upStation = upStation;
        this.downStation = downStation;
        this.line = line;
    }

    public boolean isValidSection() {
        return getUpStation() != null && getDownStation() != null;
    }

    public void updateSection(Station upStation, Station downStation, Distance distance) {
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = distance;
    }

    public boolean containsStation(Station station) {
        return station.equals(getUpStation())
                || station.equals(getDownStation());
    }

    public Long getId() {
        return id;
    }

    public Distance getDistance() {
        return distance;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }

    public Line getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "Section{" +
                "id=" + id +
                ", distance=" + distance +
                ", upStation=" + upStation +
                ", downStation=" + downStation +
                '}';
    }
}
