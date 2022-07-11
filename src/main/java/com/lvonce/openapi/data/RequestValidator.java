package com.lvonce.openapi.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.undertow.server.HttpServerExchange;
import lombok.Data;

public interface RequestValidator {
    @Data
    class ValidateException extends Exception {
        private String errorCode;
        private String errorMsg;

        public ValidateException(String errorCode, String errorMsg) {
            super();
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
        }
    }

    ObjectNode validate(RequestMatcher.RequestMatchResult matchResult, HttpServerExchange exchange) throws ValidateException;


}
