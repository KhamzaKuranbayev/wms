package uz.uzcard.genesis.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.FilterParameters;
import uz.uzcard.genesis.dto.api.req.setting.TeamFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.TeamRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;
import uz.uzcard.genesis.hibernate.dao.DepartmentDao;
import uz.uzcard.genesis.hibernate.dao.TeamDao;
import uz.uzcard.genesis.hibernate.entity._Department;
import uz.uzcard.genesis.hibernate.entity._State;
import uz.uzcard.genesis.hibernate.entity._Team;
import uz.uzcard.genesis.service.TeamService;
import uz.uzcard.genesis.uitls.GlobalizationExtentions;
import uz.uzcard.genesis.uitls.ServerUtils;
import uz.uzcard.genesis.uitls.SessionUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamDao teamDao;
    @Autowired
    private DepartmentDao departmentDao;

    @Override
    public ListResponse list(TeamFilterRequest request) {
        FilterParameters filter = new FilterParameters() {{
            setStart(request.getLimit() * request.getPage());
            setSize(request.getLimit());
            setSortColumn(request.getSortBy());

            if (!ServerUtils.isEmpty(request.getId()))
                addLong("id", request.getId());
            if (!ServerUtils.isEmpty(request.getName()))
                add("name", request.getName());
            if (request.getDepartmentIds() != null)
                addLongs("departments", request.getDepartmentIds());
        }};
        filter.setSortType(request.getSortDirection());
        Stream<_Team> teams = teamDao.list(filter);

        Integer total = teamDao.total(filter);
        return ListResponse.of(teams, total, (team, map) -> {
            if (team.getTeamLeader() != null) {
                map.add("teamLeaderId", team.getTeamLeader().getId());
                map.add("teamLeaderName", team.getTeamLeader().getNameByLanguage());
            }
            return map;
        });
    }

    @Override
    public SingleResponse save(TeamRequest request) {
        if (request.getName() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("NAME_REQUIRED"));
        if (request.getTeamLeader() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("TEAM_LEADER_REQUIRED"));
        _Department teamLeader = departmentDao.get(request.getTeamLeader());
        if (teamLeader == null)
            throw new RpcException(GlobalizationExtentions.localication("TEAM_LEADER_NOT_FOUND"));
        if (request.getDepartments() == null)
            throw new ValidatorException(GlobalizationExtentions.localication("DEPARTMENT_REQUIRED"));
        _Team team;
        if (request.getId() == null) {
            team = new _Team();
        } else {
            team = teamDao.get(request.getId());
            if (team == null)
                throw new RpcException(GlobalizationExtentions.localication("TEAM_NOT_FOUND"));
        }
        team.setName(request.getName());
        team.setTeamLeader(teamLeader);
        team.setDepartments(departmentDao.getDepartmentByIds(request.getDepartments()).collect(Collectors.toList()));
        team = teamDao.save(team);
        String language = SessionUtils.getInstance().getLanguage();
        return SingleResponse.of(team, (team1, map) -> {
            if (team1.getTeamLeader() != null) {
                map.add("teamLeaderId", team1.getTeamLeader().getId());
                map.add("teamLeaderName", team1.getTeamLeader().getNameByLanguage());
            }
            return map;
        });
    }

    @Override
    public void delete(Long id) {
        if (id == null)
            throw new ValidatorException(GlobalizationExtentions.localication("TEAM_NOT_REQUIRED"));
        _Team team = teamDao.get(id);
        if (team == null)
            throw new RpcException("TEAM_NOT_FOUND");
        team.setState(_State.DELETED);
        teamDao.save(team);
    }

    @Override
    public _Team get(Long id) {
        if (id == null)
            throw new ValidatorException(GlobalizationExtentions.localication("TEAM_NOT_REQUIRED"));
        return teamDao.get(id);
    }

    @Override
    public List<Long> getMyChildTeamIds() {
        return teamDao.getMyChildTeamIds();
    }
}
