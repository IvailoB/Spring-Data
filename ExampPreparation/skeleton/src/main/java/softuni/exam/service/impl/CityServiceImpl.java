package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dto.json.CitySeedDto;
import softuni.exam.models.entity.City;
import softuni.exam.repository.CityRepository;
import softuni.exam.service.CityService;
import softuni.exam.service.CountryService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

@Service
public class CityServiceImpl implements CityService {
    private static final String FILE_PATH = "src/main/resources/files/json/cities.json";

    private final CityRepository cityRepository;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final CountryService countryService;
    private final Gson gson;

    public CityServiceImpl(CityRepository cityRepository, ValidationUtil validationUtil, ModelMapper modelMapper, CountryService countryService, Gson gson) {
        this.cityRepository = cityRepository;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.countryService = countryService;
        this.gson = gson;
    }

    @Override
    public boolean areImported() {
        return cityRepository.count() > 0;
    }

    @Override
    public String readCitiesFileContent() throws IOException {
        return Files.readString(Path.of(FILE_PATH));
    }

    @Override
    public String importCities() throws IOException {
        StringBuilder sb = new StringBuilder();

        Arrays.stream(gson.fromJson(readCitiesFileContent(), CitySeedDto[].class))
                .filter(citySeedDto -> {
                    boolean isValid = validationUtil.isValid(citySeedDto)
                            && !cityRepository.existsCityByCityName(citySeedDto.getCityName());

                    sb.append(isValid ?
                                    String.format("Successfully imported city %s - %d",
                                            citySeedDto.getCityName(),
                                            citySeedDto.getPopulation())
                                    : "Invalid City")
                            .append(System.lineSeparator());

                    return isValid;
                })

                .map(citySeedDto -> {
                    City city = modelMapper.map(citySeedDto, City.class);
                    city.setCountry(countryService.findById(citySeedDto.getCountry()));
                    return city;
                })
                .forEach(cityRepository::save);

        return sb.toString();
    }

    @Override
    public boolean isExistCityById(Long id) {
        return
                cityRepository.existsCityById(id);
    }

    @Override
    public City findCityById(Long id) {
        return cityRepository.findCityById(id).orElse(null);
    }
}
