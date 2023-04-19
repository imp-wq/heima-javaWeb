package com.itniuma.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AccountService {
    void transfer(String out, String in, Double money);
    void showAll();
}
