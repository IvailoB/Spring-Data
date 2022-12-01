package com.example.football.service.impl;

import com.example.football.models.dto.xml.PlayersRootDto;
import com.example.football.models.entity.Player;
import com.example.football.repository.PlayerRepository;
import com.example.football.service.PlayerService;
import com.example.football.service.StatService;
import com.example.football.service.TeamService;
import com.example.football.service.TownService;
import com.example.football.util.ValidationUtil;
import com.example.football.util.XmlParse;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {

    private static final String FILE_PATH = "src/main/resources/files/xml/players.xml";
    private final PlayerRepository playerRepository;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final XmlParse xmlParse;
    private final TownService townService;
    private final StatService statService;
    private final TeamService teamService;

    public PlayerServiceImpl(PlayerRepository playerRepository, ValidationUtil validationUtil, ModelMapper modelMapper, XmlParse xmlParse, TownService townService, StatService statService, TeamService teamService) {
        this.playerRepository = playerRepository;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.xmlParse = xmlParse;
        this.townService = townService;
        this.statService = statService;
        this.teamService = teamService;
    }


    @Override
    public boolean areImported() {
        return playerRepository.count() > 0;
    }

    @Override
    public String readPlayersFileContent() throws IOException {
        return
                Files.readString(Path.of(FILE_PATH));
    }

    @Override
    public String importPlayers() throws JAXBException, FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        xmlParse.fromFile(FILE_PATH, PlayersRootDto.class)
                .getPlayers()
                .stream()
                .filter(playerDto -> {
                    boolean isValid = validationUtil.isValid(playerDto)
                            && townService.existTownByName(playerDto.getTown().getName())
                            && !playerRepository.existsPlayerByEmail(playerDto.getEmail())
                            && teamService.existTeamByName(playerDto.getTeam().getName())
                            && statService.isExistStatById(playerDto.getStat().getId());

                    sb.append(isValid ?
                                    String.format("Successfully imported Player %s %s - %s",
                                            playerDto.getFirstName(), playerDto.getLastName(),
                                            playerDto.getPosition())
                                    : "Invalid player")
                            .append(System.lineSeparator());
                    return isValid;
                })
                .map(playerDto -> {
                    Player player = modelMapper.map(playerDto, Player.class);
                    player.setTeam(teamService.findTeamByName(playerDto.getTeam().getName()));
                    player.setTown(townService.findTownByName(playerDto.getTown().getName()));
                    player.setStat(statService.findById(playerDto.getStat().getId()));
                    return player;
                })
                .forEach(playerRepository::save);


        return sb.toString();
    }

    @Override
    public String exportBestPlayers() {
        return playerRepository.findAllByBirthDateAfterAndBirthDateBeforeOrderByStatDescAndLastname(
                        LocalDate.of(2003, 1, 1),
                        LocalDate.of(1995, 1, 1)).stream()
                .map(player -> String.format("\"Player - %s %s\n" +
                                "\tPosition - %s\n" +
                                "\tTeam - %s\n" +
                                "\tStadium - %s\n"
                        , player.getFirstName(), player.getLastName(),
                        player.getPosition(), player.getTeam().getName(),
                        player.getTeam().getStadiumName()))
                .collect(Collectors.joining());


    }

    @Override
    public boolean isPlayerExistWithEmail(String email) {
        return playerRepository.existsPlayerByEmail(email);
    }
}
