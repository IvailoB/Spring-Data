package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.xml.ForecastSeedDto;
import softuni.exam.models.dto.xml.ForecastSeedRootDto;
import softuni.exam.models.entity.DayOfWeek;
import softuni.exam.models.entity.Forecast;
import softuni.exam.repository.CityRepository;
import softuni.exam.repository.ForecastRepository;
import softuni.exam.service.CityService;
import softuni.exam.service.ForecastService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParse;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForecastServiceImpl implements ForecastService {
    private static final String FILE_PATH = "src/main/resources/files/xml/forecasts.xml";

    private final ForecastRepository forecastRepository;
    private final ValidationUtil validationUtil;
    private ModelMapper modelMapper;
    private final XmlParse xmlParse;
    private final CityService cityService;
    private final CityRepository cityRepository;

    @Autowired
    public ForecastServiceImpl(ForecastRepository forecastRepository, ValidationUtil validationUtil, ModelMapper modelMapper, XmlParse xmlParse, CityService cityService, CityRepository cityRepository) {
        this.forecastRepository = forecastRepository;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.xmlParse = xmlParse;
        this.cityService = cityService;
        this.cityRepository = cityRepository;
    }

    @Override
    public boolean areImported() {
        return forecastRepository.count() > 0;
    }

    @Override
    public String readForecastsFromFile() throws IOException {
        return Files.readString(Path.of(FILE_PATH));
    }

    @Override
    public String importForecasts() throws IOException, JAXBException {
        StringBuilder sb = new StringBuilder();

        xmlParse.fromFile(FILE_PATH, ForecastSeedRootDto.class)
                .getForecasts()
                .stream()
                .filter(forecastSeedDto -> {
                    boolean isValid = validationUtil.isValid(forecastSeedDto);

                    sb.append(isValid ?
                                    String.format("Successfully import forecast %s - %.2f",
                                            forecastSeedDto.getDayOfWeek(),
                                            forecastSeedDto.getMaxTemperature())
                                    : "Invalid forecast")
                            .append(System.lineSeparator());
                    return isValid;
                })
                .map(forecastSeedDto -> {
                    Forecast forecast = modelMapper.map(forecastSeedDto, Forecast.class);
                    forecast.setMinTemperature(forecast.getMinTemperature());
                    forecast.setCity(cityService.findCityById(forecastSeedDto.getCityId()));
                    return forecast;
                })
                .forEach(forecastRepository::save);


        return sb.toString();
    }

    @Override
    public String exportForecasts() {
        return forecastRepository.findAllByDayOfWeekAndCity_PopulationLessThanOrderByMaxTemperatureDescIdAsc(DayOfWeek.SUNDAY, 150000)
                .stream()
                .map(forecast -> String.format(
                        "•\t\"City: %s:\n" +
                                "   \t\t--max temperature: %.2f\n" +
                                "   \t\t---sunrise: %s\n" +
                                "----sunset: %s\n"
                        , forecast.getCity(), forecast.getMaxTemperature(),
                        forecast.getSunrise(), forecast.getSunset()))
                .collect(Collectors.joining());
    }
}
