package com.example.boost.lookup.application;

import com.example.boost.domain.dto.LookupOptionResponse;
import com.example.boost.lookup.infrastructure.LookupRepository;
import com.example.boost.security.CurrentActorProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LookupService {
    private final LookupRepository lookupRepository;
    private final CurrentActorProvider currentActorProvider;

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> academies(String search, Integer limit) {
        return lookupRepository.academies(currentActorProvider.getCurrentActor(), search, limit);
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> academyLocations(UUID academyId, String search, Integer limit) {
        return lookupRepository.academyLocations(currentActorProvider.getCurrentActor(), academyId, search, limit);
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> coaches(UUID academyId, String search, Integer limit) {
        return lookupRepository.coaches(currentActorProvider.getCurrentActor(), academyId, search, limit);
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> students(UUID academyId, String status, String level, String search, Integer limit) {
        return lookupRepository.students(currentActorProvider.getCurrentActor(), academyId, status, level, search, limit);
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> classGroups(UUID academyId, String search, Integer limit) {
        return lookupRepository.classGroups(currentActorProvider.getCurrentActor(), academyId, search, limit);
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> classSessions(UUID academyId, LocalDate date, String status, String search, Integer limit) {
        return lookupRepository.classSessions(currentActorProvider.getCurrentActor(), academyId, date, status, search, limit);
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> assessmentSkills(UUID academyId, String search, Integer limit) {
        return lookupRepository.assessmentSkills(currentActorProvider.getCurrentActor(), academyId, search, limit);
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> trainingPackages(UUID academyId, String search, Integer limit) {
        return lookupRepository.trainingPackages(currentActorProvider.getCurrentActor(), academyId, search, limit);
    }

    @Transactional(readOnly = true)
    public List<LookupOptionResponse> events(UUID academyId, String search, Integer limit) {
        return lookupRepository.events(currentActorProvider.getCurrentActor(), academyId, search, limit);
    }
}
