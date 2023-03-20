package com.orsonwu.limited_sale.services;

import com.orsonwu.limited_sale.db.dao.LimitedSaleCommodityDao;
import com.orsonwu.limited_sale.db.dao.LimitedSaleEventDao;
import com.orsonwu.limited_sale.db.po.LimitedSaleCommodity;
import com.orsonwu.limited_sale.db.po.LimitedSaleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EventHtmlPageService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LimitedSaleEventDao limitedSaleEventDao;

    @Autowired
    private LimitedSaleCommodityDao limitedSaleCommodityDao;

    /**
     *
     * @throws Exception
     */
    public void createEventHtml(long saleEventId) {

        PrintWriter writer = null;
        try {
            LimitedSaleEvent limitedSaleEvent = limitedSaleEventDao.queryLimitedSaleEventById(saleEventId);
            LimitedSaleCommodity limitedSaleCommodity = limitedSaleCommodityDao.queryLimitedSaleCommodityById(limitedSaleEvent.getCommodityId());
            //Fill the event and item info into the result map
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("saleEvent", limitedSaleEvent);
            resultMap.put("saleCommodity", limitedSaleCommodity);
            resultMap.put("salePrice", limitedSaleEvent.getSalePrice());
            resultMap.put("originalPrice", limitedSaleEvent.getOriginalPrice());
            resultMap.put("commodityId", limitedSaleEvent.getCommodityId());
            resultMap.put("commodityName", limitedSaleCommodity.getCommodityName());
            resultMap.put("commodityDesc", limitedSaleCommodity.getCommodityDesc());

            Context context = new Context();
            context.setVariables(resultMap);

            // create the output stream
            File file = new File("src/main/resources/templates/" + "limitedsale_commodity_" + saleEventId + ".html");
            writer = new PrintWriter(file);
            // process the static page
            templateEngine.process("limitedsale_commodity", context, writer);
        } catch (Exception e) {
            log.error(e.toString());
            log.error("page statictization error：" + saleEventId);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}

