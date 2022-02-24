package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.CoreMap;
import uz.uzcard.genesis.dto.api.req.setting.TeamFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.TeamRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.ParentChildResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.dto.api.resp.TeamResponse;
import uz.uzcard.genesis.hibernate.entity._Team;
import uz.uzcard.genesis.service.TeamService;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.List;

@Api(value = "Team controller", description = "Jamoalar")
@RestController
@RequestMapping(value = "/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @ApiOperation(value = "View a list of Team", response = List.class)
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListResponse list(TeamFilterRequest request) {
        return teamService.list(request);
    }

    @ApiOperation(value = "One Team by id")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse get(Long id) {
        _Team team = teamService.get(id);
        CoreMap map = team.getMap(true);
        if (team.getTeamLeader() != null) {
            map.add("teamLeaderId", team.getTeamLeader().getId());
            map.add("teamLeaderName", team.getTeamLeader().getNameByLanguage());
        }
        TeamResponse response = new TeamResponse(map);
        team.getDepartments().forEach(department -> {
            CoreMap map1 = department.getMap();
            map1.add("name", department.getNameByLanguage());
            response.add(new TeamResponse(map1));
        });
        return SingleResponse.of(response);
    }

    @ApiOperation(value = "Team save or update")
    @Transactional
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse save(@RequestBody TeamRequest request) {
        return teamService.save(request);
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_ADMIN')")
    @ApiOperation(value = "Team delete")
    @Transactional
    @DeleteMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public SingleResponse delete(Long id) {
        teamService.delete(id);
        return SingleResponse.of(true);
    }
}
