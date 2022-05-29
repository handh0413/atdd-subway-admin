package nextstep.subway.domain;

import nextstep.subway.dto.LineRequest;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Line extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    @Column
    private String color;
    @Embedded
    private Distance distance;

    @OneToOne
    @JoinColumn(name = "up_station_id")
    private Station upStation;

    @OneToOne
    @JoinColumn(name = "down_station_id")
    private Station downStation;

    protected Line() {
    }

    public Line(String name, String color, int distance) {
        this.name = name;
        this.color = color;
        this.distance = new Distance(distance);
    }

    public Line setUpStation(Station upStation) {
        this.upStation = upStation;
        return this;
    }

    public Line setDownStation(Station downStation) {
        this.downStation = downStation;
        return this;
    }

    public void updateLine(String name, String color, int distance) {
        this.name = name;
        this.color = color;
        this.distance = new Distance(distance);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Line line = (Line) o;
        return name.equals(line.name) &&
                color.equals(line.color) &&
                distance.equals(line.distance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color, distance);
    }
}
