package nextstep.subway.section.domain;

import nextstep.subway.exception.NotFoundException;
import nextstep.subway.station.domain.Station;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Embeddable
public class Sections {
    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<Section> sections = new ArrayList<>();

    public void addSection(Section section) {
        sections.add(section);
    }

    public List<Section> getSections() {
        return sections;
    }

    public boolean validDuplicationSection(Section section) {
        return sections.stream().anyMatch(it -> it.getUpStation().equals(section.getUpStation())
                   && it.getDownStation().equals(section.getDownStation()));
    }

    public void updateUpStation(Section section) {
        Station inputUpStation = section.getUpStation();
        sections.stream()
                .filter(it -> it.getUpStation().equals(inputUpStation))
                .findFirst()
                .ifPresent(it -> it.updateUpStation(section));
    }

    public void updateDownStation(Section section) {
        Station inputDownStation = section.getDownStation();
        sections.stream()
                .filter(it -> it.getDownStation().equals(inputDownStation))
                .findFirst()
                .ifPresent(it -> it.updateDownStation(section));
    }

    public List<Station> orderSection() {
        List<Station> stations = new ArrayList<>();
        Station station = getFirstSection();
        stations.add(station);

        while (isAfterSection(station)) {
            Section afterStation = findAfterSection(station);
            station = afterStation.getDownStation();
            stations.add(station);
        }
        return stations;
    }

    public boolean isBeforeSection(Station station) {
        return sections.stream().anyMatch(it -> it.getDownStation().equals(station));
    }

    public boolean isAfterSection(Station station) {
        return sections.stream().anyMatch(it -> it.getUpStation().equals(station));
    }

    public Station getFirstSection() {
        Station station = sections.get(0).getUpStation();
        while (isBeforeSection(station)) {
            Section section = findBeforeSection(station);
            station = section.getUpStation();
        }
        return station;
    }

    public Section findBeforeSection(Station station) {
        return sections.stream()
                .filter(it -> it.getDownStation().equals(station))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    private Section findAfterSection(Station station) {
        return sections.stream()
                .filter(it -> it.getUpStation().equals(station))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }
}
