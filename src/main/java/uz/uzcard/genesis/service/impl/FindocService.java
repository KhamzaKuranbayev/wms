package uz.uzcard.genesis.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uz.uzcard.genesis.dto.api.req.findoc.ContractDTO;
import uz.uzcard.genesis.dto.api.req.findoc.LoginDTO;
import uz.uzcard.genesis.exception.RpcException;

import java.awt.*;
import java.util.Map;

@Service(value = "findocService")
public class FindocService {

    private final Logger log = LogManager.getLogger(FindocService.class);
    private final static String CONTENT_TYPE = "Content-Type";

    @Value(value = "${findoc.username}")
    private String findocUsername;
    @Value(value = "${findoc.password}")
    private String findocPassword;

    @Value(value = "${findoc.login.url}")
    private String findocLoginUrl;
    @Value(value = "${findoc.contract.url}")
    private String findocContractUrl;

    public LoginDTO login() {

        LoginDTO response = (LoginDTO) ApiConnector.newBuilder(LoginDTO.class)
                .setUrl(findocLoginUrl)
                .addHeader(CONTENT_TYPE, "application/json-patch+json")
                .addParam("Username", findocUsername)
                .addParam("Password", findocPassword)
                .post()
                .build();

        if (response == null)
            throw new RpcException("Финдок билан логин қилишда хатолик");

        return response;
    }

    public ContractDTO getContract(String name) {
        LoginDTO login = login();

        ContractDTO contractDTO = (ContractDTO) ApiConnector.newBuilder(ContractDTO.class)
                .setUrl(findocContractUrl)
                .addHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .addHeader("Authorization", "Basic " + login.getToken())
                .post()
                .build();

        if (contractDTO.getError() != null) {
            Map<String, String> error = contractDTO.getError();
            log.error("Финдоc error. code {}, message: {}", error.get("code"), error.get("message"));
            throw new RpcException(String.format("Финдоc error !!! <br/> code : %s <br/> mesage: %s", error.get("code"), error.get("message")));
        }
        return contractDTO;
    }
}
