package com.featureprobe.api.service

import com.featureprobe.api.entity.OperationLog
import com.featureprobe.api.repository.OperationLogRepository
import spock.lang.Specification

class OperationLogServiceSpec extends Specification{

    OperationLogRepository operationLogRepository

    OperationLogService operationLogService

    def setup() {
        operationLogRepository = Mock(OperationLogRepository)
        operationLogService = new OperationLogService(operationLogRepository)
    }

    def "saved operation log"() {
        when:
        operationLogService.save(new OperationLog("test", "Admin"))
        then:
        1 * operationLogRepository.save(_)
    }
}

