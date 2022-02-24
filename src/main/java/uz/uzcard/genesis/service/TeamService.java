package uz.uzcard.genesis.service;

import uz.uzcard.genesis.dto.api.req.setting.TeamFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.TeamRequest;
import uz.uzcard.genesis.dto.api.resp.ListResponse;
import uz.uzcard.genesis.dto.api.resp.SingleResponse;
import uz.uzcard.genesis.hibernate.entity._Team;

import java.util.List;

public interface TeamService {

    ListResponse list(TeamFilterRequest request);

    SingleResponse save(TeamRequest request);

    void delete(Long id);

    _Team get(Long id);

    List<Long> getMyChildTeamIds();
}
