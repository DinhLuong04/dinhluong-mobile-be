package com.dinhluong.dlmstore.config;

import com.dinhluong.dlmstore.repository.ProductRepository;
import com.dinhluong.dlmstore.service.tools.ProductDataEnricher;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDataStartupRunner implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductDataEnricher productDataEnricher;

    @Override
    @Transactional
    public void run(String... args) {

        log.info("START REBUILD PRODUCT DATA...");

        var products = productRepository.findAllForRebuild();

        for (var product : products) {

            productDataEnricher.enrichProductBeforeSave(product);

            log.info(
                    "Rebuilt product: {} - {}",
                    product.getId(),
                    product.getName()
            );
        }

        log.info("FINISH REBUILD PRODUCT DATA");
    }
}