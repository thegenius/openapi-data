package com.lvonce;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;

public interface DataRepository {

    @Data
    class DataRepositoryException extends Exception {
        private String errorCode;
        private String errorMsg;

        public DataRepositoryException(String errorCode, String errorMsg) {
            super();
            this.errorCode = errorCode;
            this.errorMsg = errorMsg;
        }
    }

    ObjectNode executeQuery(String sourceName, String command) throws DataRepositoryException;
    ObjectNode executeUpdate(String sourceName, String command) throws DataRepositoryException;
}
