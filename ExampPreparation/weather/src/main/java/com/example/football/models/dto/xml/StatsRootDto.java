package com.example.football.models.dto.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "stats")
@XmlAccessorType(XmlAccessType.FIELD)
public class StatsRootDto {
    @XmlElement(name = "stat")
    List<StatDto> statDtos;

    public List<StatDto> getStatDtos() {
        return statDtos;
    }

    public void setStatDtos(List<StatDto> statDtos) {
        this.statDtos = statDtos;
    }
}
