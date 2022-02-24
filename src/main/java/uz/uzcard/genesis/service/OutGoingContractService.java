package uz.uzcard.genesis.service;

import org.springframework.web.multipart.MultipartFile;
import uz.uzcard.genesis.dto.api.req.setting.OutGoingContractFilterRequest;
import uz.uzcard.genesis.dto.api.req.setting.OutGoingContractRequest;
import uz.uzcard.genesis.hibernate.base.PageStream;
import uz.uzcard.genesis.hibernate.entity._OutGoingContract;

public interface OutGoingContractService {

    _OutGoingContract save(OutGoingContractRequest request, MultipartFile file);

    PageStream<_OutGoingContract> list(OutGoingContractFilterRequest request);
}
