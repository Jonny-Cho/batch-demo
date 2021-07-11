package com.example.batchdemo.job;

import com.example.batchdemo.entity.Pay;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class JpaPagingItemReaderJobConfiguration {

    public static final String JOB_NAME = "jpaPagingItemReaderJob";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job jpaPagingItemReaderJob() {
        return jobBuilderFactory.get(JOB_NAME)
            .start(jpaPagingItemReaderStep())
            .build();
    }

    @Bean(BEAN_PREFIX + "step")
    public Step jpaPagingItemReaderStep() {
        return stepBuilderFactory.get(BEAN_PREFIX + "step")
            .<Pay, Pay>chunk(chunkSize)
            .reader(jpaPagingItemReader())
            .writer(jpaPagingItemWriter())
            .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> jpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<Pay>()
            .name(BEAN_PREFIX + "reader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(chunkSize)
            .queryString("SELECT p FROM Pay p WHERE amount >= 2000")
            .build();
    }

    private ItemWriter<Pay> jpaPagingItemWriter() {
        // ItemWriter의 write 메서드를 오버라이딩한 익명 클래스 람다식
        return items -> {
            for (Pay pay : items) {
                log.info("Current Pay={}", pay);
            }
        };
    }
}
