package com.orsonwu.limited_sale;


import com.orsonwu.limited_sale.services.EventHtmlPageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ThymeleafServiceTest {

    @Autowired
    private EventHtmlPageService eventHtmlPageService;

    @Test
    public void createHtmlTest(){
        eventHtmlPageService.createEventHtml(19);
    }

}
