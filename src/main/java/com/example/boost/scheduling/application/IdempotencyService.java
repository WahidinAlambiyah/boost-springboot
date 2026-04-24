package com.example.boost.scheduling.application;

import com.example.boost.domain.entity.IdempotencyRecord;
import com.example.boost.exception.BadRequestException;
import com.example.boost.scheduling.infrastructure.IdempotencyRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    public <T> Optional<T> getStoredResponse(String scope, String idempotencyKey, Class<T> responseClass) {
        return idempotencyRecordRepository.findByScopeAndIdempotencyKey(scope, idempotencyKey)
                .map(IdempotencyRecord::getResponsePayload)
                .map(payload -> deserialize(payload, responseClass));
    }

    public void saveResponse(String scope, String idempotencyKey, Object response) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setScope(scope);
        record.setIdempotencyKey(idempotencyKey);
        record.setResponsePayload(serialize(response));
        try {
            idempotencyRecordRepository.save(record);
        } catch (DataIntegrityViolationException ignored) {
            // request with same key already persisted concurrently
        }
    }

    private String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to serialize idempotent response");
        }
    }

    private <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to deserialize idempotent response");
        }
    }
}
