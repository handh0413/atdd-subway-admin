package nextstep.subway.domain;

import nextstep.subway.exception.InvalidSectionException;
import nextstep.subway.exception.SectionNotFoundException;
import nextstep.subway.exception.StationNotFoundException;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.*;

@Embeddable
public class Sections {

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL)
    private final List<Section> list;

    public Sections() {
        list = new ArrayList<>();
    }

    public Sections(List<Section> list) {
        this.list = list;
    }

    public void addSection(Section section) {
        list.add(section);
    }

    public List<Section> getList() {
        return list;
    }

    public void insertSection(Line line, Section section) {
        insertSectionWhenSectionIsHead(line, section);
        insertSectionWhenSectionIsTail(line, section);
        if (containBothStation(section)) {
            return;
        }
        insertSectionWhenStationIsIncluded(line, section);
    }

    public void insertSectionWhenSectionIsHead(Line line, Section section) {
        Station beforeLineUpStation = getLineUpStation();
        if (beforeLineUpStation.equals(section.getDownStation())) {
            addSection(section, line);
        }
    }

    public void insertSectionWhenSectionIsTail(Line line, Section section) {
        Station beforeLineDownStation = getLineDownStation();
        if (beforeLineDownStation.equals(section.getUpStation())) {
            addSection(section, line);
        }
    }

    public void insertSectionWhenStationIsIncluded(Line line, Section insertSection) {
        Optional<Section> frontSection = findSectionWithUpStation(insertSection.getUpStation());
        frontSection.ifPresent(section -> insertSectionFromFront(line, section, insertSection));
        if (containBothStation(insertSection)) {
            return;
        }
        Optional<Section> rearSection = findSectionWithDownStation(insertSection.getDownStation());
        rearSection.ifPresent(section -> insertSectionFromRear(line, section, insertSection));
    }

    public void insertSectionFromFront(Line line, Section section, Section insertSection) {
        Distance restDistance = section.getDistance().minusDistance(insertSection.getDistance());
        addSection(insertSection, line);
        addSection(new Section(restDistance, insertSection.getDownStation(), section.getDownStation()), line);
        removeSection(section);
    }

    public void insertSectionFromRear(Line line, Section section, Section insertSection) {
        Distance restDistance = section.getDistance().minusDistance(insertSection.getDistance());
        addSection(insertSection, line);
        addSection(new Section(restDistance, section.getUpStation(), insertSection.getUpStation()), line);
        removeSection(section);
    }

    private void addSection(Section section, Line line) {
        list.add(section);
        section.updateLine(line);
    }

    private void removeSection(Section section) {
        list.remove(section);
        section.updateLine(null);
    }

    public void deleteSection(Line line, Station station) {
        Optional<Section> leftSection = deleteLeftSection(station);
        Optional<Section> rightSection = deleteRightSection(station);
        if (leftSection.isPresent() && rightSection.isPresent()) {
            Section newSection = leftSection.get().connectSection(rightSection.get());
            addSection(newSection, line);
        }
    }

    private Optional<Section> deleteLeftSection(Station station) {
        Optional<Section> section = findSectionWithDownStation(station);
        section.ifPresent(this::removeSection);
        return section;
    }

    private Optional<Section> deleteRightSection(Station station) {
        Optional<Section> section = findSectionWithUpStation(station);
        section.ifPresent(this::removeSection);
        return section;
    }

    public boolean isLineUpStation(Station station) {
        return getLineUpStation().equals(station);
    }

    public boolean isLineDownStation(Station station) {
        return getLineDownStation().equals(station);
    }

    public Optional<Section> findSectionWithUpStation(Station upStation) {
        return list.stream()
                .filter(section -> upStation.equals(section.getUpStation()))
                .findFirst();
    }

    public Optional<Section> findSectionWithDownStation(Station downStation) {
        return list.stream()
                .filter(section -> downStation.equals(section.getDownStation()))
                .findFirst();
    }

    public Station getLineUpStation() {
        Set<Station> stationSet = getStationSet();
        this.list.forEach(section -> stationSet.remove(section.getDownStation()));
        return findFirstStation(stationSet);
    }

    public Station getLineDownStation() {
        Set<Station> stationSet = getStationSet();
        this.list.forEach(section -> stationSet.remove(section.getUpStation()));
        return findFirstStation(stationSet);
    }

    private Station findFirstStation(Set<Station> stationSet) {
        return stationSet.stream()
                .findFirst()
                .orElseThrow(StationNotFoundException::new);
    }

    private Set<Station> getStationSet() {
        Set<Station> stationSet = new HashSet<>();
        for (Section section : this.list) {
            stationSet.add(section.getUpStation());
            stationSet.add(section.getDownStation());
        }
        return stationSet;
    }

    public Section getLineUpSection() {
        Station lineUpStation = getLineUpStation();
        return findSectionWithUpStation(lineUpStation)
                .orElseThrow(SectionNotFoundException::new);
    }

    public Section getLineDownSection() {
        Station lineDownStation = getLineDownStation();
        return findSectionWithDownStation(lineDownStation)
                .orElseThrow(SectionNotFoundException::new);
    }

    public boolean containStation(Station station) {
        return list.stream().anyMatch(section -> section.containsStation(station));
    }

    public boolean containBothStation(Section section) {
        return containStation(section.getUpStation()) && containStation(section.getDownStation());
    }

    public boolean containNoneStation(Section section) {
        return !containStation(section.getUpStation()) && !containStation(section.getDownStation());
    }

    public Sections getSortedSections() {
        Section currentSection = findSectionWithUpStation(getLineUpStation()).orElseThrow(SectionNotFoundException::new);
        Section tailSection = findSectionWithDownStation(getLineDownStation()).orElseThrow(SectionNotFoundException::new);
        List<Section> sorted = new ArrayList<>();
        sorted.add(currentSection);
        while (currentSection != tailSection) {
            currentSection = findSectionWithUpStation(currentSection.getDownStation()).orElseThrow(SectionNotFoundException::new);
            sorted.add(currentSection);
        }
        return new Sections(sorted);
    }

    public List<Station> getSortedLineStations() {
        List<Section> sectionList = getSortedSections().getList();
        Station lineUpStation = sectionList.get(0).getUpStation();
        List<Station> stationList = new ArrayList<>();
        stationList.add(lineUpStation);
        for (Section section : sectionList) {
            stationList.add(section.getDownStation());
        }
        return stationList;
    }

    public void validateInsertSection(Section section) {
        if (containBothStation(section)) {
            throw new InvalidSectionException("이미 노선에 포함된 구간은 추가할 수 없습니다.");
        }

        if (containNoneStation(section)) {
            throw new InvalidSectionException("구간 내 지하철 역이 하나는 등록된 상태여야 합니다.");
        }
    }

    public void validateDeleteSection(Station station) {
        if (!containStation(station)) {
            throw new InvalidSectionException("제거할 지하철 역을 포함한 구간이 노선에 존재하지 않습니다.");
        }

        if (getList().size() == 1) {
            throw new InvalidSectionException("하나만 남은 구간은 삭제할 수 없습니다.");
        }
    }

    @Override
    public String toString() {
        return "Sections{" +
                "list=" + list +
                '}';
    }
}
