package com.azki.banking_system.repositories;

import com.azki.banking_system.entities.BankAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccountEntity, Long> {

    Optional<BankAccountEntity> findByAccountNumber(@Param("accountNumber") String accountNumber);

    void deleteByAccountNumber(String accountNumber);
}
