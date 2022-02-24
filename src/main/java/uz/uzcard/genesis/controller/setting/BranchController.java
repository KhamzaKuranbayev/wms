package uz.uzcard.genesis.controller.setting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import uz.uzcard.genesis.dto.api.req.setting.BranchRequest;
import uz.uzcard.genesis.hibernate.dao.BranchDao;
import uz.uzcard.genesis.hibernate.entity._Branch;

import java.util.List;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/api/branch")
public class BranchController {

    @Autowired
    private BranchDao branchDao;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get All Parents")
    @GetMapping(value = "/parents", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<_Branch> parents() {
        return branchDao.findParents().collect(Collectors.toList());
    }

    @Transactional
    @ApiOperation(value = "Save branch")
    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void save(@RequestBody BranchRequest request) {
        _Branch branch = branchDao.getByMfo(request.getMfo());
        _Branch parent = branchDao.getByMfo(request.getParentMfo());
        if (branch == null) {
            branch = new _Branch();
            branch.setMfo(request.getMfo());
        }
        branch.setParent(parent);
        branch.setName(request.getName());
        branchDao.save(branch);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @ApiOperation(value = "Get child branches")
    @GetMapping(value = "/child", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<_Branch> parents(@RequestBody String mfo) {
        return branchDao.findChild(mfo).collect(Collectors.toList());
    }
}