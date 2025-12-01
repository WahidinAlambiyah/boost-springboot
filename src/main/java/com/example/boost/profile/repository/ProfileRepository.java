package com.example.boost.profile.repository;

import com.example.boost.profile.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, String> {
}
