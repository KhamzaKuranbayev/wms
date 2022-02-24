package uz.uzcard.genesis.controller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uz.uzcard.genesis.dto.api.resp.IResponse;
import uz.uzcard.genesis.dto.api.resp.Response;
import uz.uzcard.genesis.exception.CriticException;
import uz.uzcard.genesis.exception.RpcException;
import uz.uzcard.genesis.exception.ValidatorException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ObjectStreamException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletionException;

@RestControllerAdvice
public class ControllerAdviser extends ResponseEntityExceptionHandler {

    private static final Logger log = LogManager.getLogger(ControllerAdviser.class);

    @ExceptionHandler({ValidatorException.class, RpcException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public IResponse resultException(ValidatorException e,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        log.error(e);
        e.printStackTrace();
        return new Response(1).add("message", e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public IResponse handleMultiPartException(MultipartException e, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        //process error message
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        log.error(e);
        e.printStackTrace();
        return new Response(1).add("message", e.getMessage());
    }

    @ExceptionHandler({Exception.class, Throwable.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public IResponse resultException(Exception e,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        log.error(e);
        e.printStackTrace();
        return new Response(1).add("message", e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public IResponse resultException(AccessDeniedException e,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        log.error(e);
        e.printStackTrace();
        return new Response(1).add("message", e.getMessage());
    }

    @ExceptionHandler({CriticException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public IResponse resultException(CriticException e,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        log.error(e);
        e.printStackTrace();
        return new Response(1).add("message", e.getMessage());
    }

    @ExceptionHandler({AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public IResponse resultException(AuthenticationException e,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        log.error(e);
//        e.printStackTrace();
        return new Response(1).add("message", e.getMessage());
    }

    @ExceptionHandler({ObjectStreamException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public IResponse completionException(CompletionException e, HttpServletRequest request, HttpServletResponse response) throws Throwable {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        log.error(e);
//        e.printStackTrace();
        return new Response(1).add("message", e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return errorResponse(HttpStatus.BAD_REQUEST, "Required request params missing");
    }

    private ResponseEntity<Object> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(message);
    }
}
