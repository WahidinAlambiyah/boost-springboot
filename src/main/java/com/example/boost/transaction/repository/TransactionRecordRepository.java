package com.example.boost.transaction.repository;

import com.example.boost.transaction.model.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, String> {
}
