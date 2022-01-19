package com.lvonce.openapi.data;

import lombok.Data;

public interface QueryLanguageMapper {

    @Data
    class QueryCommand {
        private String source;
        private String command;
        private String type;
        public static QueryCommand of(String type, String source, String command) {
            QueryCommand queryCommand = new QueryCommand();
            queryCommand.type = type;
            queryCommand.source = source;
            queryCommand.command = command;
            return queryCommand;
        }
    }

    @Data
    class QueryLanguageException extends Exception {
        private String errorCode;
        private String errorMsg;

        public QueryLanguageException(String errorCode, String errorMsg) {
            super();
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
        }
    }

    QueryCommand generateQueryCommand(RequestHandler.RequestHandleResult handleResult) throws QueryLanguageException;

}
